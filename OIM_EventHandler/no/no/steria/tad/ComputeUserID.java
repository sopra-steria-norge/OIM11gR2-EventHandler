package no.steria.tad;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.Platform;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.kernel.spi.PreProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.platform.utils.crypto.CryptoUtil;
import Thor.API.Operations.tcLookupOperationsIntf;

public class ComputeUserID implements PreProcessHandler{
	public static final String AIX_UID = "usr_aix_uid";
	public static final String AIX_PWD = "usr_aix_passwd";
	public static final String UID_USER = "GENERERT BRUKERNAVN IKKE PASSENDE. SKRIV INN MANUELT.";
	//Password characteristics
	int noOfCAPSAlpha = 1;
	int noOfDigits = 1;
	int noOfSplChars = 0;
	int minLen = 8;
	int maxLen = 8;

	@Override
	public void initialize(HashMap<String, String> arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean cancel(long arg0, long arg1,
			AbstractGenericOrchestration arg2) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void compensate(long arg0, long arg1,
			AbstractGenericOrchestration arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public EventResult execute(long processId, long eventId, Orchestration orchestration) {
		try {
			HashMap<String, ?> m = orchestration.getParameters();
			boolean passwordSubmitted = m.containsKey(AIX_PWD);
			boolean aix_uid_submitted = m.containsKey(AIX_UID);
			if (!aix_uid_submitted) {
				Long organisation = (Long)m.get("act_key");
				tcLookupOperationsIntf lookupTypeService =  Platform.getService(tcLookupOperationsIntf.class);
				String region = lookupTypeService.getDecodedValueForEncodedValue("Lookup.TAD.AIX_UID_EnabledOrg", organisation.toString());
				if (region != null && !region.isEmpty()) {
					Set<String> attrNames = null;
					UserManager umgr = Platform.getService(UserManager.class);
					SearchCriteria criteria = new SearchCriteria(UserManagerConstants.AttributeName.USER_LOGIN.getId(), UID_USER, SearchCriteria.Operator.EQUAL);
					attrNames = new HashSet<String>();
					List<User> users = null;
					users = umgr.search(criteria, attrNames, null);
					if (users != null && !users.isEmpty() && users.size() == 1) {
						User user = users.get(0);
						HashMap<String, Object> attrMap = new HashMap<String, Object>();
					    try {
							int aix_uid = 7777;
							try {
								aix_uid = Integer.parseInt(user.getAttribute(AIX_UID).toString())+1;
							}
							catch(Throwable t) {
								aix_uid = 8888;
							}
					    	attrMap.put(AIX_UID, ""+aix_uid);
					    	User modifyUser = new User(user.getId(),attrMap);
					    	umgr.modify(UserManagerConstants.AttributeName.USER_LOGIN.getId(), user.getLogin(), modifyUser);
					    	orchestration.addParameter(AIX_UID, ""+aix_uid);
						}
						catch(Throwable t) {
							throw new RuntimeException(t.getMessage());
					    	//orchestration.addParameter(UserManagerConstants.AttributeName.MIDDLENAME.getId(), t.getMessage());
						}
					}
				}
			}
			if (!passwordSubmitted) {
				char pwdArray[] = RandomPasswordGenerator.generatePswd(minLen, maxLen,
						noOfCAPSAlpha, noOfDigits, noOfSplChars);
				String encryptedPassword = CryptoUtil.getEncryptedPassword(pwdArray, null);
				orchestration.addParameter(AIX_PWD, encryptedPassword);
				//orchestration.addParameter(UserManagerConstants.AttributeName.PASSWORD.getId(), encryptedPassword);
			}
		} 
		catch (Throwable e) {
			throw new RuntimeException(e.getMessage());
			//orchestration.addParameter(UserManagerConstants.AttributeName.MIDDLENAME.getId(),e.getMessage());
			//e.printStackTrace();
		}
		return new EventResult();
	}
	@Override
	public BulkEventResult execute(long arg0, long arg1, BulkOrchestration arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	private String getParameterValue(HashMap<String,Serializable> parameters,String key) {
		String value = (String) ((parameters.get(key) instanceof ContextAware) ? ((ContextAware)parameters.get(key)).getObjectValue() : (String) parameters.get(key));
		return value;
	}
	*/
}