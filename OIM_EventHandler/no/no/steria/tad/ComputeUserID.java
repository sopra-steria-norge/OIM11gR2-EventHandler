package no.steria.tad;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.Platform;
import oracle.iam.platform.context.ContextAware;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.kernel.spi.PreProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.platform.utils.crypto.CryptoUtil;
import oracle.iam.provisioning.vo.Account;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidAttributeException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Exceptions.tcInvalidValueException;
import Thor.API.Operations.tcLookupOperationsIntf;

public class ComputeUserID implements PreProcessHandler{
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
	@SuppressWarnings("deprecation")
	@Override
	public EventResult execute(long processId, long eventId, Orchestration orchestration) {
		try {
			HashMap<String, ?> m = orchestration.getParameters();
			boolean passwordSubmitted = m.containsKey("usr_aix_passwd");
			boolean aix_uid_submitted = m.containsKey("usr_aix_uid");
			if (!aix_uid_submitted) {
				Set<String> attrNames = null;
				UserManager umgr = Platform.getService(UserManager.class);
				SearchCriteria criteria = new SearchCriteria(UserManagerConstants.AttributeName.USER_LOGIN.getId(), "GENERERT BRUKERNAVN IKKE PASSENDE. SKRIV INN MANUELT.", SearchCriteria.Operator.EQUAL);
				attrNames = new HashSet<String>();
				List<User> users = null;
				users = umgr.search(criteria, attrNames, null);
				if (users != null && !users.isEmpty() && users.size() == 1) {
					User user = users.get(0);
					int aix_uid = 7777;
					try {
						aix_uid = Integer.parseInt(user.getAttribute("usr_aix_uid").toString())+1;
					}
					catch(Throwable t) {
						aix_uid = 8888;
					}
					user.setAttribute("usr_aix_uid", ""+aix_uid);
					umgr.modify(user);
					orchestration.addParameter("usr_aix_uid", ""+aix_uid);
				}
			}
			if (!passwordSubmitted) {
				char pwdArray[] = RandomPasswordGenerator.generatePswd(minLen, maxLen,
	                    noOfCAPSAlpha, noOfDigits, noOfSplChars);
				String encryptedPassword = CryptoUtil.getEncryptedPassword(pwdArray, null);
				orchestration.addParameter("usr_aix_passwd", encryptedPassword);
				//orchestration.addParameter(UserManagerConstants.AttributeName.PASSWORD.getId(), encryptedPassword);
			}
		} 
		catch (Throwable e) {
			e.printStackTrace();
		}
		return new EventResult();
	}
	@Override
	public BulkEventResult execute(long arg0, long arg1, BulkOrchestration arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getParameterValue(HashMap<String,Serializable> parameters,String key) {
		String value = (String) ((parameters.get(key) instanceof ContextAware) ? ((ContextAware)parameters.get(key)).getObjectValue() : (String) parameters.get(key));
		return value;
	}
}