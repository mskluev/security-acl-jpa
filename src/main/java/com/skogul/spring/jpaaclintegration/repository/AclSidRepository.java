package com.skogul.spring.jpaaclintegration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skogul.spring.jpaaclintegration.domain.AclSid;

/**
 * Spring Data JPA repository for the AclSid entity.
 */
public interface AclSidRepository extends JpaRepository<AclSid, Long>{
	
}
