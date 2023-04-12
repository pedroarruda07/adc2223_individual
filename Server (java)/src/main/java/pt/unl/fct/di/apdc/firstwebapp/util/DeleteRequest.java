package pt.unl.fct.di.apdc.firstwebapp.util;

public class DeleteRequest {
	
	public String username;
	public String usernameToDelete;
	public String tokenID;
	public long tokenExp;
	
	public DeleteRequest() {}
	
	public DeleteRequest(String username, String usernameToDelete, String tokenID, long tokenExp) {
		
		this.username = username;
		this.usernameToDelete = usernameToDelete;
		this.tokenExp = tokenExp;
		this.tokenID = tokenID;
	}

}
