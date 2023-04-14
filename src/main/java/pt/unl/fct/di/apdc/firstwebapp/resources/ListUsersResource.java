package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserInformation;
import pt.unl.fct.di.apdc.firstwebapp.util.UserInformation2;

import java.util.ArrayList;
import java.util.List;
//import java.util.logging.Logger;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListUsersResource {

	//private static final Logger LOG = Logger.getLogger(ListUsersResource.class.getName());
	private final Gson g = new Gson();
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService(); //CLOUD
	//Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081").setProjectId("calm-virtue-379315").build().getService(); //LOCAL


	public ListUsersResource() {
	}

	
	private UserInformation info(Entity userInfo) {
		
		UserInformation info = new UserInformation(userInfo.getKey().getName(), userInfo.getString("email"), userInfo.getString("name"), userInfo.getString("phone_number"), 
				userInfo.getString("address"), userInfo.getString("role"), userInfo.getBoolean("public"), 
				userInfo.getBoolean("state"));
		
		return info;
	}
	
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response listUsers(LoginData data) { 
		
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Entity user = datastore.get(userKey);
		Query<Entity> query = null;
		List<Object> usersList = new ArrayList<Object>();
		QueryResults<Entity> list = null;
		
		if (user != null) {
			
			String role = user.getString("role");
			
			switch(role) {
			
			case "USER":
				
				query = Query.newEntityQueryBuilder().setKind("Person")
				.setFilter(CompositeFilter.and(
		                PropertyFilter.eq("role", "USER"), PropertyFilter.eq("public", true), PropertyFilter.eq("state", true)))
				.build();
		
				list = datastore.run(query);
				
				list.forEachRemaining(userInfo -> {
					
					UserInformation2 info = new UserInformation2(userInfo.getKey().getName(), userInfo.getString("email"), userInfo.getString("name"));
					usersList.add(info);
				});
				
				break;	
			case "GBO":
				
				query = Query.newEntityQueryBuilder().setKind("Person")
				.setFilter( PropertyFilter.eq("role", "USER"))
				.build();
				
				list = datastore.run(query);
				
				list.forEachRemaining(userInfo -> {
					
					usersList.add(info(userInfo));
				});
					
				break;
			case "GA":
				
				query = Query.newEntityQueryBuilder().setKind("Person")
				.setFilter(PropertyFilter.eq("role", "USER")).build();
				
				list = datastore.run(query);
				
				list.forEachRemaining(userInfo -> {
					
					usersList.add(info(userInfo));
				});
				
				query = Query.newEntityQueryBuilder().setKind("Person")
						.setFilter(PropertyFilter.eq("role", "GBO")).build();
						
				list = datastore.run(query);
						
				list.forEachRemaining(userInfo -> {
					
					usersList.add(info(userInfo));
				});
				
				break;
			case "GS":
				
				query = Query.newEntityQueryBuilder().setKind("Person")
				.setFilter(PropertyFilter.eq("role", "USER")).build();
				
				list = datastore.run(query);
				
				list.forEachRemaining(userInfo -> {
					
					usersList.add(info(userInfo));
				});
				
				query = Query.newEntityQueryBuilder().setKind("Person")
						.setFilter(PropertyFilter.eq("role", "GBO")).build();
						
				list = datastore.run(query);
						
				list.forEachRemaining(userInfo -> {
							
					usersList.add(info(userInfo));
					});
				
				query = Query.newEntityQueryBuilder().setKind("Person")
						.setFilter(PropertyFilter.eq("role", "GA")).build();
						
				list = datastore.run(query);
						
				list.forEachRemaining(userInfo -> {
							
					usersList.add(info(userInfo));
					});
					
				break;
			case "SU":
				
				query = Query.newEntityQueryBuilder().setKind("Person").build();
				
				list = datastore.run(query);
				
				list.forEachRemaining(userInfo -> {
					
					usersList.add(info(userInfo));
				});
				
				break;
			default:
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to check user's role.").build();
			}			
			
			
			return Response.ok(g.toJson(usersList)).build();
			
		} else return Response.status(Status.BAD_REQUEST).entity("User not found").build();
			
	}
}
