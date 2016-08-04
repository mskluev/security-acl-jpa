package com.skogul.spring.acl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.skogul.spring.acl.domain.jpa.AclObjectIdentity;

/**
 * Spring Data JPA repository for the AclObjectIdentity entity.
 */
public interface AclObjectIdentityRepository extends
		JpaRepository<AclObjectIdentity, Long>,
		QueryDslPredicateExecutor<AclObjectIdentity> {

	AclObjectIdentity findOneByObjectIdIdentityAndObjectIdClassClassName(
			Long id, String clazz);

}
