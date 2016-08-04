package com.skogul.spring.acl.model;

public interface SecurityIdentity {

	String getSidString();
	boolean isPrincipal();
	
}
