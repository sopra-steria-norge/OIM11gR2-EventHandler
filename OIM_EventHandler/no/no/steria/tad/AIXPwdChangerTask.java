package no.steria.tad;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import Thor.API.Operations.tcLookupOperationsIntf;

import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.ApplicationInstance;
import oracle.iam.identity.usermgmt.vo.User;

public class AIXPwdChangerTask extends oracle.iam.scheduler.vo.TaskSupport {
	OIMClient client = null;
	public AIXPwdChangerTask() {
		client = new OIMClient();		
	}

	public AIXPwdChangerTask(Hashtable env,String username,String password) throws Exception {
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
			parameters.put("MINIMUM_PASSWORD_LENGTH",new Long(6));
			parameters.put("MAXIMUM_PASSWORD_LENGTH",new Long(6));
			parameters.put("NUMBER_OF_CAPS",new Long(1));
			parameters.put("NUMBER_OF_NUMBERS",new Long(1));
			parameters.put("NUMBER_OF_SPECIAL_CHARS", new Long(1));
			parameters.put("ApplicationInstance","Linux");
			parameters.put("LOOKUP_EXCLUSION_TABLE", "Lookup.TAD.PWD_RESET_EXCLUSIONS");

			String username  = "xelsysadm";
			String password = "Steria2012";
			String OIMSERVER = "192.168.137.100";
			Hashtable<String, String> env = loginWithCustomEnv("t3://"+OIMSERVER+":14000",username,password);
			new AIXPwdChangerTask(env,username,password).execute(parameters);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void execute(HashMap parameters) throws Exception {
		//OIMClient client = new OIMClient();		
		HashMap<String,Object> exclusions = new HashMap<String, Object>();
		tcLookupOperationsIntf lookupTypeService = client.getService(tcLookupOperationsIntf.class);
		Thor.API.tcResultSet rs = lookupTypeService.getLookupValues((String)parameters.get("LOOKUP_EXCLUSION_TABLE"));
		int rowCount = rs.getTotalRowCount();
		for (int i = 0;i < rowCount;i++) {
			rs.goToRow(i);
			exclusions.put(rs.getStringValueFromColumn(3),rs.getStringValueFromColumn(2));
		}

		UserManager umgr = client.getService(UserManager.class);
		List<User> users = null;
		Set<String> attrNames = null;

		SearchCriteria criteria = null;

		ApplicationInstanceService applicationInstanceService = client.getService(ApplicationInstanceService.class);
		ApplicationInstance applicationInstance = applicationInstanceService.findApplicationInstanceByName(parameters.get("ApplicationInstance").toString());

		ProvisioningService provisioningService = client.getService(ProvisioningService.class);

		//criteria = new SearchCriteria("usr_key", "*", SearchCriteria.Operator.EQUAL);
		criteria = new SearchCriteria(UserManagerConstants.AttributeName.USER_LOGIN.getId(), "*", SearchCriteria.Operator.EQUAL);
		attrNames = new HashSet<String>();
		//attrNames.add("User Login");

		users = umgr.search(criteria, attrNames, parameters);
		if (users != null && !users.isEmpty()) {
			for (User user : users) {
				if (!exclusions.containsKey(user.getLogin())){
					List<Account> l = provisioningService.getUserAccountDetailsInApplicationInstance(user.getId(), applicationInstance.getApplicationInstanceKey());
					if (l != null) {
						Iterator<Account> i = l.iterator();
						Account a = null;
						while (i.hasNext()) {
							a = i.next();
							String accountStatus = a.getAccountStatus();
							if (!accountStatus.equals("Provisioning") && !accountStatus.equals("Revoked") && accountStatus.equals("Disabled")) {
								String OIU_KEY = a.getAccountID();				
								char pwdArray[] = RandomPasswordGenerator.generatePswd(((Long)parameters.get("MINIMUM_PASSWORD_LENGTH")).intValue(), ((Long)parameters.get("MAXIMUM_PASSWORD_LENGTH")).intValue(),
										((Long)parameters.get("NUMBER_OF_CAPS")).intValue(), ((Long)parameters.get("NUMBER_OF_NUMBERS")).intValue(), ((Long)parameters.get("NUMBER_OF_SPECIAL_CHARS")).intValue());
								provisioningService.changeAccountPassword(Long.parseLong(OIU_KEY), pwdArray);
								System.out.println(user.getLogin()+" : "+new String(pwdArray));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public HashMap getAttributes() {
		return null;
	}
	@Override
	public void setAttributes() {
	}
}
