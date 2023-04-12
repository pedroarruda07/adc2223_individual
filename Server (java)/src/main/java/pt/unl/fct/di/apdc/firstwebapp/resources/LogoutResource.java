package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;


@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LogoutResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(LoginData data) {
		
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);

		Entity user = datastore.get(userKey);

		if (user == null) {

			return Response.status(Status.BAD_REQUEST).build();
		}
		
		Key ctrsKey = datastore.newKeyFactory().addAncestors(PathElement.of("Person", data.username)).setKind("UserStats").newKey("counters");
		Entity stats = datastore.get(ctrsKey); 
		
		Entity ustats = Entity.newBuilder(ctrsKey)
				.set("user_stats_logins", stats.getLong("user_stats_logins"))
				.set("user_stats_failed", stats.getLong("user_stats_failed"))
				.set("user_first_login", stats.getTimestamp("user_first_login"))
				.set("user_last_login", stats.getTimestamp("user_last_login"))
				.set("active_token", "")
				.build();
		
		datastore.put(ustats);
		
		return Response.ok("{}").build();
	}
	

}