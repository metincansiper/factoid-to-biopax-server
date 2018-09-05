package converter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

public class FactoidToBiopaxTest {
  @Test
  public void testToOwl() throws IOException {
    Gson gson = new Gson();
    JsonReader reader = new JsonReader(new FileReader(getClass()
      .getResource("/test.json").getFile()));
    JsonArray templates = gson.fromJson(reader, JsonArray.class);
    FactoidToBiopax converter = new FactoidToBiopax();
    converter.addToModel(templates);
    String res = converter.convertToOwl();
    //quick (sanity) checks
    assertTrue(res!=null && res.length()>100);
    Model m = (new SimpleIOHandler()).convertFromOWL(new ByteArrayInputStream(res.getBytes("UTF-8")));
    assertNotNull(m);
    assertTrue(m.getObjects().size()>10);
    //TODO: add non-trivial tests
  }
}
