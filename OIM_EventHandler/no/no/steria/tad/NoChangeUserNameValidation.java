package no.steria.tad;

import java.io.Serializable;
import java.util.HashMap;

import oracle.iam.platform.context.ContextAware;
import oracle.iam.platform.kernel.ValidationException;
import oracle.iam.platform.kernel.ValidationFailedException;
import oracle.iam.platform.kernel.spi.ValidationHandler;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.Orchestration;

public class NoChangeUserNameValidation implements ValidationHandler {

	public static void main(String args[]) {
		System.out.println("KÅKÅ".replace('Å', 'A'));
	}
	@Override
	public void initialize(HashMap<String, String> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void validate(long arg0, long arg1, Orchestration 
orchestration)
			throws ValidationException, 
ValidationFailedException {
	    HashMap<String, Serializable> parameters = 
orchestration.getParameters();
	    String username = (parameters.get("usr_name") instanceof 
ContextAware)
	      ? (String) ((ContextAware) 
parameters.get("usr_name")).getObjectValue()
	      : (String) parameters.get("usr_name");
	    if (username.contains("$")) {
	      throw new ValidationFailedException();
	    }
	}

	@Override
	public void validate(long arg0, long arg1, BulkOrchestration 
arg2)
			throws ValidationException, 
ValidationFailedException {
		// TODO Auto-generated method stub

	}

}
