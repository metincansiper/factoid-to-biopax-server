package converter;

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
				String controllerName = template.get("controllerProtein").getAsString();
				String controlTypeStr = (String) template.get("controlType").getAsString();
				String targetProteinName = (String) template.get("targetProtein").getAsString();
				
				addProteinControlsState(controllerName, targetProteinName, controlTypeStr);
			}
			else if (matchesTemplateType(typeStr, TemplateType.CHEMICAL_AFFECTS_STATE)) {
				String chemicalName = template.get("chemical").getAsString();
				String controlTypeStr = (String) template.get("controlType").getAsString();
				String targetProteinName = (String) template.get("targetProtein").getAsString();
				
				addChemicalAffectsState(chemicalName, targetProteinName, controlTypeStr);
			}
			else if (matchesTemplateType(typeStr, TemplateType.EXPRESSION_REGULATION)) {
				String transcriptionFactorName = template.get("transcriptionFactor").getAsString();
				String targetProtName = template.get("targetProtein").getAsString();
				String controlTypeStr = template.get("controlType").getAsString();
				
				addRegulationOfExpression(transcriptionFactorName, targetProtName, controlTypeStr);
			}
			else if(matchesTemplateType(typeStr, TemplateType.MOLECULAR_INTERACTION)) {
				JsonArray moleculeNamesJSON = template.get("moleculeList").getAsJsonArray();
				List<String> moleculeNames = gson.fromJson(moleculeNamesJSON, List.class);
				
				addMolecularInteraction(moleculeNames);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PROTEIN_MODIFICATION)) {
				String targetProteinName = template.get("targetProtein").getAsString();
				String controllerProteinName = template.get("controllerProtein").getAsString();
				String modificationType = template.get("modification").getAsString();
				String controlTypeStr = template.get("controlType").getAsString();
				
				addProteinModification(targetProteinName, controllerProteinName, modificationType, controlTypeStr);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PROTEIN_CONTROLS_CONSUMPTION)) {
				String controllerProteinName = template.get("controllerProtein").getAsString();
				String chemicalName = template.get("chemical").getAsString();
				
				addProteinControlsConsumption(controllerProteinName, chemicalName);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PROTEIN_CONTROLS_PRODUCTION)) {
				String controllerProteinName = template.get("controllerProtein").getAsString();
				String chemicalName = template.get("chemical").getAsString();
				
				addProteinControlsProduction(controllerProteinName, chemicalName);
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
	
	private void addProteinModification(String targetProteinName, String controllerProteinName, String modificationType, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addProteinModification(targetProteinName, controllerProteinName, modificationType, controlType);
	}
	
	private void addMolecularInteraction(List<String> moleculeNames) {
		model.addMolecularInteraction(moleculeNames);
	}
	
	private void addChemicalAffectsState(String chemicalName, String targetProteinName, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addChemicalAffectsState(chemicalName, targetProteinName, controlType);
	}
	
	private void addProteinControlsState(String controllerProteinName, String targetProteinName, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addProteinControlsState(controllerProteinName, targetProteinName, controlType);
	}
	
	private void addRegulationOfExpression(String transcriptionFactorName, String targetProtName, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addRegulationOfExpression(transcriptionFactorName, targetProtName, controlType);
	}
	
	private void addProteinControlsConsumption(String controllerProteinName, String chemicalName) {
		model.addProteinControlsConsumption(controllerProteinName, chemicalName);
	}
	
	private void addProteinControlsProduction(String controllerProteinName, String chemicalName) {
		model.addProteinControlsProduction(controllerProteinName, chemicalName);
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
	
	public static void main(String[] args) {
		JsonArray templates = new JsonArray();
		
		JsonArray inputList = new JsonArray();
		JsonArray outputList = new JsonArray();
		JsonArray moleculeList = new JsonArray();
		
		inputList.add("input1");
		inputList.add("input2");
		
		outputList.add("output1");
		outputList.add("output2");
		
		moleculeList.add("molecule1");
		moleculeList.add("molecule2");
		
		JsonObject template1 = new JsonObject();
		template1.addProperty("type", TemplateType.PROTEIN_CONTROLS_STATE.getName());
		template1.addProperty("controllerProtein", "pcs_controllerProtein");
		template1.addProperty("controlType", "activation");
		template1.addProperty("targetProtein", "pcs_targetProtein");
		templates.add(template1);
		
		JsonObject template2 = new JsonObject();
		template2.addProperty("type", TemplateType.CHEMICAL_AFFECTS_STATE.getName());
		template2.addProperty("chemical", "cas_controller");
		template2.addProperty("controlType", "activation");
		template2.addProperty("targetProtein", "cas_targetProtein");
		templates.add(template2);
		
		JsonObject template3 = new JsonObject();
		template3.addProperty("type", TemplateType.EXPRESSION_REGULATION.getName());
		template3.addProperty("transcriptionFactor", "transcriptionFactor");
		template3.addProperty("targetProtein", "targetProtein");
		template3.addProperty("controlType", "activation");
		templates.add(template3);
		
		JsonObject template4 = new JsonObject();
		template4.addProperty("type", TemplateType.MOLECULAR_INTERACTION.getName());
		template4.add("moleculeList", moleculeList);
		templates.add(template4);
		
		JsonObject template5 = new JsonObject();
		template5.addProperty("type", TemplateType.PROTEIN_MODIFICATION.getName());
		template5.addProperty("targetProtein", "targetProtein");
		template5.addProperty("controllerProtein", "controllerProtein");
		template5.addProperty("controlType", "activation");
		template5.addProperty("modification", "phosphorylated");
		templates.add(template5);
		
		JsonObject template6 = new JsonObject();
		template6.addProperty("type", TemplateType.PROTEIN_CONTROLS_CONSUMPTION.getName());
		template6.addProperty("controllerProtein", "controllerProtein1");
		template6.addProperty("chemical", "chemical1");
		templates.add(template6);
		
		JsonObject template7 = new JsonObject();
		template7.addProperty("type", TemplateType.PROTEIN_CONTROLS_PRODUCTION.getName());
		template7.addProperty("controllerProtein", "controllerProtein2");
		template7.addProperty("chemical", "chemical2");
		templates.add(template7);
		
		FactoidToBiopax converter = new FactoidToBiopax();
		converter.addToModel(templates);
		
		System.out.println(converter.convertToOwl());
	}
}
