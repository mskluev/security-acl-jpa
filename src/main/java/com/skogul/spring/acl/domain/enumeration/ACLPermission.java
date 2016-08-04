package com.skogul.spring.acl.domain.enumeration;

import java.util.Map;

import com.skogul.spring.acl.model.Permission;

public enum ACLPermission implements Permission {

	/*
	 * These permission integers are used in @PreAuthorize annotations so if
	 * values are changed the annotations must be updated
	 */
	READ("READ", "Allows viewing access", 1 << 0), // 1
	WRITE("WRITE", "Allows write access", 1 << 1), // 2
	ADMINISTRATION("ADMINISTRATION", "Allows modifying access permissions of others", 1 << 2);// 4

	private final String displayName;
	private final String description;
	private final int mask;

	ACLPermission(String displayName, String description, int mask) {
		this.displayName = displayName;
		this.description = description;
		this.mask = mask;
	}

	public String getValue() {
		return this.name();
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, Object> getAdditionalFields() {
		return null;
	}

	@Override
	public int getMask() {
		return mask;
	}

	@Override
	public String getName() {
		return this.name();
	}

}
