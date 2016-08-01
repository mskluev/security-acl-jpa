package com.skogul.spring.jpaaclintegration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skogul.spring.jpaaclintegration.domain.AclClass;

/**
 * Spring Data JPA repository for the AclClass entity.
 */
public interface AclClassRepository extends JpaRepository<AclClass, Long>{
	
}
