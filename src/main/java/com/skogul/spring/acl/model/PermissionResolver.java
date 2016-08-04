package com.skogul.spring.acl.model;


public interface PermissionResolver {

	Integer resolvePermission(Object permission);
}
