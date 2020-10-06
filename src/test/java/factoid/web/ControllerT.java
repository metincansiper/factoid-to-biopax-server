package factoid.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(SpringRunner.class)
@Import(Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerT {

  @Autowired
  private TestRestTemplate template;

  @Test
  public void testJsonToBiopax() throws IOException {
    String data = new String(Files.readAllBytes(Paths.get(getClass().getResource("/test2.json").getFile())));
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    HttpEntity<String> request = new HttpEntity<>(data, headers);
    String res = template.postForObject("/v2/json-to-biopax", request, String.class);
    assertNotNull(res);
    assertThat(res, containsString("biopax-level3.owl#"));
  }

  @Test
  public void testJsonToSbgn() throws IOException {
    String data = new String(Files.readAllBytes(Paths.get(getClass().getResource("/test2.json").getFile())));
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    HttpEntity<String> request = new HttpEntity<>(data, headers);
    String res = template.postForObject("/v2/json-to-sbgn", request, String.class);
    assertNotNull(res);
    assertThat(res, containsString("http://sbgn.org/libsbgn/"));
  }

  @Test
  public void testBiopaxToSbgn() throws IOException {
    String data = new String(Files.readAllBytes(Paths.get(getClass().getResource("/test.owl").getFile())));
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/vnd.biopax.rdf+xml");
    HttpEntity<String> request = new HttpEntity<>(data, headers);
    String res = template.postForObject("/v2/biopax-to-sbgn", request, String.class);
    assertNotNull(res);
    assertThat(res, containsString("http://sbgn.org/libsbgn/"));
  }
  
  @Test
  public void testBiopaxToFactoid() throws IOException {
    String data = new String(Files.readAllBytes(Paths.get(getClass().getResource("/pc_sm.owl").getFile())));
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/vnd.biopax.rdf+xml");
    HttpEntity<String> request = new HttpEntity<>(data, headers);
    String res = template.postForObject("/v2/biopax-to-json", request, String.class);
    assertNotNull(res);
    assertThat(res, containsString("Other"));
  }
}
