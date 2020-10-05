package factoid.converter;


import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.RowSetInternal;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.XmlReader;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BiopaxToFactoidTest {
	
	@Test
	public void testToJson() throws IOException {
		InputStream f = new FileInputStream(new File(getClass().getResource("/pc_sm.owl").getFile()));
		BioPAXIOHandler handler = new SimpleIOHandler();
		Model model = handler.convertFromOWL(f);
		BiopaxToFactoid b2f = new BiopaxToFactoid();
		JsonObject js = b2f.convert(model);
		System.out.println(js);
		assertThat(1, is(1));
		Set<Map.Entry<String, JsonElement>> entries = js.entrySet();
		List<JsonObject> jsonObjs = new ArrayList<JsonObject>();
		
		for ( Map.Entry<String, JsonElement> entry : entries ) {
			JsonArray arr = entry.getValue().getAsJsonArray();
			for ( JsonElement obj : arr ) {
				jsonObjs.add(obj.getAsJsonObject());
			}
		}
		
		for ( JsonObject obj : jsonObjs ) {
			boolean condPpts = obj.has("participants") && obj.getAsJsonArray("participants").size() == 2;
			boolean condSrcTgt = obj.has("controller") && obj.has("target");
			boolean cond = condPpts ^ condSrcTgt;
			
			assertThat(cond, is(true));
		}
	}
}
