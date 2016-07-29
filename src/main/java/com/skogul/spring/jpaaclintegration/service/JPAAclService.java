package com.skogul.spring.jpaaclintegration.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.skogul.spring.jpaaclintegration.domain.AclObjectIdentity;
import com.skogul.spring.jpaaclintegration.repository.AclObjectIdentityRepository;

@Transactional
public class JPAAclService implements AclService {

	protected static final Log log = LogFactory.getLog(JPAAclService.class);

	protected AclObjectIdentityRepository objIdRepo;

	protected EntityManager em;

	public JPAAclService(EntityManager entityManager) {
		Assert.notNull(entityManager, "EntityManager required");
		this.em = entityManager;
	}

	@Override
	public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
		String qString = "SELECT NEW org.springframework.security.acls.domain.ObjectIdentityImpl(oc.className, o.objectIdIdentity) "
				+ "FROM AclObjectIdentity o "
				+ "JOIN o.objectIdClass oc "
				+ "JOIN o.parentObject p "
				+ "JOIN p.objectIdClass pc "
				+ "WHERE p.objectIdIdentity = :parentId "
				+ "AND pc.className = :className";

		TypedQuery<ObjectIdentity> query = em.createQuery(qString,
				ObjectIdentity.class);
		query.setParameter("parentId", parentIdentity.getIdentifier());
		query.setParameter("className", parentIdentity.getType());
		List<ObjectIdentity> objects = query.getResultList();
		return objects;
	}

	@Override
	public Acl readAclById(ObjectIdentity object) throws NotFoundException {
		String qString = "SELECT o FROM AclObjectIdentity o JOIN o.objectIdClass oc "
				+ "WHERE oc.className = :className AND o.objectIdIdentity = :id";
		TypedQuery<AclObjectIdentity> query = em.createQuery(qString,
				AclObjectIdentity.class);
		query.setParameter("id", object.getIdentifier());
		query.setParameter("className", object.getType());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new NotFoundException("No matching ACL found.");
		}
	}

	@Override
	public Acl readAclById(ObjectIdentity object, List<Sid> sids)
			throws NotFoundException {
		// sids are irrelevant here, each Acl contains entries for every sid
		return readAclById(object);
	}

	@Override
	public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects)
			throws NotFoundException {
		// Naive implementation
		Map<ObjectIdentity, Acl> map = new HashMap<ObjectIdentity, Acl>();
		for (ObjectIdentity objId : objects) {
			map.put(objId, readAclById(objId));
		}
		return map;
	}

	@Override
	public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects,
			List<Sid> sids) throws NotFoundException {
		return readAclsById(objects);
	}

}
