package com.skogul.spring.acl.domain;

import com.skogul.spring.acl.model.SecurityIdentity;

public class SecurityIdentityImpl implements SecurityIdentity {

	private String sidString;
	private boolean isPrincipal;
	
	public SecurityIdentityImpl(String sidString, boolean isPrincipal) {
		this.sidString = sidString;
		this.isPrincipal = isPrincipal;
	}
	@Override
	public String getSidString() {
		return sidString;
	}

	@Override
	public boolean isPrincipal() {
		return isPrincipal;
	}

}
