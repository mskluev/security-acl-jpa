package com.skogul.spring.acl.advice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CreateACL {

	Class<?> type();
	
	String[] sids();
	
	String[] permissions();
	
}
