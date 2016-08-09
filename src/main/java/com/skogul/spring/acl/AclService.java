package com.skogul.spring.acl;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.skogul.spring.acl.domain.jpa.AclClass;
import com.skogul.spring.acl.domain.jpa.AclEntry;
import com.skogul.spring.acl.domain.jpa.AclObjectIdentity;
import com.skogul.spring.acl.domain.jpa.AclSid;
import com.skogul.spring.acl.exceptions.AlreadyExistsException;
import com.skogul.spring.acl.exceptions.NotFoundException;
import com.skogul.spring.acl.model.ObjectIdentity;
import com.skogul.spring.acl.model.Permission;
import com.skogul.spring.acl.model.SecurityIdentity;
import com.skogul.spring.acl.repository.AclClassRepository;
import com.skogul.spring.acl.repository.AclEntryRepository;
import com.skogul.spring.acl.repository.AclObjectIdentityRepository;
import com.skogul.spring.acl.repository.AclSidRepository;

/**
 * Allows CRUD actions on access control domain records as well as permission
 * checks.
 * 
 * External Dependencies: - Spring Data JPA (Indirectly through repositories)
 * 
 * @author mskluev
 *
 */
public class AclService {

	@Inject
	protected AclObjectIdentityRepository objIdRepo;
	@Inject
	protected AclClassRepository classRepo;
	@Inject
	protected AclEntryRepository entryRepo;
	@Inject
	protected AclSidRepository sidRepo;

	private boolean doNotFoundchecks = false;

	public AclService() {
	}

	public AclService(boolean doNotFoundchecks) {
		this.doNotFoundchecks = doNotFoundchecks;
	}

	/**
	 * Answers the following question: 'Does this combination of sids grant the
	 * following permission on this object?'
	 * 
	 * The permission integer is assumed to a be a bitmask-based permission and
	 * it may be compound. See {@link Permission}.
	 * 
	 * @param objId
	 *            the ObjectIdentity of the domain object in question
	 * @param sids
	 *            SecurityIdentities to include in the check
	 * @param permission
	 *            bitwise permission to check for, potentially compounded (ie.
	 *            READ+WRITE)
	 * @return
	 */
	@Transactional(readOnly = true)
	public boolean hasPermission(ObjectIdentity objId,
			List<SecurityIdentity> sids, Integer permission) {

		List<String> sidStrings = sids.stream().map(s -> s.getSidString())
				.collect(Collectors.toCollection(LinkedList::new));

		// Does it exist? This is only necessary if we want to throw a 404 error
		// otherwise it is a wasted database call
		if (doNotFoundchecks) {
			AclObjectIdentity acl = findACL(objId);
			if (acl == null) {
				throw new NotFoundException("Object not found");
			}
		}

		List<AclEntry> entries = entryRepo.findEntriesForSidsOnObject(
				objId.getIdentifier(), objId.getType(), sidStrings);

		// Bitwise Or them all together
		Integer p = 0;
		for (AclEntry e : entries) {
			p = p | e.getMask();
		}

		return (p & permission) > 0;
	}

	@Transactional(readOnly = true)
	public AclObjectIdentity findACL(ObjectIdentity objId) {
		Assert.notNull(objId, "ObjectIdentity required");

		return findAclObject(objId);
	}

	@Transactional
	public AclObjectIdentity createACL(ObjectIdentity objId) {
		Assert.notNull(objId, "ObjectIdentity required");

		// Check this object identity hasn't already been persisted
		if (findAclObject(objId) != null) {
			throw new AlreadyExistsException("Object identity '" + objId
					+ "' already exists");
		}

		return createObjectIdentity(objId);
	}

	@Transactional
	public void deleteACL(ObjectIdentity objId) {
		Assert.notNull(objId, "ObjectIdentity required");

		// Find the ObjectIdentity to delete
		AclObjectIdentity aclObjId = findAclObject(objId);
		// Does it exist?
		if (aclObjId == null) {
			throw new NotFoundException("Object identity '" + objId
					+ "' not found");
		}

		// Delete it
		objIdRepo.delete(aclObjId.getId());
	}

	@Transactional(readOnly = true)
	public List<AclEntry> getPermissions(ObjectIdentity objId) {
		return findACL(objId).getAclEntries();
	}

	@Transactional
	public AclEntry addPermission(ObjectIdentity objId, SecurityIdentity secId,
			Integer perInteger) {
		// Does a corresponding AclObjectIdentity exist?
		AclObjectIdentity aclObjId = findAclObject(objId);
		// If not, throw error? Could create it...
		if (aclObjId == null) {
			throw new NotFoundException("Object identity '" + objId
					+ "' not found");
		}

		return addPermission(aclObjId, secId, perInteger);
	}

