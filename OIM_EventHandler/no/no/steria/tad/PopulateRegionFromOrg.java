package no.steria.tad;

import java.util.HashMap;

import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.platform.Platform;
import oracle.iam.platform.kernel.spi.PreProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;


public class PopulateRegionFromOrg implements PreProcessHandler{
	@Override
	public void initialize(HashMap<String, String> arg0) {
	}

	@Override
	public boolean cancel(long arg0, long arg1,
			AbstractGenericOrchestration orchestration) {
		return false;
	}
	@Override
	public void compensate(long arg0, long arg1,
			AbstractGenericOrchestration arg2) {
	}
	@Override
	public EventResult execute(long processId, long eventId, Orchestration orchestration) {
		try {
			tcLookupOperationsIntf lookupTypeService =  Platform.getService(tcLookupOperationsIntf.class);
			String orgName = UserManagerConstants.AttributeName.LDAP_ORGANIZATION.getId();
			HashMap<String, ?> m = orchestration.getParameters();
			if (m.containsKey(orgName)) {
				Thor.API.tcResultSet rs = lookupTypeService.getLookupValuesForEncoded("Lookup.TAD.Org.Region",orgName);
				int rowCount = rs.getTotalRowCount();
				if (rowCount > 0) {
					rs.goToRow(0);
					String region = rs.getStringValueFromColumn(3);
					orchestration.addParameter("region", region);
				}
			}
		} catch (tcAPIException e) {
			e.printStackTrace();
		} catch (tcInvalidLookupException e) {
			e.printStackTrace();
		} catch (tcColumnNotFoundException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return new EventResult();
	}
	
	@Override
	public BulkEventResult execute(long arg0, long arg1, BulkOrchestration arg2) {
		return null;
	}
}