package com.skogul.spring.acl.model;

import java.util.List;

public interface SidContainer {

	List<SecurityIdentity> getSids();
	
}
