package com.skogul.spring.acl.model;


public interface ObjectIdentity {

	/**
	 * Obtains the actual identifier. This identifier must not be reused to
	 * represent other domain objects with the same <tt>javaType</tt>.
	 *
	 * <p>
	 * Because ACLs are largely immutable, it is strongly recommended to use a
	 * synthetic identifier (such as a database sequence number for the primary
	 * key). Do not use an identifier with business meaning, as that business
	 * meaning may change in the future such change will cascade to the ACL
	 * subsystem data.
	 * </p>
	 *
	 * @return the identifier (unique within this <tt>type</tt>; never
	 *         <tt>null</tt>)
	 */
	Long getIdentifier();

	/**
	 * Obtains the "type" metadata for the domain object. This will often be a
	 * Java type name (an interface or a class) &ndash; traditionally it is the
	 * name of the domain object implementation class.
	 *
	 * @return the "type" of the domain object (never <tt>null</tt>).
	 */
	String getType();
}
