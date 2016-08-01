package com.skogul.spring.jpaaclintegration;

import static org.assertj.core.api.StrictAssertions.assertThat;

import junit.framework.TestCase;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testApplication-context.xml")
public class AppTest extends TestCase {

	@Inject
	public EntityManager em;
	
	@Test
	public void contextLoads() {
		assertThat(em).isNotNull();
	}

}
