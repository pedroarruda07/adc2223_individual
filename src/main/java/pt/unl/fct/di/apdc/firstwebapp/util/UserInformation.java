package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserInformation {

	public String username;
	public String email;
	public String name;
	public String phone;
	public String address;
	public String role;
	public boolean privacy;
	public boolean state;

	public UserInformation() {}
	
	
	public UserInformation(String username, String email, String name, String phone, String address, String role, boolean privacy, boolean state) {
		
		this.username = username;
		this.email = email;
		this.name = name;
		this.phone = phone;
		this.address = address;
		this.role = role;
		this.privacy = privacy;
		this.state = state;
	}
}
