package factoid.converter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

//TODO: test each distinct json template (cases 1 - 4E from the interaction types doc.)
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

    assertThat(res,notNullValue());
    assertThat(res, endsWith("</rdf:RDF>"));

    Model m = converterResultToModel(res);
    assertThat(m, notNullValue());
    assertThat(m.getObjects().size(), greaterThan(20));

    Set<SmallMoleculeReference> smols = m.getObjects(SmallMoleculeReference.class);
    assertThat(smols.size(), is(2));

    Set<Modulation> mods = m.getObjects(Modulation.class);
    assertThat(mods.size(), is(2));

    List<?> controllers = mods.stream().map(Modulation::getController)
      .flatMap(Set::stream).collect(Collectors.toList());
    assertThat(controllers.stream().filter(SmallMolecule.class::isInstance).count(), is(2L));
  }

  @Test
  //Progesterone chemical-affects LEP (case 4D) is about activation of LEP protein (catalyst) by Progesterone.
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
    assertThat(m.getObjects().size(), equalTo(8));

    Set<RelationshipXref> xrefs = m.getObjects(RelationshipXref.class);
    assertThat(xrefs, notNullValue());
    assertThat(xrefs.size(), equalTo(2));
    xrefs.stream().forEach(x -> {
      assertThat(x.getDb(), notNullValue());
      assertThat(x.getDb(), isOneOf("pubchem","uniprot"));
      assertThat(x.getId(), isOneOf("P41159","5994"));
    });

    Set<Modulation> mos = m.getObjects(Modulation.class);
    assertThat(mos, not(empty()));
    Modulation mod = mos.iterator().next();
    assertThat(mod.getControlType(), is(ControlType.ACTIVATION));
    assertThat(mod.getDisplayName(), nullValue()); //would be nice to generate "Progesterone activates production of LEP"
    assertThat(mod.getController().size(), equalTo(1));
    Controller con = mod.getController().iterator().next();
    assertThat(con, instanceOf(SmallMolecule.class));
    assertThat(con.getXref(), empty());
    assertThat(((SmallMolecule)con).getEntityReference(), notNullValue());
    assertThat(((SmallMolecule)con).getEntityReference().getXref(), not(empty()));
    assertThat(mod.getControlled().size(), equalTo(1));
    Process proc = mod.getControlled().iterator().next();
    assertThat(proc, instanceOf(Catalysis.class));
    Catalysis cat = (Catalysis) proc;
    assertThat(cat.getController().size(), equalTo(1));
    con = cat.getController().iterator().next();
    assertThat(con, instanceOf(Protein.class));
    assertThat(con.getXref(), empty());
    assertThat(((Protein)con).getEntityReference(), notNullValue());
    assertThat(((Protein)con).getEntityReference().getXref(), not(empty()));
    assertThat(cat.getControlType(), is(ControlType.ACTIVATION));
    //controlled reaction is unknown
    assertThat(cat.getControlled(), empty());
  }

  //local utils

  private Model converterResultToModel(String result) throws UnsupportedEncodingException {
    return (new SimpleIOHandler()).convertFromOWL(new ByteArrayInputStream(result.getBytes("UTF-8")));
  }

}
