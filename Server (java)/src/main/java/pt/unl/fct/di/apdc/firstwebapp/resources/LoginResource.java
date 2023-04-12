package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.*;

import java.util.logging.Logger;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService(); //CLOUD
    //Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081").setProjectId("calm-virtue-379315").build().getService(); //LOCAL


	public LoginResource() {
	}

	/*@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if (username.equals("pedroa")) {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}*/
	
	
	@POST
	@Path("/local")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginV1(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		
		Entity user = datastore.get(userKey);
		if (user == null) {
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}
		
		String hashedPwd = (String) user.getString("password");
		if (hashedPwd.equals(DigestUtils.sha512Hex(data.password))) {
			
			datastore.put(user);
			AuthToken token = new AuthToken(data.username, user.getString("role"));
			LOG.info("User '" + data.username + "' logged in successfully.");
			return Response.ok(g.toJson(token)).build();

			
		} else return Response.status(Status.FORBIDDEN).build(); 
		
	}
	
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginV2(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		LOG.fine("Login attempt by user: " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory().addAncestors(PathElement.of("Person", data.username)).setKind("UserStats").newKey("counters");
		
		Key logKey = datastore.allocateId(datastore.newKeyFactory().addAncestors(PathElement.of("Person", data.username)).setKind("UserLog").newKey());
		
		Transaction txn = datastore.newTransaction();
		try {
			
			Entity user = datastore.get(userKey);
			if (user == null) {
				LOG.warning("Failed login attempt for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity stats = txn.get(ctrsKey);
			if (stats == null) {
				stats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 0L)
						.set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now())
						.set("user_last_login", Timestamp.now())
						.build();
			}
			
			String hashedPwd = (String) user.getString("password");
			if (hashedPwd.equals(DigestUtils.sha512Hex(data.password))) {
				
				Entity log = Entity.newBuilder(logKey)
						.set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost())
						.set("user_login_latlon", StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong")).setExcludeFromIndexes(true)
						.build())
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_tine", Timestamp.now())
						.build();
				
				AuthToken token = new AuthToken(data.username, user.getString("role"));
				
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 1L + stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 0L)
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", Timestamp.now())
						.set("active_token", token.tokenID)
						.build();
				
				txn.put(log, ustats);
				txn.commit();
				
				LOG.info("User '" + data.username + "' logged in successfully.");
				return Response.ok(g.toJson(token)).build();
			} else {
				
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 1L + stats.getLong("user_stats_failed"))
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", stats.getTimestamp("user_last_login"))
						.set("user_last_attempt", Timestamp.now())
						.build();
				
				txn.put(ustats);
				txn.commit();
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
			
		} catch(Exception e){
			
			txn.rollback();
			LOG.warning(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if(txn.isActive()) {
				
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
				
			}
			
		}
			
	}
	
}
