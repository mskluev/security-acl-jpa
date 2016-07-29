package com.skogul.spring.jpaaclintegration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.skogul.spring.jpaaclintegration.domain.AclObjectIdentity;

/**
 * Spring Data JPA repository for the AclObjectIdentity entity.
 */
public interface AclObjectIdentityRepository extends
		JpaRepository<AclObjectIdentity, Long>,
		QueryDslPredicateExecutor<AclObjectIdentity> {

	AclObjectIdentity findOneByObjectIdIdentityAndObjectIdClassClassName(
			Long id, String clazz);
	
	List<AclObjectIdentity> findByParentObjectIdAndParentObjectObjectIdClassClassName(Long id, String clazz);

	/*@Query("SELECT new nsrp.security.acl.dto.AccessControlRecordDTO(e.id, s.sid, e.mask) "
			+ "FROM AclObjectIdentity o "
			+ "JOIN o.objectIdClass c "
			+ "JOIN o.aclEntries e "
			+ "JOIN e.sid s "
			+ "WHERE o.objectIdIdentity = :objId AND c.className = :class")
	List<AccessControlRecordDTO> findEntriesByObjIdAndClass(@Param("objId") Long id,
			@Param("class") String clazz);*/
}
