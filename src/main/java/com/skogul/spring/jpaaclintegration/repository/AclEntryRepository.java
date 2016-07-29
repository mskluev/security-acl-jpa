package com.skogul.spring.jpaaclintegration.repository;

import com.skogul.spring.jpaaclintegration.domain.AclEntry;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the AclEntry entity.
 */
public interface AclEntryRepository extends JpaRepository<AclEntry, Long>{
	
}
