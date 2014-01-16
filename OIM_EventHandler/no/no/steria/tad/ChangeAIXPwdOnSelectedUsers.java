package no.steria.tad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.ApplicationInstance;

public class ChangeAIXPwdOnSelectedUsers extends oracle.iam.scheduler.vo.TaskSupport {
	OIMClient client = null;
	public static final String NOT_FOUND = "Not found";
	public static final String NOT_PROVISIONED = "Not provisioned";
	public static final String FAILED = "Failed";
	public static final String OK = "Ok";
	
	public ChangeAIXPwdOnSelectedUsers() {
		client = new OIMClient();		
	}

	public ChangeAIXPwdOnSelectedUsers(Hashtable env,String username,String password) throws Exception {
		client = new OIMClient(env);
		client.login(username, password.toCharArray());
	}
	
	private static String OIMInitialContextFactory = "weblogic.jndi.WLInitialContextFactory";

	public static Hashtable<String,String> loginWithCustomEnv(String OIMURL,String username,String password) throws LoginException {
		System.setProperty("java.security.auth.login.config", "file:authwl.conf");
		System.setProperty("APPSERVER_TYPE", "wls");
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, OIMInitialContextFactory);
		env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, OIMURL);
		return env;
	}

	public static void main(String args[]) {
		HashMap<String, Object> parameters = new HashMap<String,Object>();
		try {
			parameters.put("Minimum password length",new Long(6));
			parameters.put("Maximum password length",new Long(6));
			parameters.put("Number of caps",new Long(1));
			parameters.put("Number of numbers",new Long(1));
			parameters.put("Number of special chars", new Long(1));
			parameters.put("ApplicationInstance","Linux");
			parameters.put("UserFile", "c:\\log\\fileOfUsers.txt");
			parameters.put("LogFile", "c:\\log\\Logfile.log");

			String username  = "xelsysadm";
			String password = "Steria2012";
			String OIMSERVER = "192.168.137.100";
			Hashtable<String, String> env = loginWithCustomEnv("t3://"+OIMSERVER+":14000",username,password);
			new ChangeAIXPwdOnSelectedUsers(env,username,password).execute(parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void execute(HashMap parameters) throws Exception {
		//OIMClient client = new OIMClient();		
		String status = OK;
		String filename = (String)parameters.get("UserFile");
		String logFilename = (String)parameters.get("LogFile");
		BufferedReader br = new BufferedReader(new FileReader(filename));
		FileWriter logWriter = new FileWriter(logFilename);
		String line = null;
		ApplicationInstanceService applicationInstanceService = client.getService(ApplicationInstanceService.class);
		ApplicationInstance applicationInstance = applicationInstanceService.findApplicationInstanceByName(parameters.get("ApplicationInstance").toString());

		ProvisioningService provisioningService = client.getService(ProvisioningService.class);
		SearchCriteria criteria = null;
		UserManager umgr = client.getService(UserManager.class);
		List<User> users = null;
		Set<String> attrNames = null;
		attrNames = new HashSet<String>();
		while ((line = br.readLine()) != null) {
			status = OK;
			try {
				criteria = new SearchCriteria(UserManagerConstants.AttributeName.USER_LOGIN.getId(), line, SearchCriteria.Operator.EQUAL);
				users = umgr.search(criteria, attrNames, parameters);
				if (users != null && !users.isEmpty()) {
					for (User user : users) {
						List<Account> l = provisioningService.getUserAccountDetailsInApplicationInstance(user.getId(), applicationInstance.getApplicationInstanceKey());
						if (l != null) {
							Iterator<Account> i = l.iterator();
							Account a = null;
							while (i.hasNext()) {
								a = i.next();
								String accountStatus = a.getAccountStatus();
								if (!accountStatus.equals("Provisioning") && !accountStatus.equals("Revoked") && !accountStatus.equals("Disabled")) {
									String OIU_KEY = a.getAccountID();				
									char pwdArray[] = RandomPasswordGenerator.generatePswd(((Long)parameters.get("Minimum password length")).intValue(), ((Long)parameters.get("Maximum password length")).intValue(),
											((Long)parameters.get("Number of caps")).intValue(), ((Long)parameters.get("Number of numbers")).intValue(), ((Long)parameters.get("Number of special chars")).intValue());
									provisioningService.changeAccountPassword(Long.parseLong(OIU_KEY), pwdArray);
									status = OK;
								}
								else {
									status = NOT_PROVISIONED;
								}
							}
						}
						else {
							status = NOT_PROVISIONED;
						}
					}
				}
				else {
					status = NOT_FOUND;
				}
			}
			catch (Throwable t) {
				status = FAILED + " - " + t.getMessage();
			}
			finally {
				try {
					if (status != NOT_PROVISIONED) {
						logWriter.write(line + " - " + status + "\n");
						logWriter.flush();
					}
				}
				catch(Throwable t){
					t.printStackTrace();
				}
				finally {
					if (isStop()) {
						logWriter.write("Task is stopping");
						logWriter.flush();
						logWriter.close();
						return;
					}
				}
			}
		}
		logWriter.flush();
		logWriter.close();
	}

	@Override
	public HashMap getAttributes() {
		return null;
	}
	@Override
	public void setAttributes() {
	}
}
