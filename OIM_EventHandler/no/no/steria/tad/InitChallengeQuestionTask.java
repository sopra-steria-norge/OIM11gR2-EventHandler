package no.steria.tad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;

public class InitChallengeQuestionTask {
	public static OIMClient client;
	private static String OIMInitialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	public static final String FAILED = "Failed";
	public static final String OK = "Ok";


	private String delimiter = ";";
	private String csvFile = null;

	public static void loginWithCustomEnv(String OIMURL,String username,String password) throws LoginException {
		System.setProperty("java.security.auth.login.config", "file:authwl.conf");
		System.setProperty("APPSERVER_TYPE", "wls");
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, OIMInitialContextFactory);
		env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, OIMURL);
		client = new OIMClient(env);
		client.login(username, password.toCharArray());
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.err.println("java InitChallengeQuestionTask <OIM_SERVER> <CSV-file> <username> <password> [<delimiter>]");
			System.exit(1);
		}
		String username  = args[2];
		String password = args[3];
		String OIMSERVER = args[0];
		loginWithCustomEnv("t3://"+OIMSERVER+":14000",username,password);
		InitChallengeQuestionTask readCSV = new InitChallengeQuestionTask();
		readCSV.csvFile = args[1];
		if (args.length == 5)
			readCSV.delimiter = args[4];
		readCSV.run();
	}
	public void run() throws IOException {
		HashMap<String, Object> parameters = new HashMap<String,Object>();
		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		String line = null;
		SearchCriteria criteria = null;
		UserManager umgr = client.getService(UserManager.class);
		List<User> users = null;
		Set<String> attrNames = null;
		attrNames = new HashSet<String>();
		String userLogin = null;
		String challengeQuestion = null;
		String challengeAnswer = null;
		String lineSplit[] = null;
		while ((line = br.readLine()) != null) {
			try {
				lineSplit = line.split(delimiter);
				userLogin = lineSplit[0];
				challengeQuestion = lineSplit[1];
				challengeAnswer = lineSplit[2];
				criteria = new SearchCriteria(UserManagerConstants.AttributeName.USER_LOGIN.getId(), userLogin, SearchCriteria.Operator.EQUAL);
				users = umgr.search(criteria, attrNames, parameters);
				if (users != null && !users.isEmpty()) {
					for (User user : users) {
			            try {
			                HashMap<String, Object> attrMap = new HashMap<String, Object>();
			                User modifyUser = new User(user.getId(),attrMap);
			                modifyUser.setAttribute("TAD_PQUESTION", challengeQuestion);
			                modifyUser.setAttribute("TAD_PANSWER", challengeAnswer);
			                umgr.modify(UserManagerConstants.AttributeName.USER_LOGIN.getId(), user.getLogin(), modifyUser);
			            }
			            catch(Throwable t) {
			            	
			            }
					}
				}
			}
			catch (Throwable t) {
				System.out.println(FAILED + " - " + t.getMessage());
			}
			finally {
				br.close();
			}
		}
	}
}
