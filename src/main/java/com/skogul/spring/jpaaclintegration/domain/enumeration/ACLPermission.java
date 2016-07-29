package com.skogul.spring.jpaaclintegration.domain.enumeration;

import java.util.Map;

import org.springframework.security.acls.domain.AclFormattingUtils;
import org.springframework.security.acls.model.Permission;

public enum ACLPermission implements Permission {

	/*
	 * These permission integers are used in @PreAuthorize annotations so if
	 * values are changed the annotations must be updated
	 */
	READ("READ", "Allows viewing access", 1 << 0), // 1
	WRITE("WRITE", "Allows write access", 1 << 1), // 2
	ADMINISTRATION("ADMINISTRATION", "Allows viewing access", 1 << 4);// 16
	/*
	 * To go above a mask value of 16 we will need to do additional work
	 * detailed here:
	 * 
	 * http://www.harezmi.com.tr/blogpost433-Adding-New-Permission-Types-to-Spring-Security-ACL
	 * 
	 * This is because org.springframework.security.acls.domain.BasePermission
	 * only goes to 16.
	 */

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
	public String getPattern() {
		return AclFormattingUtils.printBinary(mask, 'X');
	}

	public static ACLPermission fromMask(int value) {
		for (ACLPermission obj : ACLPermission.values()) {
			if (obj.getMask() == value) {
				return obj;
			}
		}
		return null;
	}

}
