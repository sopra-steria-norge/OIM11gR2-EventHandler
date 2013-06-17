package no.steria.tad;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import Thor.API.Operations.tcLookupOperationsIntf;

import oracle.iam.identity.exception.NoSuchUserException;
import oracle.iam.identity.exception.SearchKeyNotUniqueException;
import oracle.iam.identity.exception.UserModifyException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.exception.ValidationFailedException;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.Platform;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.context.ContextManager;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.kernel.spi.PreProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.ApplicationInstance;

public class RegisterPWDChangeUser implements PreProcessHandler{
	public static final String PWD_CHG_FIELD = "tad_requester";
	@Override
	public void initialize(HashMap<String, String> arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public EventResult execute(long arg0, long arg1, Orchestration orchestration) {
		Iterator i = orchestration.getParameters().keySet().iterator();
		oracle.iam.identity.usermgmt.vo.User user = (oracle.iam.identity.usermgmt.vo.User)orchestration.getInterEventData().get("CURRENT_USER");
		UserManager umgr = Platform.getService(UserManager.class);
		try {
			HashMap<String, Object> attrMap = new HashMap<String, Object>();
		    //attrMap.put(PWD_CHG_FIELD, ContextManager.getOIMUser());
		    attrMap.put(UserManagerConstants.AttributeName.MIDDLENAME.getId(), ContextManager.getOIMUser());
			User modifyUser = new User(user.getId(),attrMap);
	    	umgr.modify(UserManagerConstants.AttributeName.USER_LOGIN.getId(), user.getLogin(), modifyUser);
		} catch (ValidationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UserModifyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SearchKeyNotUniqueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*
		List<User> users = null;
		Set<String> attrNames = null;

		SearchCriteria criteria = null;
		criteria = new SearchCriteria("usr_key", orchestration.getTarget().getEntityId(), SearchCriteria.Operator.EQUAL);
		try {
			users = umgr.search(criteria, attrNames, null);
			System.err.println("_*_"+orchestration.getTarget().getEntityId());
			if (users != null && !users.isEmpty()) {
				for (User user : users) {
					System.err.println("_"+orchestration.getTarget().getEntityId());
					user.setAttribute("usr_udf_tad_requester", ContextManager.getOIMUser());
					umgr.modify(user);
				}
			}
		} catch (UserSearchException e) {
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			e.printStackTrace();
		} catch (ValidationFailedException e) {
			e.printStackTrace();
		} catch (UserModifyException e) {
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			e.printStackTrace();
		}
		*/
		return new EventResult();
	}

	@Override
	public BulkEventResult execute(long arg0, long arg1, BulkOrchestration orchestration) {
    	//orchestration.addParameter(PWD_CHG_FIELD, ContextManager.getOIMUser());
    	//orchestration.addParameter(UserManagerConstants.AttributeName.MIDDLENAME.toString(), ContextManager.getOIMUser());
		return new BulkEventResult();
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
}