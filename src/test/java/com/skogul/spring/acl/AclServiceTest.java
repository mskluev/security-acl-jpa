package com.skogul.spring.acl;

import static org.assertj.core.api.StrictAssertions.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.skogul.spring.acl.domain.ObjectIdentityImpl;
import com.skogul.spring.acl.domain.SecurityIdentityImpl;
import com.skogul.spring.acl.domain.jpa.AclEntry;
import com.skogul.spring.acl.domain.jpa.AclObjectIdentity;
import com.skogul.spring.acl.model.ObjectIdentity;
import com.skogul.spring.acl.model.SecurityIdentity;
import com.skogul.spring.acl.repository.AclEntryRepository;
import com.skogul.spring.acl.repository.AclObjectIdentityRepository;

/**
 * TODO - test error states
 * 
 * @author mskluev
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testApplication-context.xml")
@Transactional
public class AclServiceTest extends TestCase {

	private static final String TEST_CLASS_NAME = "foo.Bar";
	private static final String TEST_SID_STRING = "admin";
	private static final Integer TEST_PERMISSION_MASK = 1;
	private static final Integer TEST_PERMISSION_MASK_2 = 2;
	private static final Integer TEST_PERMISSION_MASK_4 = 4;

	@Inject
	public AclService aclService;
	@Inject
	public AclObjectIdentityRepository objIdRepo;
	@Inject
	public AclEntryRepository entryRepo;

	@Test
	public void contextLoads() {
		assertThat(aclService).isNotNull();
	}

	@Test
	public void testCreateACL() {
		int sizeBeforeCreate = objIdRepo.findAll().size();

		AclObjectIdentity acl = aclService.createACL(new ObjectIdentityImpl(
				TEST_CLASS_NAME, 1L));
		assertThat(acl.getId()).isNotNull();
		assertThat(objIdRepo.findAll().size()).isEqualTo(sizeBeforeCreate + 1);

		acl = objIdRepo.findOne(acl.getId());
		assertThat(acl.getObjectIdIdentity()).isEqualTo(1L);
		assertThat(acl.getObjectIdClass().getClassName()).isEqualTo(
				TEST_CLASS_NAME);
	}

	@Test
	public void testFindACL() {
		ObjectIdentity objId = new ObjectIdentityImpl(TEST_CLASS_NAME, 1L);
		AclObjectIdentity acl = aclService.createACL(objId);

		AclObjectIdentity acl2 = aclService.findACL(objId);
		assertThat(acl2.getObjectIdIdentity()).isEqualTo(
				acl.getObjectIdIdentity());
		assertThat(acl2.getObjectIdClass().getClassName()).isEqualTo(
				acl.getObjectIdClass().getClassName());
		assertThat(acl2).isEqualTo(acl);
	}

	@Test
	public void testDeleteACL() {
		aclService.createACL(new ObjectIdentityImpl(TEST_CLASS_NAME, 1L));
		int sizeBeforeDelete = objIdRepo.findAll().size();

		aclService.deleteACL(new ObjectIdentityImpl(TEST_CLASS_NAME, 1L));
		assertThat(objIdRepo.findAll().size()).isEqualTo(sizeBeforeDelete - 1);
	}

	@Test
	public void testAddPermission() {
		int sizeBeforeCreate = entryRepo.findAll().size();
		ObjectIdentity objId = new ObjectIdentityImpl(TEST_CLASS_NAME, 1L);
		AclObjectIdentity acl = aclService.createACL(objId);
		AclEntry entry = aclService.addPermission(objId,
				new SecurityIdentityImpl(TEST_SID_STRING, true),
				TEST_PERMISSION_MASK);

		assertThat(entry.getId()).isNotNull();
		assertThat(entryRepo.findAll().size()).isEqualTo(sizeBeforeCreate + 1);

		// Check all entry fields
		entry = entryRepo.findOne(entry.getId());
		assertThat(entry.getAclObjectIdentity()).isEqualTo(acl);
		assertThat(entry.getSid().getSid()).isEqualTo(TEST_SID_STRING);
		assertThat(entry.getEditable()).isTrue();
		assertThat(entry.getMask()).isEqualTo(TEST_PERMISSION_MASK);
	}

	@Test
	public void testRemovePermission() {
		// First Create an object
		int sizeBeforeCreate = entryRepo.findAll().size();
		ObjectIdentity objId = new ObjectIdentityImpl(TEST_CLASS_NAME, 1L);
		aclService.createACL(objId);
		// Add a permission to it
		SecurityIdentity secId = new SecurityIdentityImpl(TEST_SID_STRING, true);
		AclEntry entry = aclService.addPermission(objId, secId,
				TEST_PERMISSION_MASK);
		// Ensure everything is created
		assertThat(entry.getId()).isNotNull();
		assertThat(entryRepo.findAll().size()).isEqualTo(sizeBeforeCreate + 1);
		// Remove the permission, do a manual permission check
		entry = aclService.removePermission(objId, secId, TEST_PERMISSION_MASK);
		assertThat((entry.getMask() & TEST_PERMISSION_MASK) == 0).isTrue();
	}

	@Test
	public void testHasPermission() {
		int sizeBeforeCreate = entryRepo.findAll().size();
		// Add ACL
		ObjectIdentity objId = new ObjectIdentityImpl(TEST_CLASS_NAME, 1L);
		aclService.createACL(objId);
		// Add several permissions
		SecurityIdentity secId1 = new SecurityIdentityImpl(TEST_SID_STRING + 1,
				true);
		SecurityIdentity secId2 = new SecurityIdentityImpl(TEST_SID_STRING + 2,
				true);
		SecurityIdentity secId3 = new SecurityIdentityImpl(TEST_SID_STRING + 3,
				true);
		aclService.addPermission(objId, secId1, TEST_PERMISSION_MASK);
		aclService.addPermission(objId, secId2, TEST_PERMISSION_MASK_2);
		aclService.addPermission(objId, secId3, TEST_PERMISSION_MASK_4);

		// Everything get added?
		assertThat(entryRepo.findAll().size()).isEqualTo(sizeBeforeCreate + 3);

		// Permission Checks
		List<SecurityIdentity> sids = Arrays.asList(secId1, secId2, secId3);
		int _1 = TEST_PERMISSION_MASK;
		int _2 = TEST_PERMISSION_MASK_2;
		int _3 = TEST_PERMISSION_MASK + TEST_PERMISSION_MASK_2;
		int _4 = TEST_PERMISSION_MASK_4;
		int _5 = TEST_PERMISSION_MASK + TEST_PERMISSION_MASK_4;
		int _6 = TEST_PERMISSION_MASK_2 + TEST_PERMISSION_MASK_4;
		int _7 = TEST_PERMISSION_MASK + TEST_PERMISSION_MASK_2
				+ TEST_PERMISSION_MASK_4;
		assertThat(aclService.hasPermission(objId, sids, _1)).isTrue();
		assertThat(aclService.hasPermission(objId, sids, _2)).isTrue();
		assertThat(aclService.hasPermission(objId, sids, _3)).isTrue();
		assertThat(aclService.hasPermission(objId, sids, _4)).isTrue();
		assertThat(aclService.hasPermission(objId, sids, _5)).isTrue();
		assertThat(aclService.hasPermission(objId, sids, _6)).isTrue();
		assertThat(aclService.hasPermission(objId, sids, _7)).isTrue();
		assertThat(aclService.hasPermission(objId, sids, 16)).isFalse();
	}
}
