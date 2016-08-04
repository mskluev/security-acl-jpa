package com.skogul.spring.acl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skogul.spring.acl.domain.jpa.AclClass;

/**
 * Spring Data JPA repository for the AclClass entity.
 */
public interface AclClassRepository extends JpaRepository<AclClass, Long>{
	
	AclClass findOneByClassName(String className);
	
}
