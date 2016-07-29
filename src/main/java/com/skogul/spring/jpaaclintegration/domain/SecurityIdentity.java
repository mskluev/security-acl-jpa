package com.skogul.spring.jpaaclintegration.domain;

public interface SecurityIdentity {

	String getSidString();
	boolean isPrincipal();
	
}
