package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePassword {
	
	public String username;
	public String oldPwd;
	public String newPwd;
	public String newPwdConf;
	public String tokenID;
	public long tokenExp;
	
	public ChangePassword() {}

	public ChangePassword (String username, String oldPwd, String newPwd, String newPwdConf, String tokenID, long tokenExp) {
		
		this.username = username;
		this.oldPwd = oldPwd;
		this.newPwd = newPwd;
		this.newPwdConf = newPwdConf;
	}
	
}
