package com.skogul.spring.acl.testClasses;

import java.io.Serializable;

public class TestDomainObject implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	public TestDomainObject(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
