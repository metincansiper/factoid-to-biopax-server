package converter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
/*
 * A converter class that gets a JSON object that includes sequence of BioPAX templates and enables
 * conversion to BioPAX by adding these templates to underlying Templates Model instance.
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biopax.paxtools.model.level3.ControlType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import model.*;

public class FactoidToBiopax {
	
	private TemplatesModel model;
	private JsonParser jsonParser;
	private Gson gson;
	
	public FactoidToBiopax() {
		model = new TemplatesModel();
		jsonParser = new JsonParser();
		gson = new Gson();
	}
	
	public void addToModel(String templatesContent) {
		JsonArray templates = jsonParser.parse(templatesContent).getAsJsonArray();
		addToModel(templates);
	}
	
	public void addToModel(Reader contentReader) {
		JsonArray templates = jsonParser.parse(contentReader).getAsJsonArray();
		addToModel(templates);
	}
	
	public void addToModel(JsonArray templates) {
		
		Iterator<JsonElement> it = templates.iterator();
		
		while (it.hasNext()) {
			JsonObject template = (JsonObject) it.next();
			String typeStr = template.get("type").getAsString();
			System.out.println(typeStr);
			
			if (matchesTemplateType(typeStr, TemplateType.PROTEIN_CONTROLS_STATE)) {
				JsonObject controllerJson = template.get("controllerProtein").getAsJsonObject();
				String controlTypeStr = template.get("controlType").getAsString();
				JsonObject targetProteinJson = template.get("targetProtein").getAsJsonObject();
				
				addProteinControlsState(controllerJson, targetProteinJson, controlTypeStr);
			}
			else if (matchesTemplateType(typeStr, TemplateType.CHEMICAL_AFFECTS_STATE)) {
				JsonObject chemicalJson = template.get("chemical").getAsJsonObject();
				String controlTypeStr = template.get("controlType").getAsString();
				JsonObject targetProteinJson = template.get("targetProtein").getAsJsonObject();
				
				addChemicalAffectsState(chemicalJson, targetProteinJson, controlTypeStr);
			}
			else if (matchesTemplateType(typeStr, TemplateType.EXPRESSION_REGULATION)) {
				JsonObject transcriptionFactorJson = template.get("transcriptionFactor").getAsJsonObject();
				JsonObject targetProtJson = template.get("targetProtein").getAsJsonObject();
				String controlTypeStr = template.get("controlType").getAsString();
				
				addRegulationOfExpression(transcriptionFactorJson, targetProtJson, controlTypeStr);
			}
			else if(matchesTemplateType(typeStr, TemplateType.MOLECULAR_INTERACTION)) {
				JsonArray moleculeNamesJSON = template.get("moleculeList").getAsJsonArray();
				
				addMolecularInteraction(moleculeNamesJSON);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PROTEIN_MODIFICATION)) {
				JsonObject targetProteinJson = template.get("targetProtein").getAsJsonObject();
				JsonObject controllerProteinJson = template.get("controllerProtein").getAsJsonObject();
				String modificationType = template.get("modification").getAsString();
				String controlTypeStr = template.get("controlType").getAsString();
				
				addProteinModification(targetProteinJson, controllerProteinJson, modificationType, controlTypeStr);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PROTEIN_CONTROLS_CONSUMPTION)) {
				JsonObject controllerProteinJson = template.get("controllerProtein").getAsJsonObject();
				JsonObject chemicalJson = template.get("chemical").getAsJsonObject();
				
				addProteinControlsConsumption(controllerProteinJson, chemicalJson);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PROTEIN_CONTROLS_PRODUCTION)) {
				JsonObject controllerProteinJson = template.get("controllerProtein").getAsJsonObject();
				JsonObject chemicalJson = template.get("chemical").getAsJsonObject();
				
				addProteinControlsProduction(controllerProteinJson, chemicalJson);
			}
		}
	}
	
	public String convertToOwl() {
		return model.convertToOwl();
	}
	
	private static String getTemplateName(TemplateType templateType) {
		return templateType.getName();
	}
	
	private static boolean matchesTemplateType(String typeStr, TemplateType templateType) {
		return typeStr.equalsIgnoreCase(getTemplateName(templateType));
	}
	
	private ControlType getControlType(String controlTypeStr) {
		return CONTROL_TYPE_MAP.get(controlTypeStr.toUpperCase());
	}
	
	private void addProteinModification(JsonObject targetProteinJson, JsonObject controllerProteinJson, String modificationType, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		EntityModel targetProteinModel = gson.fromJson(targetProteinJson, EntityModel.class);
		EntityModel controllerProteinModel = gson.fromJson(controllerProteinJson, EntityModel.class);
		
		model.addProteinModification(targetProteinModel, controllerProteinModel, modificationType, controlType);
	}
	
	private void addMolecularInteraction(JsonArray moleculeNamesJSON) {
		List<EntityModel> moleculeModels = gson.fromJson(moleculeNamesJSON, new TypeToken<List<EntityModel>>(){}.getType());
		model.addMolecularInteraction(moleculeModels);
	}
	
	private void addChemicalAffectsState(JsonObject chemicalJson, JsonObject targetProteinJson, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		EntityModel chemicalModel = gson.fromJson(chemicalJson, EntityModel.class);
		EntityModel targetProteinModel = gson.fromJson(targetProteinJson, EntityModel.class);
		
		model.addChemicalAffectsState(chemicalModel, targetProteinModel, controlType);
	}
	
	private void addProteinControlsState(JsonObject controllerProteinJson, JsonObject targetProteinJson, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		EntityModel controllerProteinModel = gson.fromJson(controllerProteinJson, EntityModel.class);
		EntityModel targetProteinModel = gson.fromJson(targetProteinJson, EntityModel.class);
		
		model.addProteinControlsState(controllerProteinModel, targetProteinModel, controlType);
	}
	
	private void addRegulationOfExpression(JsonObject transcriptionFactorJson, JsonObject targetProtJson, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		EntityModel transcriptionFactorModel = gson.fromJson(transcriptionFactorJson, EntityModel.class);
		EntityModel targetProtModel = gson.fromJson(targetProtJson, EntityModel.class);
		
		model.addRegulationOfExpression(transcriptionFactorModel, targetProtModel, controlType);
	}
	
	private void addProteinControlsConsumption(JsonObject controllerProteinJson, JsonObject chemicalJson) {
		EntityModel controllerProteinModel = gson.fromJson(controllerProteinJson, EntityModel.class);
		EntityModel chemicalModel = gson.fromJson(chemicalJson, EntityModel.class);
		
		model.addProteinControlsConsumption(controllerProteinModel, chemicalModel);
	}
	
	private void addProteinControlsProduction(JsonObject controllerProteinJson, JsonObject chemicalJson) {
		EntityModel controllerProteinModel = gson.fromJson(controllerProteinJson, EntityModel.class);
		EntityModel chemicalModel = gson.fromJson(chemicalJson, EntityModel.class);
		
		model.addProteinControlsProduction(controllerProteinModel, chemicalModel);
	}
	
	private static Map<String, ControlType> createControlTypeMap() {
		Map<String, ControlType> map = new HashMap<String, ControlType>();
		map.put("ACTIVATION", ControlType.ACTIVATION);
		map.put("INHIBITION", ControlType.INHIBITION);
		
		return map;
	}
	
	private static enum TemplateType {
		PROTEIN_MODIFICATION("Protein Modification"),
		MOLECULAR_INTERACTION("Molecular Interaction"),
		PROTEIN_CONTROLS_STATE("Protein Controls State"),
		CHEMICAL_AFFECTS_STATE("Chemical Affects State"),
		EXPRESSION_REGULATION("Expression Regulation"),
		PROTEIN_CONTROLS_CONSUMPTION("Protein Controls Consumption"),
		PROTEIN_CONTROLS_PRODUCTION("Protein Controls Production");
			
		private String name;
		
		TemplateType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return getName();
		}
	}
	
	private static final Map<String, ControlType> CONTROL_TYPE_MAP = createControlTypeMap();
	
	public static void main(String[] args) throws FileNotFoundException {
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("src/resources/test.json"));
		JsonArray templates = gson.fromJson(reader, JsonArray.class);
		FactoidToBiopax converter = new FactoidToBiopax();
		converter.addToModel(templates);
		
		System.out.println(converter.convertToOwl());
	}
}
