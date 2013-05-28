package no.steria.tad;

import java.util.Locale;
import java.util.Map;

import oracle.iam.identity.exception.UserNameGenerationException;
import oracle.iam.identity.usermgmt.api.UserNamePolicy;
import oracle.iam.platform.Platform;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;

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
		String username = sb.toString().toUpperCase();
		tcLookupOperationsIntf lookupTypeService =  Platform.getService(tcLookupOperationsIntf.class);
		try {
			Thor.API.tcResultSet rs = lookupTypeService.getLookupValues("Lookup.TAD.ImproperUserNames");
			int rowCount = rs.getTotalRowCount();
			for (int i = 0; i < rowCount; i++) {
				rs.goToRow(i);
				String code = rs.getStringValueFromColumn(2);
				if (code != null && code.toUpperCase().equals(username)) {
					return "GENERERT BRUKERNAVN IKKE PASSENDE. SKRIV INN MANUELT.";
				}
			} 
		} catch (tcAPIException e) {
			return "GENERERT BRUKERNAVN IKKE PASSENDE. SKRIV INN MANUELT.";
		} catch (tcInvalidLookupException e) {
			return "GENERERT BRUKERNAVN IKKE PASSENDE. SKRIV INN MANUELT.";
		} catch (tcColumnNotFoundException e) {
			return "GENERERT BRUKERNAVN IKKE PASSENDE. SKRIV INN MANUELT.";
		}
		return username.replace('Å', 'A').replace('Ø', 'O').replace('Æ', 'E');
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
