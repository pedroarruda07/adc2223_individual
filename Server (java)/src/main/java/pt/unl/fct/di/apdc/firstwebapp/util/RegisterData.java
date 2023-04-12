package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	
	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String name;
	public String phone;
	public String address;
	
	public RegisterData() {}
	
	public RegisterData(String username, String password, String confirmation, String email, String name, String phone, String address) {
		
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.email = email;
		this.name = name;
		this.phone = phone;
		this.address = address;
	}

	public boolean validRegistration() {
		
		return username != null && password != null && password.equals(confirmation) && email != null && name != null;
	}

}
