package com.skogul.spring.jpaaclintegration.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Immutable;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.UnloadedSidException;
import org.springframework.util.Assert;

@Entity(name = "AclObjectIdentity")
@Table(name = "acl_object_identity", uniqueConstraints = @UniqueConstraint(
		name = "unique_acl_object_identity", columnNames = { "object_id_class",
				"object_id_identity" }))
@Immutable
public class AclObjectIdentity implements ObjectIdentity, MutableAcl,
		Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "object_id_identity", nullable = false, unique = false)
	private Long objectIdIdentity;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "object_id_class", referencedColumnName = "id",
			nullable = false, unique = false, insertable = true,
			updatable = true)
	private AclClass objectIdClass;

	@Column(name = "entries_inheriting", nullable = false, unique = false)
	private Boolean entriesInheriting;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_object", referencedColumnName = "id",
			nullable = true, unique = false, insertable = true,
			updatable = true)
	private AclObjectIdentity parentObject;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_sid", referencedColumnName = "id",
			nullable = true, unique = false, insertable = true,
			updatable = true)
	private AclSid ownerSid;

	@OneToMany(targetEntity = AclEntry.class, fetch = FetchType.LAZY,
			mappedBy = "aclObjectIdentity", cascade = CascadeType.REMOVE)
	private List<AclEntry> aclEntries = new ArrayList<AclEntry>();

	public AclObjectIdentity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getObjectIdIdentity() {
		return objectIdIdentity;
	}

	public void setObjectIdIdentity(Long objectIdIdentity) {
		this.objectIdIdentity = objectIdIdentity;
	}

	public Boolean getEntriesInheriting() {
		return entriesInheriting;
	}

	@Override
	public void setEntriesInheriting(boolean entriesInheriting) {
		this.entriesInheriting = entriesInheriting;
	}

	public AclObjectIdentity getParentObject() {
		return parentObject;
	}

	public void setParentObject(AclObjectIdentity parentObject) {
		this.parentObject = parentObject;
	}

	public AclClass getObjectIdClass() {
		return objectIdClass;
	}

	public void setObjectIdClass(AclClass objectIdClass) {
		this.objectIdClass = objectIdClass;
	}

	public AclSid getOwnerSid() {
		return ownerSid;
	}

	public void setOwnerSid(AclSid ownerSid) {
		this.ownerSid = ownerSid;
	}

	public List<AclEntry> getAclEntries() {
		return aclEntries;
	}

	public void setAclEntries(List<AclEntry> aclEntries) {
		this.aclEntries = aclEntries;
	}

	// -----------------------------------------------
	// ObjectIdentity
	// -----------------------------------------------

	@Override
	public Serializable getIdentifier() {
		return getObjectIdIdentity();
	}

	@Override
	public String getType() {
		return getObjectIdClass().getClassName();
	}

	// -----------------------------------------------
	// MutableAcl
	// -----------------------------------------------

	@Override
	public List<AccessControlEntry> getEntries() {
		ArrayList<AccessControlEntry> ret = new ArrayList<AccessControlEntry>();
		ret.addAll(getAclEntries());
		return ret;
	}

	@Override
	public ObjectIdentity getObjectIdentity() {
		return this;
	}

	@Override
	public Sid getOwner() {
		return getOwnerSid();
	}

	@Override
	public Acl getParentAcl() {
		return getParentObject();
	}

	@Override
	public boolean isEntriesInheriting() {
		return getEntriesInheriting();
	}

	@Override
	public boolean isGranted(List<Permission> permission, List<Sid> sids,
			boolean administrativeMode) throws NotFoundException,
			UnloadedSidException {
		Assert.notEmpty(permission, "Permissions required");
		Assert.notEmpty(sids, "SIDs required");
		final List<AccessControlEntry> aces = getEntries();

		AccessControlEntry firstRejection = null;

		for (Permission p : permission) {
			for (Sid sid : sids) {
				// Attempt to find exact match for this permission mask and SID
				boolean scanNextSid = true;

				for (AccessControlEntry ace : aces) {

					if ((ace.getPermission().getMask() == p.getMask())
							&& ace.getSid().equals(sid)) {
						// Found a matching ACE, so its authorization decision
						// will prevail
						if (ace.isGranting()) {
							// Success
							/*
							 * if (!administrativeMode) {
							 * auditLogger.logIfNeeded(true, ace); }
							 */

							return true;
						}

						// Failure for this permission, so stop search
						// We will see if they have a different permission
						// (this permission is 100% rejected for this SID)
						if (firstRejection == null) {
							// Store first rejection for auditing reasons
							firstRejection = ace;
						}

						scanNextSid = false; // helps break the loop

						break; // exit aces loop
					}
				}

				if (!scanNextSid) {
					break; // exit SID for loop (now try next permission)
				}
			}
		}

		if (firstRejection != null) {
			// We found an ACE to reject the request at this point, as no
			// other ACEs were found that granted a different permission
			/*
			 * if (!administrativeMode) { auditLogger.logIfNeeded(false,
			 * firstRejection); }
			 */

			return false;
		}

		// No matches have been found so far
		if (isEntriesInheriting() && (getParentAcl() != null)) {
			// We have a parent, so let them try to find a matching ACE
			return getParentAcl().isGranted(permission, sids, false);
		} else {
			// We either have no parent, or we're the uppermost parent
			throw new NotFoundException(
					"Unable to locate a matching ACE for passed permissions and SIDs");
		}
	}

	@Override
	public boolean isSidLoaded(List<Sid> sids) {
		return true;
	}

	@Override
	public void deleteAce(int aceIndex) throws NotFoundException {
		verifyAceIndexExists(aceIndex);
		getEntries().remove(aceIndex);
	}

	private void verifyAceIndexExists(int aceIndex) {
		if (aceIndex < 0) {
			throw new NotFoundException(
					"aceIndex must be greater than or equal to zero");
		}
		if (aceIndex >= getEntries().size()) {
			throw new NotFoundException(
					"aceIndex must refer to an index of the AccessControlEntry list. "
							+ "List size is " + getEntries().size()
							+ ", index was " + aceIndex);
		}
	}

	@Override
	public void insertAce(int atIndexLocation, Permission permission, Sid sid,
			boolean granting) throws NotFoundException {
		Assert.notNull(permission, "Permission required");
		Assert.notNull(sid, "Sid required");
		if (atIndexLocation < 0) {
			throw new NotFoundException(
					"atIndexLocation must be greater than or equal to zero");
		}
		if (atIndexLocation > getEntries().size()) {
			throw new NotFoundException(
					"atIndexLocation must be less than or equal to the size of the AccessControlEntry collection");
		}

		AclEntry entry = new AclEntry();
		entry.setAclObjectIdentity(this);
		entry.setSid((AclSid) sid);
		entry.setMask(permission.getMask());
		entry.setGranting(granting);

		aclEntries.add(atIndexLocation, entry);
	}

	@Override
	public void setOwner(Sid newOwner) {
		Assert.isInstanceOf(AclSid.class, newOwner,
				"Implementation only accepts instaces of AclSid");
		setOwnerSid((AclSid) newOwner);
	}

	@Override
	public void setParent(Acl newParent) {
		Assert.isInstanceOf(AclObjectIdentity.class, newParent,
				"Implementation only accepts instaces of AclSid");
		setParentObject((AclObjectIdentity) newParent);
	}

	@Override
	public void updateAce(int aceIndex, Permission permission)
			throws NotFoundException {
		verifyAceIndexExists(aceIndex);
		aclEntries.get(aceIndex).setMask(permission.getMask());
	}

}
