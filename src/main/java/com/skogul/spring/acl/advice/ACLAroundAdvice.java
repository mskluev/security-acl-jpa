package com.skogul.spring.acl.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.skogul.spring.acl.AclService;
import com.skogul.spring.acl.domain.ObjectIdentityImpl;
import com.skogul.spring.acl.domain.SecurityIdentityImpl;
import com.skogul.spring.acl.domain.jpa.AclObjectIdentity;
import com.skogul.spring.acl.model.Identifiable;
import com.skogul.spring.acl.model.ObjectIdentity;
import com.skogul.spring.acl.model.PermissionResolver;

@Aspect
@Component
public class ACLAroundAdvice {

	@Autowired
	protected AclService aclService;
	@Autowired
	protected PermissionResolver permResolver;

	@Around("@annotation(com.skogul.spring.acl.advice.CreateACL) && @annotation(createACL)")
	public Object create(ProceedingJoinPoint pjp, CreateACL createAcl)
			throws Throwable {

		Object arg0 = pjp.getArgs()[0];
		if (!(arg0 instanceof Identifiable))
			throw new IllegalStateException(
					"Expecting arg0 to be instance of Identifiable");

		// get the entity and check if the id is present, if yes, no op
		Identifiable entity = (Identifiable) arg0;

		if (entity.getId() != null)
			return pjp.proceed();

		// else, get the return value, get id, and create
		Object retVal = pjp.proceed();

		if (!(retVal instanceof Identifiable))
			throw new IllegalStateException(
					"Expecting instance of Identifiable as return value");

		entity = (Identifiable) retVal;

		ObjectIdentity objId = new ObjectIdentityImpl(createAcl.type(),
				entity.getId());

		// Resolve permissions
		Integer p = permResolver.resolvePermission(createAcl.permissions());

		// Create AclObjectIdentity, domain object just got an id so it should
		// not exist yet
		aclService.createACL(objId);

		for (String sid : createAcl.sids()) {
			aclService.addPermission(objId,
					new SecurityIdentityImpl(sid, false), p);
		}

		return retVal;
	}

	@AfterReturning(
			pointcut = "@annotation(com.skogul.spring.acl.advice.ACLAdd) && @annotation(ACLAdd)",
			returning = "result")
	public void add(ACLAdd aclAdd, Object result) throws Throwable {

		if (!(result instanceof Identifiable))
			throw new IllegalStateException(
					"Expecting instance of Identifiable as return value");

		Identifiable entity = (Identifiable) result;
		String type = result.getClass().getName();

		ObjectIdentity objId = new ObjectIdentityImpl(type, entity.getId());

		// Resolve permissions
		Integer p = permResolver.resolvePermission(aclAdd.permissions());

		// Find or Create AclObjectIdentity
		AclObjectIdentity acl = aclService.findACL(objId);
		if (acl == null) {
			acl = aclService.createACL(objId);
		}

		// Add Permissions
		for (String sid : aclAdd.sids()) {
			aclService.addPermission(acl, new SecurityIdentityImpl(sid, false),
					p);
		}
	}

	@AfterReturning(
			pointcut = "@annotation(com.skogul.spring.acl.advice.ACLRemove) && @annotation(ACLRemove)",
			returning = "result")
	public void remove(ACLRemove aclAdd, Object result) throws Throwable {

		if (!(result instanceof Identifiable))
			throw new IllegalStateException(
					"Expecting instance of Identifiable as return value");

		Identifiable entity = (Identifiable) result;
		String type = result.getClass().getName();

		ObjectIdentity objId = new ObjectIdentityImpl(type, entity.getId());

		// Resolve permissions
		Integer p = permResolver.resolvePermission(aclAdd.permissions());

		// Find AclObjectIdentity
		AclObjectIdentity acl = aclService.findACL(objId);
		if (acl == null) {
			// Nothing to do
			return;
		}

		// Remove Permissions
		for (String sid : aclAdd.sids()) {
			aclService.removePermission(objId, new SecurityIdentityImpl(sid,
					false), p);
		}
	}
}
