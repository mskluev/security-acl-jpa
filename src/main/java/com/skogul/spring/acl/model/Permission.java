package com.skogul.spring.acl.model;

/**
 * An atomic, bitmask-based permission.
 * 
 * For example:
 * 0 = No permission
 * 1 = Read
 * 2 = Write
 * 4 = Something else
 * ...
 * 2^31 = last available bit
 * 
 * This scheme allows bitwise operations for permission checks and compound 
 * permissions (3 = Read+Write). 
 * 
 * @author mskluev
 *
 */
public interface Permission {

	/**
	 * Bitmask representing the permission.
	 */
	int getMask();
	
	/**
	 * Human-readable representation of this permission for error messages.
	 */
	String getName();
	
}
