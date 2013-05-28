package no.steria.tad;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.platform.Platform;
import oracle.iam.platform.context.ContextAware;
import oracle.iam.platform.kernel.spi.PreProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.platform.utils.crypto.CryptoUtil;
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
			boolean passwordSubmitted = m.containsKey(UserManagerConstants.AttributeName.PASSWORD.getId());
			boolean aix_uid_submitted = m.containsKey("usr_aix_uid");
			tcLookupOperationsIntf lookupTypeService =  Platform.getService(tcLookupOperationsIntf.class);
			Thor.API.tcResultSet rs = lookupTypeService.getLookupValues("Lookup.TAD.AIX_UID");
			int rowCount = rs.getTotalRowCount();
			if (!aix_uid_submitted && rowCount > 0) {
				rs.goToRow(0);
				String value = rs.getStringValueFromColumn(3);
				String newValue = ""+(Integer.parseInt(value) + 1);
				Map<String,String> lookupValues = new HashMap<String,String>();
				lookupValues.put("Lookup Definition.Lookup Code Information.Decode",newValue);
				lookupTypeService.updateLookupValue("Lookup.TAD.AIX_UID", "COUNTER", lookupValues);
				orchestration.addParameter("usr_aix_uid", value);
			}	
			if (!passwordSubmitted) {
				char pwdArray[] = RandomPasswordGenerator.generatePswd(minLen, maxLen,
	                    noOfCAPSAlpha, noOfDigits, noOfSplChars);
				String encryptedPassword = CryptoUtil.getEncryptedPassword(pwdArray, null);
				orchestration.addParameter("usr_aix_passwd", encryptedPassword);
				orchestration.addParameter(UserManagerConstants.AttributeName.PASSWORD.getId(), encryptedPassword);
			}
		} catch (tcAPIException e) {
			e.printStackTrace();
		} catch (tcInvalidLookupException e) {
			e.printStackTrace();
		} catch (tcColumnNotFoundException e) {
			e.printStackTrace();
		} catch (tcInvalidValueException e) {
			e.printStackTrace();
		} catch (tcInvalidAttributeException e) {
			e.printStackTrace();
		} catch (Throwable e) {
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