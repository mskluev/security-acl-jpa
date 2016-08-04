package com.skogul.spring.acl.exceptions;

public class AlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlreadyExistsException(String msg) {
		super(msg);
	}

	public AlreadyExistsException(String msg, Throwable t) {
		super(msg, t);
	}
}
