package factoid.converter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class FactoidToBiopaxTest {
  @Test
  public void testToOwl() throws IOException {
    Gson gson = new Gson();
    JsonReader reader = new JsonReader(new FileReader(getClass()
      .getResource("/test2.json").getFile()));
    JsonArray templates = gson.fromJson(reader, JsonArray.class);
    FactoidToBiopax converter = new FactoidToBiopax();
    converter.addToModel(templates);
    String res = converter.convertToBiopax();
    //quick (sanity) checks
    assertTrue(res!=null && res.length()>100);
    Model m = converterResultToModel(res);
    assertNotNull(m);
    assertTrue(m.getObjects().size()>10);

    Set<SmallMoleculeReference> smols = m.getObjects(SmallMoleculeReference.class);
    assertThat(smols.size(), is(2));

    Set<Modulation> mods = m.getObjects(Modulation.class);
    assertThat(mods.size(), is(2));

    List<?> controllers = mods.stream().map(Modulation::getController)
      .flatMap(Set::stream).collect(Collectors.toList());
    assertThat(controllers.stream().filter(SmallMolecule.class::isInstance).count(), is(2L));
  }

  //TODO: add a test method for each unique json template case
  @Test
  public void test4D_ChemicalAffects() throws IOException {
    String template = "[{\n" +
      "    \"type\": \"Other Interaction\",\n" +
      "    \"controlType\": \"activation\",\n" +
      "    \"participants\": [\n" +
      "      {\n" +
      "        \"type\": \"chemical\",\n" +
      "        \"name\": \"Progesterone\",\n" +
      "        \"xref\": {\n" +
      "          \"id\": 5994,\n" +
      "          \"db\": \"pubchem\"\n" +
      "        }\n" +
      "      },\n" +
      "      {\n" +
      "        \"type\": \"protein\",\n" +
      "        \"name\": \"LEP\",\n" +
      "        \"xref\": {\n" +
      "          \"id\": \"P41159\",\n" +
      "          \"db\": \"uniprot\"\n" +
      "        }\n" +
      "      }\n" +
      "    ]\n" +
      "  }]";

    FactoidToBiopax converter = new FactoidToBiopax();
    converter.addToModel(new Gson().fromJson(template, JsonArray.class));
    Model m = converterResultToModel(converter.convertToBiopax());

    assertEquals(8, m.getObjects().size());
  }

  private Model converterResultToModel(String result) throws UnsupportedEncodingException {
    return (new SimpleIOHandler()).convertFromOWL(new ByteArrayInputStream(result.getBytes("UTF-8")));
  }

}
