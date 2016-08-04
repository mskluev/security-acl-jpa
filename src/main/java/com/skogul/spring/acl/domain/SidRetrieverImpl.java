package com.skogul.spring.acl.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import com.skogul.spring.acl.model.SecurityIdentity;
import com.skogul.spring.acl.model.SidContainer;
import com.skogul.spring.acl.model.SidRetriever;

public class SidRetrieverImpl implements SidRetriever {

	private RoleHierarchy roleHierarchy = new NullRoleHierarchy();

	public SidRetrieverImpl() {
	}

	public SidRetrieverImpl(RoleHierarchy roleHierarchy) {
		Assert.notNull(roleHierarchy, "RoleHierarchy must not be null");
		this.roleHierarchy = roleHierarchy;
	}

	public List<SecurityIdentity> getSids(Authentication authentication) {
		Assert.notNull(authentication, "Authentication required");
		Assert.notNull(authentication.getPrincipal(), "Principal required");

		// Check to see if either the Authentication or
		// Authentication.getPrincipal() implement SidContainer
		if (authentication instanceof SidContainer) {
			return ((SidContainer) authentication).getSids();
		} else if (authentication.getPrincipal() instanceof SidContainer) {
			return ((SidContainer) authentication.getPrincipal()).getSids();
		}

		// If not, just default to username + authorities
		Collection<? extends GrantedAuthority> authorities = roleHierarchy
				.getReachableGrantedAuthorities(authentication.getAuthorities());
		List<SecurityIdentity> sids = new ArrayList<SecurityIdentity>(
				authorities.size() + 1);

		// Get the username
		String username = null;
		if (authentication.getPrincipal() instanceof UserDetails) {
			username = ((UserDetails) authentication.getPrincipal())
					.getUsername();
		} else {
			username = authentication.getPrincipal().toString();
		}
		sids.add(new SecurityIdentityImpl(username, true));

		// Get the authorities
		for (GrantedAuthority authority : authorities) {
			sids.add(new SecurityIdentityImpl(authority.getAuthority(), false));
		}

		return sids;
	}

}
