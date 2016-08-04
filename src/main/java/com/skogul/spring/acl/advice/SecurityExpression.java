package com.skogul.spring.acl.advice;

import com.skogul.spring.acl.model.Identifiable;

public class SecurityExpression {

	public boolean isNew(Object entity) {

		if (!(entity instanceof Identifiable))
			throw new IllegalStateException(
					"Expecting entity to be instance of Base");

		// get the entity and check if the id is present, if yes, no op
		Identifiable base = (Identifiable) entity;

		if (base.getId() != null)
			return false;

		return true;
	}
}
