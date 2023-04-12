package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.DeleteRequest;

import java.util.logging.Logger;

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteResource {

	private static final Logger LOG = Logger.getLogger(DeleteResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public DeleteResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteUser(DeleteRequest data) {
		LOG.fine("Delete attempt by user: " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Key userKey2 = datastore.newKeyFactory().setKind("Person").newKey(data.usernameToDelete);
		
		
		Transaction txn = datastore.newTransaction();
		try {
			
			Key ctrsKey = datastore.newKeyFactory().addAncestors(PathElement.of("Person", data.username)).setKind("UserStats").newKey("counters");
			Key ctrsKey2 = datastore.newKeyFactory().addAncestors(PathElement.of("Person", data.usernameToDelete)).setKind("UserStats").newKey("counters");
			Entity stats = datastore.get(ctrsKey); 
			
			if (stats == null ) {	
				txn.rollback();
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}
			
			String userToken = stats.getString("active_token");
			
			if (!data.tokenID.equals(userToken) || data.tokenExp < System.currentTimeMillis()) {
				txn.rollback();
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}
			
			Entity user = datastore.get(userKey);
			Entity userToDelete = datastore.get(userKey2);
			
			if (user == null || userToDelete == null) {
				
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}
			
				
			String role = user.getString("role");
			String roleToDelete = userToDelete.getString("role");
			
			switch(role) {
			
			case "USER":
				
					if (!data.username.equals(data.usernameToDelete)) {
						txn.rollback();
						return Response.status(Status.FORBIDDEN).build();
					
					} else {
						
						txn.delete(userKey);
						txn.delete(ctrsKey);
						txn.commit();
						return Response.ok("User removed: " + data.usernameToDelete).build();
					}
					
			case "GBO":
				
					if (!roleToDelete.equals("USER")) {
						txn.rollback();
						return Response.status(Status.FORBIDDEN).build();
					} else {
						
						txn.delete(userKey2);
						txn.delete(ctrsKey2);
						txn.commit();
						return Response.ok("User removed: " + data.usernameToDelete).build();
					}
				
			case "GA":
				
					if (!roleToDelete.equals("USER") && !roleToDelete.equals("GBO")) {
						txn.rollback();
						return Response.status(Status.FORBIDDEN).build();
					}else {
						
						txn.delete(userKey2);
						txn.delete(ctrsKey2);
						txn.commit();
						return Response.ok("User removed: " + data.usernameToDelete).build();
					}
				
			case "GS":
				
					if (!roleToDelete.equals("USER") && !roleToDelete.equals("GBO") && !roleToDelete.equals("GA")) {
						txn.rollback();
						return Response.status(Status.FORBIDDEN).build();
					}else {
						
						txn.delete(userKey2);
						txn.delete(ctrsKey2);
						txn.commit();
						return Response.ok("User removed: " + data.usernameToDelete).build();
					}
				
			case "SU":
				
					if (!roleToDelete.equals("USER") && !roleToDelete.equals("GBO") && !roleToDelete.equals("GA") && !roleToDelete.equals("GS")) {
						txn.rollback();
						return Response.status(Status.FORBIDDEN).build();
					}else {
						
						txn.delete(userKey2);
						txn.delete(ctrsKey2);
						txn.commit();
						return Response.ok("User removed: " + data.usernameToDelete).build();
					}
				
			default:
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to check user's role.").build();
			}			
			
		} catch(Exception e){
			
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if(txn.isActive()) {
				
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
				
			}
			
		}
		
	}
	
}