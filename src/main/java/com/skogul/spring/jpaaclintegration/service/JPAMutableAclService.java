package com.skogul.spring.jpaaclintegration.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.ChildrenExistException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.skogul.spring.jpaaclintegration.domain.AclClass;
import com.skogul.spring.jpaaclintegration.domain.AclObjectIdentity;
import com.skogul.spring.jpaaclintegration.domain.AclSid;

@Transactional
public class JPAMutableAclService extends JPAAclService implements
		MutableAclService {

	protected static final Log log = LogFactory
			.getLog(JPAMutableAclService.class);

	public JPAMutableAclService(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public MutableAcl createAcl(ObjectIdentity objectIdentity)
			throws AlreadyExistsException {
		Assert.notNull(objectIdentity, "Object Identity required");

		// Check this object identity hasn't already been persisted
		if (retrieveObjectIdentityPrimaryKey(objectIdentity) != null) {
			throw new AlreadyExistsException("Object identity '"
					+ objectIdentity + "' already exists");
		}

		// Need to retrieve the current principal, in order to know who "owns"
		// this ACL
		// (can be changed later on)
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		PrincipalSid sid = new PrincipalSid(auth);

		// Create the acl_object_identity row
		createObjectIdentity(objectIdentity, sid);

		// Retrieve the ACL via superclass (ensures cache registration, proper
		// retrieval
		// etc)
		Acl acl = readAclById(objectIdentity);
		Assert.isInstanceOf(MutableAcl.class, acl,
				"MutableAcl should be been returned");

		return (MutableAcl) acl;
	}

	@Override
	public void deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren)
			throws ChildrenExistException {
		Assert.notNull(objectIdentity, "Object Identity required");
		Assert.notNull(objectIdentity.getIdentifier(),
				"Object Identity doesn't provide an identifier");

		if (deleteChildren) {
			List<ObjectIdentity> children = findChildren(objectIdentity);
			if (children != null) {
				for (ObjectIdentity child : children) {
					deleteAcl(child, true);
				}
			}
		} else {
			// We need to perform a manual verification for what a FK would
			// normally do We generally don't do this, in the interests of
			// deadlock management
			List<ObjectIdentity> children = findChildren(objectIdentity);
			if (children != null) {
				throw new ChildrenExistException("Cannot delete '"
						+ objectIdentity + "' (has " + children.size()
						+ " children)");
			}
		}

		Long oidPrimaryKey = retrieveObjectIdentityPrimaryKey(objectIdentity);

		// Delete this ACL's ACEs in the acl_entry table
		em.createQuery("DELETE FROM AclEntry e WHERE e.aclObjectIdentity.id = :id")
				.setParameter("id", oidPrimaryKey).executeUpdate();

		// Delete this ACL's acl_object_identity row
		em.createQuery("DELETE FROM AclObjectIdentity o WHERE o.id = :id")
				.setParameter("id", oidPrimaryKey).executeUpdate();
	}

	@Override
	public MutableAcl updateAcl(MutableAcl acl) throws NotFoundException {
		Assert.notNull(acl.getId(), "Object Identity doesn't provide an identifier");
		Assert.isInstanceOf(AclObjectIdentity.class, acl, "Implementation only accepts AclObjectIdentity");

		return em.merge((AclObjectIdentity) acl);
	}

	// -----------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------

	protected Long retrieveObjectIdentityPrimaryKey(ObjectIdentity oid) {
		try {
			String qString = "SELECT o.id FROM AclObjectIdentity o JOIN o.objectIdClass oc "
					+ "WHERE o.objectIdIdentity = :id "
					+ "AND oc.className = :className";
			TypedQuery<Long> q = em.createQuery(qString, Long.class);
			q.setParameter("id", oid.getIdentifier());
			q.setParameter("className", oid.getType());
			return q.getSingleResult();
		} catch (NoResultException notFound) {
			return null;
		}
	}

	protected Acl createObjectIdentity(ObjectIdentity object, Sid owner) {
		AclSid ownerSid = createOrRetrieveSidPrimaryKey(owner, true);
		AclClass aclClass = createOrRetrieveClassPrimaryKey(object.getType(),
				true);

		AclObjectIdentity aclObjId = new AclObjectIdentity();
		aclObjId.setObjectIdIdentity((Long) object.getIdentifier());
		aclObjId.setObjectIdClass(aclClass);
		aclObjId.setOwnerSid(ownerSid);
		aclObjId.setEntriesInheriting(true);
		em.persist(aclObjId);
		return aclObjId;
	}

	protected AclSid createOrRetrieveSidPrimaryKey(Sid sid, boolean allowCreate) {
		Assert.notNull(sid, "Sid required");

		String sidName;
		boolean sidIsPrincipal = true;

		if (sid instanceof PrincipalSid) {
			sidName = ((PrincipalSid) sid).getPrincipal();
		} else if (sid instanceof GrantedAuthoritySid) {
			sidName = ((GrantedAuthoritySid) sid).getGrantedAuthority();
			sidIsPrincipal = false;
		} else {
			throw new IllegalArgumentException(
					"Unsupported implementation of Sid");
		}

		return createOrRetrieveSidPrimaryKey(sidName, sidIsPrincipal,
				allowCreate);
	}

	protected AclSid createOrRetrieveSidPrimaryKey(String sidName,
			boolean sidIsPrincipal, boolean allowCreate) {

		AclSid sid = null;

		// Does this Sid already exist?
		String findString = "SELECT s FROM AclSid s WHERE s.sid = :sidString";
		TypedQuery<AclSid> findQuery = em.createQuery(findString, AclSid.class);
		findQuery.setParameter("sidString", sidName);
		try {
			return findQuery.getSingleResult();
		} catch (NoResultException e) {
			// If not, should we create it?
			if (allowCreate) {
				sid = new AclSid();
				sid.setPrincipal(sidIsPrincipal);
				sid.setSid(sidName);
				em.persist(sid);
			}
		}

		return sid;
	}

	protected AclClass createOrRetrieveClassPrimaryKey(String type,
			boolean allowCreate) {

		AclClass aclClass = null;

		// Does this class already exist?
		String findString = "SELECT c FROM AclClass c WHERE c.className = :className";
		TypedQuery<AclClass> findQuery = em.createQuery(findString,
				AclClass.class);
		findQuery.setParameter("className", type);
		try {
			return findQuery.getSingleResult();
		} catch (NoResultException e) {
			// If not, should we create it?
			if (allowCreate) {
				aclClass = new AclClass();
				aclClass.setClassName(type);
				em.persist(aclClass);
			}
		}

		return aclClass;
	}

}
