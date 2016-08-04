package com.skogul.spring.acl.model;

import java.util.List;

import org.springframework.security.core.Authentication;

/**
 * Strategy interface that provides an ability to determine the SID Strings
 * applicable for an {@link Authentication}.
 * 
 */
public interface SidRetriever {

	List<SecurityIdentity> getSids(Authentication authentication);

}
