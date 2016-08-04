package com.skogul.spring.acl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skogul.spring.acl.domain.jpa.AclSid;

/**
 * Spring Data JPA repository for the AclSid entity.
 */
public interface AclSidRepository extends JpaRepository<AclSid, Long>{
	
	AclSid findOneBySid(String sidString);
}
