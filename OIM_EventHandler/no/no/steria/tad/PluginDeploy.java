package no.steria.tad;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;

import javax.security.auth.login.LoginException;

import oracle.iam.platform.OIMClient;
import oracle.iam.platformservice.api.PlatformService;

public class PluginDeploy {
	public static OIMClient client;
	private static String OIMInitialContextFactory = "weblogic.jndi.WLInitialContextFactory";

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
		if (args.length < 5) {
			System.err.println("java PluginDeploy register <OIM_SERVER> <Plugin.zip> <username> <password>");
			System.err.println("java PluginDeploy unregister <OIM_SERVER> <PluginClassName> <version>  <username> <password>");			
			System.exit(1);
		}
		String username  = args[3];
		String password = args[4];
		if (args.length == 6) {
			username = password;
			password = args[5];
		}
		String OIMSERVER = args[1];

		loginWithCustomEnv("t3://"+OIMSERVER+":14000",username,password);
		PlatformService service = client.getService(PlatformService.class);
		if (args[0].equals("register")) {
			String fileName = args[2];
			File zipFile = new File(fileName);
			FileInputStream fis = new FileInputStream(zipFile);
			int size = (int) zipFile.length();
			byte[] b = new byte[size];
			int bytesRead = fis.read(b, 0, size);			
	
			while (bytesRead < size) {
				bytesRead += fis.read(b, bytesRead, size - bytesRead);
			}
			fis.close();
			service.registerPlugin(b);
		}
		else {
			String className = args[2];
			String version = args[3];
			System.out.println(className+':'+version);
			//System.exit(0);
			service.unRegisterPlugin(className,version);
			System.out.println(className+':'+version+":Uninstalled");
		}
	}
}
