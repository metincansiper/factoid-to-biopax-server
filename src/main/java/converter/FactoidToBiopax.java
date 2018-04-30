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
			
			if (matchesTemplateType(typeStr, TemplateType.ACTIVITION_INHIBITION)) {
				String regulatorProteinName = template.get("regulatorProtein").getAsString();
				String effectTypeStr = (String) template.get("effectType").getAsString();
				String targetProteinName = (String) template.get("targetProtein").getAsString();
				
				addActivationInhibition(regulatorProteinName, targetProteinName, effectTypeStr);
			}
			else if (matchesTemplateType(typeStr, TemplateType.BIOCHEMICAL_REACTION)) {
				String catalyzerName = template.get("catalyzerName").getAsString();
				
				JsonArray inputMoleculeNamesJSON = template.get("inputSmallMolecules").getAsJsonArray();
				JsonArray outputMoleculeNamesJSON = template.get("outputSmallMolecules").getAsJsonArray();
				
				List<String> inputMoleculeNames = gson.fromJson(inputMoleculeNamesJSON, List.class);
				List<String> outputMoleculeNames = gson.fromJson(outputMoleculeNamesJSON, List.class);
				
				addBiochemicalReaction(catalyzerName, inputMoleculeNames, outputMoleculeNames);
			}
			else if (matchesTemplateType(typeStr, TemplateType.COMPLEX_ASSOCIATION)) {
				JsonArray moleculeNamesJSON = template.get("moleculeList").getAsJsonArray();
				List<String> moleculeNames = gson.fromJson(moleculeNamesJSON, List.class);
				
				addComplexAssociation(moleculeNames);
			}
			else if(matchesTemplateType(typeStr, TemplateType.COMPLEX_DISSOCIATION)) {
				JsonArray moleculeNamesJSON = template.get("moleculeList").getAsJsonArray();
				List<String> moleculeNames = gson.fromJson(moleculeNamesJSON, List.class);
				
				addComplexDissociation(moleculeNames);
			}
			else if (matchesTemplateType(typeStr, TemplateType.EXPRESSION_REGULATION)) {
				String transcriptionFactorName = template.get("transcriptionFactor").getAsString();
				String targetProtName = template.get("targetProtein").getAsString();
				String effectTypeStr = template.get("effectType").getAsString();
				
				addRegulationOfExpression(transcriptionFactorName, targetProtName, effectTypeStr);
			}
			else if(matchesTemplateType(typeStr, TemplateType.LOCATION_CHANGE)) {
				JsonArray macromoleculeNamesJSON = template.get("macromoleculeList").getAsJsonArray();
				List<String> macromoleculeNames = gson.fromJson(macromoleculeNamesJSON, List.class);
				
				String controllerProteinName = template.get("contollerProtein").getAsString();
				String oldLocation = template.get("oldLocation").getAsString();
				String newLocation = template.get("newLocation").getAsString();
				String controlTypeStr = template.get("controlType").getAsString();
				
				addLocationChange(macromoleculeNames, controllerProteinName, oldLocation, newLocation, controlTypeStr);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PHYSICAL_INTERACTION)) {
				JsonArray moleculeNamesJSON = template.get("moleculeList").getAsJsonArray();
				List<String> moleculeNames = gson.fromJson(moleculeNamesJSON, List.class);
				
				addComplexDissociation(moleculeNames);
			}
			else if(matchesTemplateType(typeStr, TemplateType.PROTEIN_MODIFICATION)) {
				String modifiedProteinName = template.get("modifiedProtein").getAsString();
				String controllerProteinName = template.get("controllerProtein").getAsString();
				String modificationType = template.get("modification").getAsString();
				String controlTypeStr = template.get("controlType").getAsString();
				
				addProteinModification(modifiedProteinName, controllerProteinName, modificationType, controlTypeStr);
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
	
	private void addLocationChange(List<String> macromoleculeNames, String controllerProteinName, String oldLocation, 
			String newLocation, String controlTypeStr) {
		
		ControlType controlType = getControlType(controlTypeStr);
		model.addLocationChange(macromoleculeNames, controllerProteinName, oldLocation, newLocation, controlType);
	}
	
	private void addProteinModification(String modifiedProteinName, String controllerProteinName, String modificationType, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addProteinModification(modifiedProteinName, controllerProteinName, modificationType, controlType);
	}
	
	private void addComplexAssociation(List<String> moleculeNames) {
		model.addComplexAssociation(moleculeNames);
	}
	
	private void addComplexDissociation(List<String> macromoleculeNames) {
		model.addComplexDissociation(macromoleculeNames);
	}
	
	private void addPhysicalInteraction(List<String> moleculeNames) {
		model.addPhysicalInteraction(moleculeNames);
	}
	
	private void addBiochemicalReaction(String catalyzerName, List<String> inputMoleculeNames, List<String> outputMoleculeNames) {
		model.addBiochemicalReaction(catalyzerName, inputMoleculeNames, outputMoleculeNames);
	}
	
	private void addActivationInhibition(String regulatorProteinName, String targetProteinName, String effectTypeStr) {
		ControlType effectType = getControlType(effectTypeStr);
		model.addActivationInhibition(regulatorProteinName, targetProteinName, effectType);
	}
	
	private void addRegulationOfExpression(String transcriptionFactorName, String targetProtName, String effectTypeStr) {
		ControlType effectType = getControlType(effectTypeStr);
		model.addRegulationOfExpression(transcriptionFactorName, targetProtName, effectType);
	}
	
	private static Map<String, ControlType> createControlTypeMap() {
		Map<String, ControlType> map = new HashMap<String, ControlType>();
		map.put("ACTIVATION", ControlType.ACTIVATION);
		map.put("INHIBITION", ControlType.INHIBITION);
		
		return map;
	}
	
	private static enum TemplateType {
		PROTEIN_MODIFICATION("Protein Modification"),
		COMPLEX_ASSOCIATION("Complex Association"),
		COMPLEX_DISSOCIATION("Complex Dissociation"),
		LOCATION_CHANGE("Location Change"),
		BIOCHEMICAL_REACTION("Biochemical Reaction"),
		PHYSICAL_INTERACTION("Physical Interaction"),
		ACTIVITION_INHIBITION("Activation Inhibition"),
		EXPRESSION_REGULATION("Expression Regulation");
			
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
		template1.addProperty("type", TemplateType.ACTIVITION_INHIBITION.getName());
		template1.addProperty("regulatorProtein", "regulatorProtein");
		template1.addProperty("effectType", "effectType");
		template1.addProperty("targetProtein", "targetProtein");
		templates.add(template1);
		
		JsonObject template2 = new JsonObject();
		template2.addProperty("type", TemplateType.BIOCHEMICAL_REACTION.getName());
		template2.addProperty("catalyzerName", "catalyzerName");
		template2.add("inputSmallMolecules", inputList);
		template2.add("outputSmallMolecules", outputList);
		templates.add(template2);
		
		JsonObject template3 = new JsonObject();
		template3.addProperty("type", TemplateType.COMPLEX_ASSOCIATION.getName());
		template3.add("moleculeList", moleculeList);
		templates.add(template3);
		
		JsonObject template4 = new JsonObject();
		template4.addProperty("type", TemplateType.COMPLEX_DISSOCIATION.getName());
		template4.add("moleculeList", moleculeList);
		templates.add(template4);
		
		JsonObject template5 = new JsonObject();
		template5.addProperty("type", TemplateType.EXPRESSION_REGULATION.getName());
		template5.addProperty("transcriptionFactor", "transcriptionFactor");
		template5.addProperty("targetProtein", "targetProtein");
		template5.addProperty("effectType", "activation");
		templates.add(template5);
		
		JsonObject template6 = new JsonObject();
		template6.addProperty("type", TemplateType.LOCATION_CHANGE.getName());
		template6.add("macromoleculeList", moleculeList);
		template6.addProperty("controlType", "activation");
		template6.addProperty("contollerProtein", "contollerProtein");
		template6.addProperty("oldLocation", "oldLocation");
		template6.addProperty("newLocation", "newLocation");
		templates.add(template6);
		
		JsonObject template7 = new JsonObject();
		template7.addProperty("type", TemplateType.PHYSICAL_INTERACTION.getName());
		template7.add("moleculeList", moleculeList);
		templates.add(template7);
		
		JsonObject template8 = new JsonObject();
		template8.addProperty("type", TemplateType.PROTEIN_MODIFICATION.getName());
		template8.addProperty("modifiedProtein", "modifiedProtein");
		template8.addProperty("controllerProtein", "controllerProtein");
		template8.addProperty("controlType", "activation");
		template8.addProperty("modification", "phosphorylated");
		templates.add(template8);
		
		FactoidToBiopax converter = new FactoidToBiopax();
		converter.addToModel(templates);
		
		System.out.println(converter.convertToOwl());
		
		System.out.println(TemplateType.ACTIVITION_INHIBITION.getName().equals("Activation Inhibition"));
	}
}
