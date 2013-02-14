package no.steria.tad;

import java.util.Locale;
import java.util.Map;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Operations.tcUserOperationsIntf;

import oracle.iam.identity.exception.UserNameGenerationException;
import oracle.iam.identity.usermgmt.api.UserNamePolicy;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;

public class TadUsernamePolicy implements UserNamePolicy {

	@Override
	public String getDescription(Locale arg0) {
		return "TadUsernamePolicy";
	}

	@Override
	public String getUserNameFromPolicy(Map<String, String> hashMap)
			throws UserNameGenerationException {
		
		String firstName = hashMap.get("First Name");	
		String lastName = hashMap.get("Last Name");	

		StringBuffer sb = new StringBuffer();
		if (firstName != null) { 
			int length = firstName.length();
			if (length>2) 
				length = 2;
			sb.append(firstName.substring(0, length));
		}		
		if (lastName != null) { 
			int length = lastName.length();
			if (length>2) 
				length = 2;
			sb.append(lastName.substring(0, length));
		}
		tcLookupOperationsIntf lookupTypeService =  Platform.getService(tcLookupOperationsIntf.class);
		try {
			Thor.API.tcResultSet rs = lookupTypeService.getLookupValues("Lookup.TAD.ImproperUserNames");
			int rowCount = rs.getTotalRowCount();
			for (int i = 0; i < rowCount; i++) {
				String code = rs.getStringValueFromColumn(2);
				if (code != null && code.equals(sb.toString().toUpperCase())) {
					return "Generert brukernavn ikke passende. Skriv inn manuelt.";
				}
			} 
		} catch (tcAPIException e) {
			return "Generert brukernavn ikke passende. Skriv inn manuelt.";
		} catch (tcInvalidLookupException e) {
			return "Generert brukernavn ikke passende. Skriv inn manuelt.";
		} catch (tcColumnNotFoundException e) {
			return "Generert brukernavn ikke passende. Skriv inn manuelt.";
		}
		return sb.toString();
	}
/*
	
*/	
	@Override
	public boolean isUserNameValid(String arg0, Map<String, String> arg1) {
		//return false;
		//OIMClient oimClient = new OIMClient();
		//oimClient.getService(serviceClass)
		return true;
		/*
		OIMCLient
		List<String> userLogins = new ArrayList<String>();
		UserManager usrService = oimClient.getService(UserManager.class);
		Set<String> retAttrs = new HashSet<String>();
		retAttrs.add(UserManagerConstants.AttributeName.USER_LOGIN.getId());

		SearchCriteria criteria = new SearchCriteria("User ID", UserID2Search, SearchCriteria.Operator.EQUAL);		
		return false;
		*/
	}

}
