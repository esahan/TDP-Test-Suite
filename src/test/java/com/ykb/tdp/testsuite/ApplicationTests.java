package com.ykb.tdp.testsuite;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
	
    @LocalServerPort
    private int port;

    private URL base;
    
    @Value("${server.servlet.context-path}")
    private String context;
    
    @Autowired
    private TestRestTemplate template;
  
    
    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + context+ "/");
    }

	@Test
	public void contextLoads() {
		ResponseEntity<String> response = template.getForEntity(base.toString(),String.class);
		assertNotNull(response.getBody());
        //assertThat(response.getBody(), equalTo("Hello World"));
	}

}
