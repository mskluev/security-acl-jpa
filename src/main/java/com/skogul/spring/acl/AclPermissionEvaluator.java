package com.skogul.spring.acl;

import java.io.Serializable;
import java.util.List;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import com.skogul.spring.acl.domain.ObjectIdentityImpl;
import com.skogul.spring.acl.domain.PermissionResolverImpl;
import com.skogul.spring.acl.domain.SidRetrieverImpl;
import com.skogul.spring.acl.model.ObjectIdentity;
import com.skogul.spring.acl.model.PermissionResolver;
import com.skogul.spring.acl.model.SecurityIdentity;
import com.skogul.spring.acl.model.SidRetriever;

/**
 * Strategy used in expression evaluation to determine whether a user has a
 * permission or permissions for a given domain object.
 * 
 * External Dependencies:
 * - Spring Security
 * 
 * @author mskluev
 *
 */
public class AclPermissionEvaluator implements PermissionEvaluator {

	protected AclService aclService;

	protected SidRetriever sidRetriever = new SidRetrieverImpl();
	protected PermissionResolver permResolver = new PermissionResolverImpl();

	public AclPermissionEvaluator(AclService aclService) {
		Assert.notNull(aclService, "AclService required!");
		this.aclService = aclService;
	}

	@Override
	public boolean hasPermission(Authentication authentication,
			Object targetDomainObject, Object permission) {
		// Verify object exists, 403 if not?
		if (targetDomainObject == null) {
			return false;
		}

		// Extract the targetDomainObject id and class
		ObjectIdentity objId = new ObjectIdentityImpl(targetDomainObject);
		// Pass on to the other method
		return hasPermission(authentication, objId.getIdentifier(),
				objId.getType(), permission);
	}

	@Override
	public boolean hasPermission(Authentication authentication,
			Serializable targetId, String targetType, Object permission) {

		// Get all Sids for the authentication
		List<SecurityIdentity> sids = sidRetriever.getSids(authentication);
		// Turn the permission Object in to something more usable
		Integer permInt = permResolver.resolvePermission(permission);
		
		// Create ObjectIdentity
		Assert.isInstanceOf(Long.class, targetId, "targetId must be a Long");
		ObjectIdentity objId = new ObjectIdentityImpl(targetType, (Long)targetId);
		
		return aclService.hasPermission(objId, sids, permInt);
	}

	// -----------------------------------------------
	// Replaceable Components
	// -----------------------------------------------

	public void setSidRetriever(SidRetriever sidRetriever) {
		this.sidRetriever = sidRetriever;
	}

	public void setPermResolver(PermissionResolver permResolver) {
		this.permResolver = permResolver;
	}

}
