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
				String controllerName = template.get("controller").getAsString();
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
				String controlTypeStr = template.get("controlType").getAsString();
				
				addRegulationOfExpression(transcriptionFactorName, targetProtName, controlTypeStr);
			}
			else if(matchesTemplateType(typeStr, TemplateType.LOCATION_CHANGE)) {
				JsonArray macromoleculeNamesJSON = template.get("macromoleculeList").getAsJsonArray();
				List<String> macromoleculeNames = gson.fromJson(macromoleculeNamesJSON, List.class);
				
				String controllerProteinName = template.get("controllerProtein").getAsString();
				String oldLocation = template.get("oldLocation").getAsString();
				String newLocation = template.get("newLocation").getAsString();
				String controlTypeStr = template.get("controlType").getAsString();
				
				addLocationChange(macromoleculeNames, controllerProteinName, oldLocation, newLocation, controlTypeStr);
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
	
	private void addProteinModification(String targetProteinName, String controllerProteinName, String modificationType, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addProteinModification(targetProteinName, controllerProteinName, modificationType, controlType);
	}
	
	private void addComplexAssociation(List<String> moleculeNames) {
		model.addComplexAssociation(moleculeNames);
	}
	
	private void addComplexDissociation(List<String> macromoleculeNames) {
		model.addComplexDissociation(macromoleculeNames);
	}
	
	private void addMolecularInteraction(List<String> moleculeNames) {
		model.addMolecularInteraction(moleculeNames);
	}
	
	private void addBiochemicalReaction(String catalyzerName, List<String> inputMoleculeNames, List<String> outputMoleculeNames) {
		model.addBiochemicalReaction(catalyzerName, inputMoleculeNames, outputMoleculeNames);
	}
	
	private void addChemicalAffectsState(String chemicalName, String targetProteinName, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addChemicalAffectsState(chemicalName, targetProteinName, controlType);
	}
	
	private void addProteinControlsState(String controllerName, String targetProteinName, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addProteinControlsState(controllerName, targetProteinName, controlType);
	}
	
	private void addRegulationOfExpression(String transcriptionFactorName, String targetProtName, String controlTypeStr) {
		ControlType controlType = getControlType(controlTypeStr);
		model.addRegulationOfExpression(transcriptionFactorName, targetProtName, controlType);
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
		MOLECULAR_INTERACTION("Molecular Interaction"),
		PROTEIN_CONTROLS_STATE("Protein Controls State"),
		CHEMICAL_AFFECTS_STATE("Chemical Affects State"),
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
		template1.addProperty("type", TemplateType.PROTEIN_CONTROLS_STATE.getName());
		template1.addProperty("controller", "pcs_controller");
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
		template3.addProperty("type", TemplateType.BIOCHEMICAL_REACTION.getName());
		template3.addProperty("catalyzerName", "catalyzerName");
		template3.add("inputSmallMolecules", inputList);
		template3.add("outputSmallMolecules", outputList);
		templates.add(template3);
		
		JsonObject template4 = new JsonObject();
		template4.addProperty("type", TemplateType.COMPLEX_ASSOCIATION.getName());
		template4.add("moleculeList", moleculeList);
		templates.add(template4);
		
		JsonObject template5 = new JsonObject();
		template5.addProperty("type", TemplateType.COMPLEX_DISSOCIATION.getName());
		template5.add("moleculeList", moleculeList);
		templates.add(template5);
		
		JsonObject template6 = new JsonObject();
		template6.addProperty("type", TemplateType.EXPRESSION_REGULATION.getName());
		template6.addProperty("transcriptionFactor", "transcriptionFactor");
		template6.addProperty("targetProtein", "targetProtein");
		template6.addProperty("controlType", "activation");
		templates.add(template6);
		
		JsonObject template7 = new JsonObject();
		template7.addProperty("type", TemplateType.LOCATION_CHANGE.getName());
		template7.add("macromoleculeList", moleculeList);
		template7.addProperty("controlType", "activation");
		template7.addProperty("controllerProtein", "controllerProtein");
		template7.addProperty("oldLocation", "oldLocation");
		template7.addProperty("newLocation", "newLocation");
		templates.add(template7);
		
		JsonObject template8 = new JsonObject();
		template8.addProperty("type", TemplateType.MOLECULAR_INTERACTION.getName());
		template8.add("moleculeList", moleculeList);
		templates.add(template8);
		
		JsonObject template9 = new JsonObject();
		template9.addProperty("type", TemplateType.PROTEIN_MODIFICATION.getName());
		template9.addProperty("targetProtein", "targetProtein");
		template9.addProperty("controllerProtein", "controllerProtein");
		template9.addProperty("controlType", "activation");
		template9.addProperty("modification", "phosphorylated");
		templates.add(template9);
		
		FactoidToBiopax converter = new FactoidToBiopax();
		converter.addToModel(templates);
		
		System.out.println(converter.convertToOwl());
	}
}
