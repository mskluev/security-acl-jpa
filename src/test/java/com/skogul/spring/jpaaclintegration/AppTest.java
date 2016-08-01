package com.skogul.spring.jpaaclintegration;

import static org.assertj.core.api.StrictAssertions.assertThat;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.skogul.spring.jpaaclintegration.domain.AclSid;
import com.skogul.spring.jpaaclintegration.repository.AclSidRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testApplication-context.xml")
@Transactional
public class AppTest extends TestCase {

	@Inject
	public EntityManager em;
	@Inject
	public AclSidRepository sidRepo;
	
	
	@Test
	public void contextLoads() {
		assertThat(em).isNotNull();
	}

	@Test
	public void testSaveObject() {
		AclSid sid = new AclSid();
		sid.setPrincipal(true);
		sid.setSid("admin");
		sidRepo.save(sid);
		
		List<AclSid> sids = sidRepo.findAll();
		assertThat(sids.size()).isEqualTo(1);
	}
}
