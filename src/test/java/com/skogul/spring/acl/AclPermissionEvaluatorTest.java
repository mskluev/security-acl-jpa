package com.skogul.spring.acl;

import static org.assertj.core.api.StrictAssertions.assertThat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.skogul.spring.acl.domain.ObjectIdentityImpl;
import com.skogul.spring.acl.domain.SecurityIdentityImpl;
import com.skogul.spring.acl.model.ObjectIdentity;
import com.skogul.spring.acl.model.SecurityIdentity;
import com.skogul.spring.acl.repository.AclEntryRepository;
import com.skogul.spring.acl.repository.AclObjectIdentityRepository;
import com.skogul.spring.acl.testClasses.TestDomainObject;

/**
 * TODO - test error states
 * 
 * @author mskluev
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testApplication-context.xml")
@Transactional
public class AclPermissionEvaluatorTest extends TestCase {

	private static final String TEST_SID_STRING = "admin";
	private static final Integer TEST_PERMISSION_MASK = 1;

	@Inject
	public AclService aclService;
	@Inject
	public AclObjectIdentityRepository objIdRepo;
	@Inject
	public AclEntryRepository entryRepo;

	private AclPermissionEvaluator evaluator;

	@PostConstruct
	protected void init() {
		evaluator = new AclPermissionEvaluator(aclService);
	}

	@Test
	public void contextLoads() {
		assertThat(aclService).isNotNull();
	}

	@Test
	public void testHasPermission() {
		Authentication auth = new UsernamePasswordAuthenticationToken(
				TEST_SID_STRING, "admin");
		TestDomainObject domainObject = new TestDomainObject(1L);
		int sizeBeforeCreate = entryRepo.findAll().size();
		// Add ACL
		ObjectIdentity objId = new ObjectIdentityImpl(domainObject.getClass()
				.getName(), domainObject.getId());
		aclService.createACL(objId);
		// Add several permissions
		SecurityIdentity secId1 = new SecurityIdentityImpl(TEST_SID_STRING,
				true);
		aclService.addPermission(objId, secId1, TEST_PERMISSION_MASK);

		// Everything get added?
		assertThat(entryRepo.findAll().size()).isEqualTo(sizeBeforeCreate + 1);

		// Permission Checks
		int _1 = TEST_PERMISSION_MASK;
		assertThat(evaluator.hasPermission(auth, domainObject, _1)).isTrue();
		assertThat(evaluator.hasPermission(auth, domainObject, 16)).isFalse();
		assertThat(
				evaluator.hasPermission(auth, domainObject.getId(),
						domainObject.getClass().getName(), _1)).isTrue();
		assertThat(
				evaluator.hasPermission(auth, domainObject.getId(),
						domainObject.getClass().getName(), 16)).isFalse();
	}
}
