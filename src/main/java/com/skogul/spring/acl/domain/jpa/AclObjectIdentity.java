package com.skogul.spring.acl.domain.jpa;

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

@Entity(name = "AclObjectIdentity")
@Table(name = "acl_object_identity", uniqueConstraints = @UniqueConstraint(
		name = "unique_acl_object_identity", columnNames = { "object_id_class",
				"object_id_identity" }))
public class AclObjectIdentity implements Serializable {

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

	public AclClass getObjectIdClass() {
		return objectIdClass;
	}

	public void setObjectIdClass(AclClass objectIdClass) {
		this.objectIdClass = objectIdClass;
	}

	public List<AclEntry> getAclEntries() {
		return aclEntries;
	}

	public void setAclEntries(List<AclEntry> aclEntries) {
		this.aclEntries = aclEntries;
	}

	@Override
	public String toString() {
		return "AclObjectIdentity [id=" + id + ", objectIdIdentity="
				+ objectIdIdentity + ", objectIdClass=" + objectIdClass + "]";
	}

}
