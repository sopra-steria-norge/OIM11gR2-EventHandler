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
			tcLookupOperationsIntf lookupTypeService =  Platform.getService(tcLookupOperationsIntf.class);
			HashMap<String,Serializable> parameters = orchestration.getParameters();

			Thor.API.tcResultSet rs = lookupTypeService.getLookupValues("Lookup.TAD.AIX_UID");
			int rowCount = rs.getTotalRowCount();
			if (rowCount > 0) {
				rs.goToRow(0);
				String value = rs.getStringValueFromColumn(3);
				String newValue = ""+(Integer.parseInt(value) + 1);
				Map<String,String> lookupValues = new HashMap<String,String>();
				lookupValues.put("Lookup Definition.Lookup Code Information.Decode",newValue);
				lookupTypeService.updateLookupValue("Lookup.TAD.AIX_UID", "COUNTER", lookupValues);
				orchestration.addParameter("USR_AIX_UID", value);
				char pwdArray[] = RandomPasswordGenerator.generatePswd(minLen, maxLen,
	                    noOfCAPSAlpha, noOfDigits, noOfSplChars);
				orchestration.addParameter("USR_AIX_PASSWD", new String(pwdArray));
				if (getParameterValue(parameters,"Password Generated") == null) {
					return new EventResult();
				}
				String encryptedPassword = CryptoUtil.getEncryptedPassword(pwdArray, null);
				orchestration.addParameter(UserManagerConstants.AttributeName.PASSWORD.getId(), encryptedPassword);
			}	
		} catch (tcAPIException e) {
			orchestration.addParameter("Middle Name", "ABC"+e.isMessage);
		} catch (tcInvalidLookupException e) {
			orchestration.addParameter("Middle Name", "DEF");
		} catch (tcColumnNotFoundException e) {
			orchestration.addParameter("Middle Name", "GHI");
		} catch (tcInvalidValueException e) {
			orchestration.addParameter("Middle Name", "QPR");
		} catch (tcInvalidAttributeException e) {
			orchestration.addParameter("Middle Name", "STU"+e.isMessage+"::"+e.getMessage());
		} catch (Throwable e) {
			orchestration.addParameter("Middle Name", "XYZ"+e.getMessage());
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