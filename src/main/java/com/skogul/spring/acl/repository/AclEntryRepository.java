package com.skogul.spring.acl.repository;

import java.util.List;

import com.skogul.spring.acl.domain.jpa.AclEntry;
import com.skogul.spring.acl.domain.jpa.AclObjectIdentity;
import com.skogul.spring.acl.domain.jpa.AclSid;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the AclEntry entity.
 */
public interface AclEntryRepository extends JpaRepository<AclEntry, Long> {

	AclEntry findOneByAclObjectIdentityAndSid(AclObjectIdentity objId,
			AclSid sid);

	@Query("SELECT e "
			+ "FROM AclEntry e "
			+ "JOIN e.aclObjectIdentity o "
			+ "JOIN o.objectIdClass c "
			+ "JOIN e.sid s "
			+ "WHERE o.objectIdIdentity = :objId "
			+ "AND c.className = :class "
			+ "AND s.sid = :sidString")
	AclEntry findOneByObjectIdAndClassNameAndSidString(@Param("objId") Long id,
			@Param("class") String clazz, @Param("sidString") String sidString);
	
	@Query("SELECT e "
			+ "FROM AclEntry e "
			+ "JOIN e.aclObjectIdentity o "
			+ "JOIN o.objectIdClass c "
			+ "JOIN e.sid s "
			+ "WHERE o.objectIdIdentity = :objId "
			+ "AND c.className = :class "
			+ "AND s.sid IN :sidStrings")
	List<AclEntry> findEntriesForSidsOnObject(@Param("objId") Long id,
			@Param("class") String clazz, @Param("sidStrings") List<String> sidStrings);
}
