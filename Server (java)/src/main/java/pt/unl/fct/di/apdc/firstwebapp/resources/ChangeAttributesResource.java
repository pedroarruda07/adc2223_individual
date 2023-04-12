package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAttributesData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePassword;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserInformation;

import java.util.logging.Logger;

@Path("/change")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeAttributesResource {

	private static final Logger LOG = Logger.getLogger(ChangeAttributesResource.class.getName());
	private final Gson g = new Gson();
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ChangeAttributesResource() {
	}

	private void change(ChangeAttributesData data, Key userKey, Transaction txn, Entity userToChange) {

		Entity updatedUser = Entity.newBuilder(userKey)
				.set("password", userToChange.getString("password"))
				.set("email", data.email)
				.set("name", data.name)
				.set("phone_number", StringValue.newBuilder(data.phone).setExcludeFromIndexes(true).build())
				.set("address", StringValue.newBuilder(data.address).setExcludeFromIndexes(true).build())
				.set("public", data.privacy)
				.set("role", userToChange.getString("role"))
				.set("state", data.state)
				.set("creation_time", userToChange.getTimestamp("creation_time")).build();

		txn.put(updatedUser);
		txn.commit();

	}
	
	@POST
	@Path("/myInfo")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getInformation(LoginData data) {
		
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user == null) {
			
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		String username = user.getKey().getName();
		String email = user.getString("email");
		String name = user.getString("name");
		String phone = user.getString("phone_number");
		String address = user.getString("address");
		String role = user.getString("role");
		boolean privacy = user.getBoolean("public");
		boolean state = user.getBoolean("state");
		
		UserInformation info = new UserInformation(username, email, name, phone, address, role, privacy, state);
		
		return Response.ok(g.toJson(info)).build();
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeAttributes(ChangeAttributesData data) {
		
		
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Key userKey2 = datastore.newKeyFactory().setKind("Person").newKey(data.userToChange);

		Transaction txn = datastore.newTransaction();
		try {
			
			Key ctrsKey = datastore.newKeyFactory().addAncestors(PathElement.of("Person", data.username)).setKind("UserStats").newKey("counters");
			Entity stats = datastore.get(ctrsKey); 
			
			if (stats == null ) {	
				
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}
			
			String userToken = stats.getString("active_token");
			
			if (!data.tokenID.equals(userToken) || data.tokenExp < System.currentTimeMillis()) {
				
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}

			Entity user = datastore.get(userKey);
			Entity userToChange = datastore.get(userKey2);

			if (user == null || userToChange == null) {

				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}


			String role = user.getString("role");
			String roleToChange = userToChange.getString("role");

			switch (role) {

			case "USER":

				if (!data.username.equals(data.userToChange)) {

					txn.rollback();
					return Response.status(Status.FORBIDDEN).build();
				} else {

					Entity updatedUser = Entity.newBuilder(userKey2)
							.set("password", userToChange.getString("password"))
							.set("email", userToChange.getString("email"))
							.set("name", userToChange.getString("name"))
							.set("phone_number", StringValue.newBuilder(data.phone).setExcludeFromIndexes(true).build())
							.set("address", StringValue.newBuilder(data.address).setExcludeFromIndexes(true).build())
							.set("public", data.privacy)
							.set("role", userToChange.getString("role"))
							.set("state", userToChange.getBoolean("state"))
							.set("creation_time", userToChange.getTimestamp("creation_time")).build();

					txn.put(updatedUser);
					txn.commit();
					return Response.ok("Information changed").build();

				}

			case "GBO":

				if (!roleToChange.equals("USER")) {

					txn.rollback();
					return Response.status(Status.FORBIDDEN).build();
				} else {

					change(data, userKey2, txn, userToChange);
					return Response.ok("Information changed").build();

				}

			case "GA":

				if (!roleToChange.equals("USER") && !roleToChange.equals("GBO")) {

					txn.rollback();
					return Response.status(Status.FORBIDDEN).build();
				} else {

					change(data, userKey2, txn, userToChange);
					return Response.ok("Information changed").build();

				}

			case "GS":

				if (!roleToChange.equals("USER") && !roleToChange.equals("GBO") && !roleToChange.equals("GA")) {

					txn.rollback();
					return Response.status(Status.FORBIDDEN).build();
				} else {

					change(data, userKey2, txn, userToChange);
					return Response.ok("Information changed").build();

				}

			case "SU":

				Entity updatedUser = Entity.newBuilder(userKey2)
						.set("password", userToChange.getString("password"))
						.set("email", data.email)
						.set("name", data.name)
						.set("phone_number", StringValue.newBuilder(data.phone).setExcludeFromIndexes(true).build())
						.set("address", StringValue.newBuilder(data.address).setExcludeFromIndexes(true).build())
						.set("public", data.privacy)
						.set("role", data.role)
						.set("state", data.state)
						.set("creation_time", userToChange.getTimestamp("creation_time")).build();

				txn.put(updatedUser);
				txn.commit();
				return Response.ok("Information changed").build();

			default:
				
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to check user's role.").build();
			}

		} catch (Exception e) {

			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {

				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();

			}

		}

	}

	@POST
	@Path("/password")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(ChangePassword data) {

		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Entity user = datastore.get(userKey);
		
		Key ctrsKey = datastore.newKeyFactory().addAncestors(PathElement.of("Person", data.username)).setKind("UserStats").newKey("counters");
		Entity stats = datastore.get(ctrsKey); 
		
		if (stats == null ) {	
			
			return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
		}
		
		String userToken = stats.getString("active_token");
		
		if (!data.tokenID.equals(userToken) || data.tokenExp < System.currentTimeMillis()) {
			
			return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
		}

		if (user == null) {

			return Response.status(Status.BAD_REQUEST).entity("User doesn't exist").build();
		}
	

		String hashedPwd = user.getString("password");
		if (hashedPwd.equals(DigestUtils.sha512Hex(data.oldPwd))) {

			if (data.newPwd.equals(data.newPwdConf)) {

				Entity updatedUser = Entity.newBuilder(userKey).set("password", DigestUtils.sha512Hex(data.newPwd))
						.set("email", user.getString("email")).set("name", user.getString("name"))
						.set("phone_number",
								StringValue.newBuilder(user.getString("phone_number")).setExcludeFromIndexes(true)
										.build())
						.set("address",
								StringValue.newBuilder(user.getString("address")).setExcludeFromIndexes(true).build())
						.set("public", user.getBoolean("public")).set("role", user.getString("role"))
						.set("state", user.getBoolean("state")).set("creation_time", user.getTimestamp("creation_time"))
						.build();

				datastore.put(updatedUser);
				return Response.ok("Password updated for user: " + data.username).build();
			} else
				return Response.status(Status.BAD_REQUEST).entity("Password and confirmation don't match").build();
		} else
			return Response.status(Status.BAD_REQUEST).entity("Wrong Password").build();

	}

}