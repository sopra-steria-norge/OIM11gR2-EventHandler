package no.steria.tad;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import oracle.iam.platform.OIMClient;
import oracle.iam.platform.authopss.api.PolicyConstants.Resources;
import oracle.iam.platform.authopss.exception.AccessDeniedException;
import oracle.iam.platform.authopss.vo.EntityPublication;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platformservice.api.EntityPublicationService;
import oracle.iam.provisioning.api.EntitlementService;
import oracle.iam.provisioning.exception.GenericEntitlementServiceException;
import oracle.iam.provisioning.vo.Entitlement;

public class EntityPublicationInit {
	public static OIMClient client;
	private static String OIMInitialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	private static HashMap<String,Object> configParams = new HashMap<String,Object>();

	private String delimiter = ",";
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
			System.err.println("java EntityPublicationInit <OIM_SERVER> <CSV-file> <username> <password> [<delimiter>]");
			System.exit(1);
		}
		String username  = args[2];
		String password = args[3];
		String OIMSERVER = args[0];

		loginWithCustomEnv("t3://"+OIMSERVER+":14000",username,password);
		EntityPublicationInit readCSV = new EntityPublicationInit();
		readCSV.csvFile = args[1];
		if (args.length == 5)
			readCSV.delimiter = args[4];
		readCSV.run();
	}
	public void run() {
		EntitlementService service = client.getService(EntitlementService.class);
		EntityPublicationService publicationService = client.getService(EntityPublicationService.class);
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(this.csvFile));
			SearchCriteria criteria = null;
			Entitlement e = null;
			while ((line = br.readLine()) != null) {
				String[] entitlement = line.split(this.delimiter);
				criteria = new SearchCriteria(Entitlement.ENTITLEMENT_VALUE, entitlement[0], SearchCriteria.Operator.EQUAL);
				java.util.List<Entitlement> list = service.findEntitlements(criteria, configParams);
				if (!list.isEmpty()) {
					if (list.size() > 1) {
						System.out.println("Warning: More than one entitlement matched your entitlement-value : "+entitlement[0]);
					}
					try {
						e = list.get(0);
						updatePublication(publicationService,e,entitlement[1]);
					}
					catch(Throwable t) {
						System.err.println("Error : Exception updating : "+ entitlement[0]);
						t.printStackTrace();
					}
				}
				else {
					System.out.println(entitlement[0] + " : Not found");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GenericEntitlementServiceException e) {
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			client.logout();
		}
		System.out.println("Done");
	}
	
	private static Map HASHMAP = new HashMap();
	
	private void updatePublication(EntityPublicationService publicationService, Entitlement e, String organisation) {
		System.out.println("UpdatePublication : "+e.getEntitlementKey());
		java.util.List<EntityPublication> publicationList = publicationService.listEntityPublications(Resources.IT_RESOURCE_ENTITLEMENT, ""+e.getEntitlementKey(), HASHMAP);
		publicationService.removeEntityPublications(publicationList);
		List<EntityPublication> l = new ArrayList<EntityPublication>();
		EntityPublication ep = new EntityPublication();
		ep.setEntityIdAsLong(new Long(e.getEntitlementKey()));
		ep.setEntityType("Entitlement");
		ep.setScopeType("Company");
		ep.setRootScopeId(organisation);
		ep.setScopeId(organisation);
		l.add(ep);
		publicationService.addEntityPublications(l);
	}
}
