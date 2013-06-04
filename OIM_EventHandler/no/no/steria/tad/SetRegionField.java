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

public class SetRegionField implements PreProcessHandler{
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
			boolean organisationChanged = m.containsKey(UserManagerConstants.AttributeName.LDAP_ORGANIZATION);
			if (organisationChanged) {
				String organisation = (String)m.get(UserManagerConstants.AttributeName.LDAP_ORGANIZATION);
				tcLookupOperationsIntf lookupTypeService =  Platform.getService(tcLookupOperationsIntf.class);
				String region = lookupTypeService.getDecodedValueForEncodedValue("Lookup.TAD.ORG_2_REGION", organisation);
				orchestration.addParameter("usr_udf_tad_region", region);
			}
		}catch (tcAPIException e) {
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