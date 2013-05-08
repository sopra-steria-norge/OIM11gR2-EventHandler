package no.steria.tad;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.platform.kernel.spi.PreProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;

public class NoChangeUserID implements PreProcessHandler{
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
		EventResult er = new EventResult();
		oracle.iam.identity.usermgmt.vo.User user = (oracle.iam.identity.usermgmt.vo.User)orchestration.getInterEventData().get("CURRENT_USER");
		HashMap<String, ?> m = orchestration.getParameters();
		if (m.containsKey(UserManagerConstants.AttributeName.USER_LOGIN.getId())) {
			Date provisionedDate = user.getProvisioningDate();
			if (provisionedDate != null && provisionedDate.before(new Date())){
				throw new RuntimeException("Ikke lov å endre brukernavn etter at bruker er provisjonert mot AD"+provisionedDate.toGMTString());
				//er.setFailureReason(new RuntimeException("IKKE lov å endre brukernavn etter at bruker er provisjonert mot AD"));
				//er.setVeto(true);
				//orchestration.deleteParameter(UserManagerConstants.AttributeName.USER_LOGIN.getId());
				//er.addWarning("Ikke lov å endre brukernavn etter at bruker er provisjonert mot AD");
				//throw new RuntimeException("Ikke lov å endre brukernavn etter at bruker er provisjonert mot AD");
				//er.setFailureReason(new Exception("Ikke lov å endre brukernavn etter at bruker er provisjonert mot AD"));
				//er.setVeto(true);
			}
		}
		return er;
	}
	
	@Override
	public BulkEventResult execute(long arg0, long arg1, BulkOrchestration arg2) {
		return null;
	}
}