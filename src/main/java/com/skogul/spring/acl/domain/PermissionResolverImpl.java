package com.skogul.spring.acl.domain;

import com.skogul.spring.acl.model.Permission;
import com.skogul.spring.acl.model.PermissionResolver;

public class PermissionResolverImpl implements PermissionResolver {

	@Override
	public Integer resolvePermission(Object permission) {
		if (permission instanceof Integer) {
			return (Integer) permission;
		}

		if (permission instanceof Permission) {
			return ((Permission) permission).getMask();
		}

		// TODO if (permission instanceof Permission[]) {}

		// TODO if (permission instanceof String) {}
		
		throw new IllegalArgumentException("Unsupported permission: " + permission);
	}

}
