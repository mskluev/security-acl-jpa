package com.skogul.spring.acl.domain.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

//TODO add uniqueness constraint on objId + sid
@Entity(name = "AclEntry")
@Table(name = "acl_entry")
public class AclEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "mask", nullable = false, unique = false)
	private Integer mask;

	@Column(name = "editable", nullable = false, unique = false)
	private Boolean editable;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "acl_object_identity", referencedColumnName = "id",
			nullable = false, unique = false, insertable = true,
			updatable = true)
	private AclObjectIdentity aclObjectIdentity;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sid", referencedColumnName = "id", nullable = false,
			unique = false, insertable = true, updatable = true)
	private AclSid sid;

	public AclEntry() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getMask() {
		return mask;
	}

	public void setMask(Integer mask) {
		this.mask = mask;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public AclObjectIdentity getAclObjectIdentity() {
		return aclObjectIdentity;
	}

	public void setAclObjectIdentity(AclObjectIdentity aclObjectIdentity) {
		this.aclObjectIdentity = aclObjectIdentity;
	}

	public AclSid getSid() {
		return sid;
	}

	public void setSid(AclSid sid) {
		this.sid = sid;
	}

	@Override
	public String toString() {
		return "AclEntry [id=" + id + ", mask=" + mask + ", editable="
				+ editable + ", aclObjectIdentity=" + aclObjectIdentity
				+ ", sid=" + sid + "]";
	}


}
