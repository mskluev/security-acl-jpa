package com.skogul.spring.acl.exceptions;

public class IdentityUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IdentityUnavailableException(String msg) {
		super(msg);
	}

	public IdentityUnavailableException(String msg, Throwable t) {
		super(msg, t);
	}
}
