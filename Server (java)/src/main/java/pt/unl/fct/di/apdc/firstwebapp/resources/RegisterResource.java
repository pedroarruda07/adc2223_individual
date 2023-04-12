package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.*;

import java.util.logging.Logger;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService(); //CLOUD
	//Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081").setProjectId("calm-virtue-379315").build().getService(); //LOCAL

	public RegisterResource() {
	}


	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerV2(RegisterData data) {

		LOG.fine("User register attempt: " + data.username);
		
		if ( !data.validRegistration()) {
			
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}
		
		Transaction txn = datastore.newTransaction();
		try {
			
			Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
			Entity user = txn.get(userKey);
			if( user != null) {
				
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				
				user = Entity.newBuilder(userKey)
						.set("password", DigestUtils.sha512Hex(data.password))
						.set("email", data.email)
						.set("name", data.name)
						.set("phone_number", StringValue.newBuilder(data.phone).setExcludeFromIndexes(true).build())
						.set("address", StringValue.newBuilder(data.address).setExcludeFromIndexes(true).build())
						.set("public", true)
						.set("role", "USER")
						.set("state", false)
						.set("creation_time", Timestamp.now())
						.build();
				txn.add(user);
				LOG.info("User created " + data.username);
				txn.commit();
				return Response.ok("{}").build();
			}
		} finally {
			
			if(txn.isActive()) txn.rollback();
		}

	}
}
