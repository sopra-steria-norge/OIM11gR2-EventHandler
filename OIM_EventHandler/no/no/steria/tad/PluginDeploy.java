package no.steria.tad;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;

import javax.security.auth.login.LoginException;

import oracle.iam.platform.OIMClient;
import oracle.iam.platformservice.api.PlatformService;

public class PluginDeploy {
	public static OIMClient client;
	private static String OIMUserName = "xelsysadm";
	private static String OIMPassword = "Steria2012";
	private static String OIMURL = "t3://192.168.137.100:14000";
	private static String OIMInitialContextFactory = "weblogic.jndi.WLInitialContextFactory";

	public static void loginWithCustomEnv() throws LoginException {
		System.setProperty("java.security.auth.login.config", "file:authwl.conf");
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, OIMInitialContextFactory);
		env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, OIMURL);
		client = new OIMClient(env);
		client.login(OIMUserName, OIMPassword.toCharArray());
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			System.err.println("java PluginDeploy register <Plugin.zip>");
			System.err.println("java PluginDeploy unregister <Plugin.zip> <PluginClassName> <version>");			
			System.exit(1);
		}
		loginWithCustomEnv();
		PlatformService service = client.getService(PlatformService.class);
		if (args[0].equals("register")) {
			String fileName = args[1];
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
			String className = args[1];
			String version = args[2];
			service.unRegisterPlugin(className,version);
		}
	}
}
