package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAttributesData{

	public String userToChange;
	public String username;
	public String email;
	public String name;
	public String phone;
	public String address;
	public boolean privacy;
	public String role;
	public boolean state;
	public String tokenID;
	public long tokenExp;
	
	public ChangeAttributesData() {}
	
	public ChangeAttributesData(String userToChange, String username, String email, String name, String phone, String address, boolean privacy, 
			String role, boolean state, String tokenID, long tokenExp) {
		
		this.userToChange = userToChange;
		this.username = username;
		this.email = email;
		this.name = name;
		this.phone = phone;
		this.address = address;
		this.privacy = privacy;
		this.role = role;
		this.state = state;
		this.tokenID = tokenID;
		this.tokenExp = tokenExp;
	}
	
}