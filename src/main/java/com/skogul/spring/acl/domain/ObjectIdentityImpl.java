package com.skogul.spring.acl.domain;

import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.skogul.spring.acl.exceptions.IdentityUnavailableException;
import com.skogul.spring.acl.model.ObjectIdentity;

public class ObjectIdentityImpl implements ObjectIdentity {

	private final String type;
	private Long identifier;

	@Override
	public Long getIdentifier() {
		return identifier;
	}

	@Override
	public String getType() {
		return type;
	}

	public ObjectIdentityImpl(String type, Long identifier) {
		Assert.hasText(type, "Type required");
		Assert.notNull(identifier, "identifier required");

		this.identifier = identifier;
		this.type = type;
	}

	/**
	 * Constructor which uses the name of the supplied class as the
	 * <tt>type</tt> property.
	 */
	public ObjectIdentityImpl(Class<?> javaType, Long identifier) {
		Assert.notNull(javaType, "Java Type required");
		Assert.notNull(identifier, "identifier required");
		this.type = javaType.getName();
		this.identifier = identifier;
	}

	/**
	 * Creates the <code>ObjectIdentityImpl</code> based on the passed object
	 * instance. The passed object must provide a <code>getId()</code> method,
	 * otherwise an exception will be thrown.
	 * <p>
	 * The class name of the object passed will be considered the {@link #type},
	 * so if more control is required, a different constructor should be used.
	 *
	 * @param object
	 *            the domain object instance to create an identity for.
	 *
	 * @throws IdentityUnavailableException
	 *             if identity could not be extracted
	 */
	public ObjectIdentityImpl(Object object)
			throws IdentityUnavailableException {
		Assert.notNull(object, "object cannot be null");

		Class<?> typeClass = ClassUtils.getUserClass(object.getClass());
		type = typeClass.getName();

		Object result;

		try {
			Method method = typeClass.getMethod("getId", new Class[] {});
			result = method.invoke(object);
		} catch (Exception e) {
			throw new IdentityUnavailableException(
					"Could not extract identity from object " + object, e);
		}

		Assert.notNull(result, "getId() is required to return a non-null value");
		Assert.isInstanceOf(Long.class, result,
				"Getter must provide a return value of type Long");
		this.identifier = (Long) result;
	}

}