	@Transactional
	public AclEntry addPermission(AclObjectIdentity aclObjId,
			SecurityIdentity secId, Integer perInteger) {
		// Get the sidObject
		AclSid aclSid = createOrRetrieveSid(secId, true);

		// Does an AclEntry already exist?
		AclEntry aclEntry = entryRepo.findOneByAclObjectIdentityAndSid(
				aclObjId, aclSid);
		// If not, create one
		if (aclEntry == null) {
			// TODO editable should not always be true
			return createPermissionEntry(aclObjId, aclSid, true, perInteger);
		}

		// Check permission
		if ((perInteger & aclEntry.getMask()) > 0) {
			// Sid already has permission, we're done here
			return aclEntry;
		}
		// Add permission
		aclEntry.setMask(aclEntry.getMask() | perInteger);

		// Save & return
		return entryRepo.save(aclEntry);
	}

	@Transactional
	public AclEntry removePermission(ObjectIdentity objId,
			SecurityIdentity secId, Integer perInteger) {
		// Does an entry exist?
		AclEntry aclEntry = entryRepo
				.findOneByObjectIdAndClassNameAndSidString(
						objId.getIdentifier(), objId.getType(),
						secId.getSidString());
		// If not, throw error
		if (aclEntry == null) {
			throw new NotFoundException("AclEntry for Object identity '"
					+ objId + "' and Security identity '" + secId
					+ "' not found");
		}

		// Check permission
		if ((perInteger & aclEntry.getMask()) == 0) {
			// Sid doesn't have permission, we're done here
			return aclEntry;
		} // Remove permission
		aclEntry.setMask(aclEntry.getMask() ^ perInteger);

		// Save & return
		return entryRepo.save(aclEntry);
	}

	@Transactional
	public AclEntry setPermission(ObjectIdentity objId, SecurityIdentity secId,
			Integer perInteger) {
		// Does a corresponding AclObjectIdentity exist?
		AclObjectIdentity aclObjId = findAclObject(objId);
		// If not, throw error? Could create it...
		if (aclObjId == null) {
			throw new NotFoundException("Object identity '" + objId
					+ "' not found");
		}

		// Get the sidObject
		AclSid aclSid = createOrRetrieveSid(secId, true);

		// Does an AclEntry already exist?
		AclEntry aclEntry = entryRepo.findOneByAclObjectIdentityAndSid(
				aclObjId, aclSid);
		// If not, create it
		if (aclEntry == null) {
			// TODO editable should not always default to true
			return createPermissionEntry(aclObjId, aclSid, true, perInteger);
		}

		aclEntry.setMask(perInteger);

		// Save & return
		return entryRepo.save(aclEntry);
	}

	// -----------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------

	protected AclObjectIdentity findAclObject(ObjectIdentity objId) {
		return objIdRepo.findOneByObjectIdIdentityAndObjectIdClassClassName(
				objId.getIdentifier(), objId.getType());
	}

	protected AclObjectIdentity createObjectIdentity(ObjectIdentity object) {
		AclClass aclClass = createOrRetrieveClassPrimaryKey(object.getType(),
				true);

		AclObjectIdentity aclObjId = new AclObjectIdentity();
		aclObjId.setObjectIdIdentity((Long) object.getIdentifier());
		aclObjId.setObjectIdClass(aclClass);
		return objIdRepo.save(aclObjId);
	}

	protected AclClass createOrRetrieveClassPrimaryKey(String type,
			boolean allowCreate) {

		// Does this class already exist?
		AclClass aclClass = classRepo.findOneByClassName(type);
		if (aclClass != null) {
			return aclClass;
		}

		// Otherwise, create one
		if (allowCreate) {
			aclClass = new AclClass();
			aclClass.setClassName(type);
			classRepo.save(aclClass);
		}

		return aclClass;
	}

	protected AclSid createOrRetrieveSid(SecurityIdentity secId,
			boolean allowCreate) {

		// Does this sid already exist?
		AclSid aclSid = sidRepo.findOneBySid(secId.getSidString());
		if (aclSid != null) {
			return aclSid;
		}

		// Otherwise, create one
		if (allowCreate) {
			aclSid = new AclSid();
			aclSid.setSid(secId.getSidString());
			aclSid.setPrincipal(secId.isPrincipal());
			sidRepo.save(aclSid);
		}

		return aclSid;
	}

	protected AclEntry createPermissionEntry(AclObjectIdentity objId,
			AclSid sid, boolean editable, Integer permission) {
		AclEntry aclEntry = new AclEntry();
		aclEntry.setAclObjectIdentity(objId);
		aclEntry.setSid(sid);
		aclEntry.setEditable(editable);
		aclEntry.setMask(permission);
		aclEntry = entryRepo.save(aclEntry);
		// Add it to the parent container
		//objId.getAclEntries().add(aclEntry);
		return aclEntry;
	}
}
