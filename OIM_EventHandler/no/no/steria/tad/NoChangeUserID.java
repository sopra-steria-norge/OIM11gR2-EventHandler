package no.steria.tad;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.authopss.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.kernel.spi.PreProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.exception.ApplicationInstanceNotFoundException;
import oracle.iam.provisioning.exception.GenericAppInstanceServiceException;
import oracle.iam.provisioning.exception.GenericProvisioningException;
import oracle.iam.provisioning.exception.UserNotFoundException;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.ApplicationInstance;
import oracle.iam.platform.Platform;

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
		if (true) {
			if (m.containsKey(UserManagerConstants.AttributeName.USER_LOGIN.getId())) {
				ApplicationInstanceService applicationInstanceService = Platform.getService(ApplicationInstanceService.class);
				ApplicationInstance applicationInstance = null;
				try {
					applicationInstance = applicationInstanceService.findApplicationInstanceByName("Auto Generated Application Instance for Resource: AD User and IT Resource: Active Directory");
					//applicationInstance = applicationInstanceService.findApplicationInstanceByName("ActiveDirectory");
				} catch (ApplicationInstanceNotFoundException e) {
					throw new RuntimeException("Fant ikke Applikasjonsinstans");
				} catch (GenericAppInstanceServiceException e) {
					throw new RuntimeException("Fant ikke Applikasjonsinstans");
				}
				ProvisioningService provisioningService = Platform.getService(ProvisioningService.class);
				List<Account> l = null;
				try {
					l = provisioningService.getUserAccountDetailsInApplicationInstance(user.getId(), applicationInstance.getApplicationInstanceKey());
				} catch (AccessDeniedException e) {
					throw new RuntimeException("Ingen tilgang til Applikasjonsinstans");
				} catch (UserNotFoundException e) {
					throw new RuntimeException("Bruker ikke funnet. : "+user.getId());
				} catch (ApplicationInstanceNotFoundException e) {
					throw new RuntimeException("Fant ikke Applikasjonsinstansen ved brukersøk");
				} catch (GenericProvisioningException e) {
					throw new RuntimeException("GenericProvisioningException");
				}
				if (l != null) {
					Iterator<Account> i = l.iterator();
					Account a = null;
					while (i.hasNext()) {
						a = i.next();
						String accountStatus = a.getAccountStatus();
						if (accountStatus.equals("Provisioned") || accountStatus.equals("Enabled")) {
							throw new RuntimeException("Ikke lov å endre brukernavn etter at bruker er provisjonert mot AD"+accountStatus);
						}
					}
				}
			}
		}
		else {
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
		}
		return er;
	}
	
	@Override
	public BulkEventResult execute(long arg0, long arg1, BulkOrchestration arg2) {
		return null;
	}
}