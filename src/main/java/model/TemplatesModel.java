package model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;

/*
 * A model class that keeps an underlying BioPAX model and enables updating it by adding templates.
 */
public class TemplatesModel {
	
	BioPAXModel model;
	
	public TemplatesModel() {
		model = new BioPAXModel();
	}
	
	// Section: public methods
	
	// modifiers

	public void addProteinModification(EntityModel targetProteinModel, EntityModel controllerProteinModel, String modificationType, ControlType controlType) {
		
		Set<String> leftModificationTypes = new HashSet<String>();
		Set<String> rightModificationTypes = new HashSet<String>();
		
		rightModificationTypes.add(modificationType);
		
		addActiveInactiveModifications(controlType, leftModificationTypes, rightModificationTypes);
		
		String targetProteinName = targetProteinModel.getName();
		XrefModel targetProteinXref = targetProteinModel.getXref();
		
		String controllerProteinName = controllerProteinModel.getName();
		XrefModel controllerProteinXref = controllerProteinModel.getXref();
		
		ProteinReference protRef = model.getOrCreateEntityReference(ProteinReference.class, targetProteinName, targetProteinXref);
		Protein left = model.getOrCreatePhysicalEntity(Protein.class, targetProteinName, null, protRef, leftModificationTypes);
		Protein right = model.getOrCreatePhysicalEntity(Protein.class, targetProteinName, null, protRef, rightModificationTypes);
		
		ProteinReference controllerRef = model.getOrCreateEntityReference(ProteinReference.class, controllerProteinName, controllerProteinXref);
		Protein controller = model.getOrCreatePhysicalEntity(Protein.class, controllerProteinName, null, controllerRef);
		
		Conversion conversion = model.addNewConversion(Conversion.class, left, right);
		model.addNewControl(Control.class, controller, conversion, controlType);
	}
	
	public void addMolecularInteraction(List<EntityModel> moleculeModels) {
		MolecularInteraction molecularInteraction = model.addNew(MolecularInteraction.class);
		
		for(EntityModel moleculeModel : moleculeModels) {
			String moleculeName = moleculeModel.getName();
			XrefModel moleculeXref = moleculeModel.getXref();
			
			ProteinReference protRef = model.getOrCreateEntityReference(ProteinReference.class, moleculeName, moleculeXref);
			Protein molecule = model.getOrCreatePhysicalEntity(Protein.class, moleculeName, null, protRef);
			molecularInteraction.addParticipant(molecule);
		}
	}
	
	public void addRegulationOfExpression(EntityModel transcriptionFactorModel, EntityModel targetProtModel, ControlType controlType) {
		
		String transcriptionFactorName = transcriptionFactorModel.getName();
		XrefModel transcriptionFactorXref = transcriptionFactorModel.getXref();
		
		String targetProtName = targetProtModel.getName();
		XrefModel targetProtXref = targetProtModel.getXref();
		
		ProteinReference targetRef = model.getOrCreateEntityReference(ProteinReference.class, targetProtName, targetProtXref);
		ProteinReference tfRef = model.getOrCreateEntityReference(ProteinReference.class, transcriptionFactorName, transcriptionFactorXref);
		
		Protein tf = model.getOrCreatePhysicalEntity(Protein.class, transcriptionFactorName, null, tfRef);
		Protein product = model.getOrCreatePhysicalEntity(Protein.class, targetProtName, null, targetRef);
		
		TemplateReaction reaction = model.addNew(TemplateReaction.class);
		reaction.addProduct(product);
		
		model.addNewControl(TemplateReactionRegulation.class, tf, reaction, controlType);
	}
	
	public void addChemicalAffectsState(EntityModel chemicalModel, EntityModel targetProteinModel, ControlType controlType) {
		addStateChange(chemicalModel, targetProteinModel, controlType, SmallMolecule.class, SmallMoleculeReference.class);
	}
	
	public void addProteinControlsState(EntityModel controllerProteinModel, EntityModel targetProteinModel, ControlType controlType) {
		addStateChange(controllerProteinModel, targetProteinModel, controlType, SmallMolecule.class, SmallMoleculeReference.class);
	}
	
	public void addProteinControlsConsumption(EntityModel controllerProteinModel, EntityModel chemicalModel) {
		addProteinControlsConsumptionOrProduction(controllerProteinModel, chemicalModel, SideType.LEFT);
	}
	
	public void addProteinControlsProduction(EntityModel controllerProteinModel, EntityModel chemicalModel) {
		addProteinControlsConsumptionOrProduction(controllerProteinModel, chemicalModel, SideType.RIGHT);
	}
	
	// accessors
	
	public String convertToOwl() {
		return model.convertToOwl();
	}
	
	// Section: private helper methods
	
	private void addProteinControlsConsumptionOrProduction(EntityModel controllerProteinModel, EntityModel chemicalModel, SideType chemicalSide) {
		String controllerProteinName = controllerProteinModel.getName();
		XrefModel controllerProteinXref = controllerProteinModel.getXref();
		
//		SideType otherSide = getOppositeSide(chemicalSide);
//		String otherChemName = null;
		
		BiochemicalReaction reaction = model.addNewConversion(BiochemicalReaction.class);
		
		addNewSmallMoleculeToConversion(reaction, chemicalModel, chemicalSide);
//		addNewSmallMoleculeToConversion(reaction, otherChemName, otherSide);
		
		ProteinReference catalyzerRef = model.getOrCreateEntityReference(ProteinReference.class, controllerProteinName, controllerProteinXref);
		Protein catalyzer = model.getOrCreatePhysicalEntity(Protein.class, controllerProteinName, null, catalyzerRef);
		
		model.addNewControl(Catalysis.class, catalyzer, reaction, null);
	}
	
	private <T1 extends PhysicalEntity, T2 extends EntityReference> void addStateChange(EntityModel controllerModel, EntityModel targetProteinModel, ControlType controlType, Class<T1> controllerClass, Class<T2> controllerRefClass) {
		
		Set<String> leftModificationTypes = new HashSet<String>();
		Set<String> rightModificationTypes = new HashSet<String>();
		
		addActiveInactiveModifications(controlType, leftModificationTypes, rightModificationTypes);
		
		String controllerName = controllerModel.getName();
		XrefModel controllerXref = controllerModel.getXref();
		
		String targetProteinName = targetProteinModel.getName();
		XrefModel targetProteinXref = targetProteinModel.getXref();
		
		ProteinReference targetProtRef = model.getOrCreateEntityReference(ProteinReference.class, targetProteinName, targetProteinXref);
		
		T2 controllerRef = model.getOrCreateEntityReference(controllerRefClass, controllerName, controllerXref);
		
		T1 controller = model.getOrCreatePhysicalEntity(controllerClass, controllerName, null, controllerRef);
		Protein leftProtein = model.getOrCreatePhysicalEntity(Protein.class, targetProteinName, null, targetProtRef, leftModificationTypes);
		Protein rightProtein = model.getOrCreatePhysicalEntity(Protein.class, targetProteinName, null, targetProtRef, rightModificationTypes);
		
		Conversion conversion = model.addNewConversion(Conversion.class, leftProtein, rightProtein);
		model.addNewControl(Control.class, controller, conversion, controlType);
	}
	
	// Add a new small molecule to the given side of conversion
	private void addNewSmallMoleculeToConversion(Conversion conversion, EntityModel moleculeModel, SideType sideType) {
		String moleculeName = moleculeModel.getName();
		XrefModel moleculeXref = moleculeModel.getXref();
		
		SmallMoleculeReference moleculeRef = model.getOrCreateEntityReference(SmallMoleculeReference.class, moleculeName, moleculeXref);
		SmallMolecule molecule = model.getOrCreatePhysicalEntity(SmallMolecule.class, moleculeName, null, moleculeRef);
		addSideToConversion(conversion, molecule, sideType);
	}
	
	// Add a physical entity to given side of conversation.
	private static void addSideToConversion(Conversion conversion, PhysicalEntity entity, SideType side) {
		if (side == SideType.LEFT) {
			conversion.addLeft(entity);
		}
		else if (side == SideType.RIGHT) {
			conversion.addRight(entity);
		}
	}
	
//	private static SideType getOppositeSide(SideType side) {
//		return side == SideType.LEFT ? SideType.RIGHT : SideType.LEFT;
//	}
	
	// Update modification types set by adding ACTIVE and INACTIVE modifications to the sides according to control type
	private static void addActiveInactiveModifications(ControlType controlType, Set<String> leftModificationTypes, Set<String> rightModificationTypes){
		String leftType = INACTIVE;
		String rightType = ACTIVE;
		
		// ACTIVATION by default
		if (controlType == ControlType.INHIBITION) {
			leftType = ACTIVE;
			rightType = INACTIVE;
		}
		
		leftModificationTypes.add(leftType);
		rightModificationTypes.add(rightType);
	}
	
	// Section: static variables
	
	private static final String ACTIVE = "active";
	private static final String INACTIVE = "inactive";
	
	private static enum SideType {
		LEFT,
		RIGHT
	}
}

//	Non functional commented out methods
//  These methods were written before for some templates not supported by factoid yet
//  if factoid will support these templates in the feature they would be revised and used
//	public void addLocationChange(List<String> macromoleculeNames, String controllerProteinName, String oldLocation, 
//			String newLocation, ControlType controlType) {
//		
//		Transport transport = model.addNewConversion(Transport.class);
//		
//		Complex leftComplex = null;
//		Complex rightComplex = null;
//		
//		boolean multipleMolecules = false;
//	
//		if (macromoleculeNames.size() > 0) {		
//			multipleMolecules = true;
//			
//			leftComplex = model.getOrCreatePhysicalEntity(Complex.class);
//			rightComplex = model.getOrCreatePhysicalEntity(Complex.class);
//			
//			transport.addLeft(leftComplex);
//			transport.addRight(rightComplex);
//		}
//		
//		CellularLocationVocabulary oldClv = model.getOrCreateCellularLocationVocabulary(oldLocation);
//		CellularLocationVocabulary newClv = model.getOrCreateCellularLocationVocabulary(newLocation);
//		
//		for(String macromoleculeName : macromoleculeNames) {
//			
//			ProteinReference mmRef = model.getOrCreateEntityReference(ProteinReference.class, macromoleculeName);	
//			Protein left = model.getOrCreatePhysicalEntity(Protein.class, macromoleculeName, oldClv, mmRef);
//			Protein right = model.getOrCreatePhysicalEntity(Protein.class, macromoleculeName, newClv, mmRef);
//			
//			if (multipleMolecules) {
//				leftComplex.addComponent(left);
//				rightComplex.addComponent(right);
//			}
//			else {
//				transport.addLeft(left);
//				transport.addRight(right);
//			}
//		}
//		
//		ProteinReference controllerRef = model.getOrCreateEntityReference(ProteinReference.class, controllerProteinName);
//		Protein controller = model.getOrCreatePhysicalEntity(Protein.class, controllerProteinName, null, controllerRef);
//		
//		model.addNewControl(Control.class, controller, transport, controlType);
//	}
//	
//	public void addComplexAssociation(List<String> moleculeNames) {
//		addComplexAssembly(moleculeNames, ComplexAssemblyType.ASSOCIATION);
//	}
//	
//	public void addComplexDissociation(List<String> moleculeNames) {
//		addComplexAssembly(moleculeNames, ComplexAssemblyType.DISSOCIATION);
//	}
//	
//	public void addBiochemicalReaction(String catalyzerName, List<String> inputMoleculeNames, List<String> outputMoleculeNames) {
//		BiochemicalReaction reaction = model.addNewConversion(BiochemicalReaction.class);
//		
//		addNewSmallMoleculesToConversion(reaction, inputMoleculeNames, SideType.LEFT);
//		addNewSmallMoleculesToConversion(reaction, outputMoleculeNames, SideType.RIGHT);
//		
//		ProteinReference catalyzerRef = model.getOrCreateEntityReference(ProteinReference.class, catalyzerName);
//		Protein catalyzer = model.getOrCreatePhysicalEntity(Protein.class, catalyzerName, null, catalyzerRef);
//		
//		model.addNewControl(Catalysis.class, catalyzer, reaction, null);
//	}
//	
//	private void addComplexAssembly(List<String> moleculeNames, ComplexAssemblyType assemblyType) {
//		
//		ComplexAssembly complexAssembly = model.addNewConversion(ComplexAssembly.class);
//		Complex complex = model.getOrCreatePhysicalEntity(Complex.class);
//		
//		SideType moleculeSide = SideType.LEFT;
//		SideType complexSide = SideType.RIGHT;
//		
//		// Association by default
//		if (assemblyType == ComplexAssemblyType.DISSOCIATION) {
//			moleculeSide = SideType.RIGHT;
//			complexSide = SideType.LEFT;
//		}
//		
//		addSideToConversion(complexAssembly, complex, complexSide);
//		
//		for (String moleculeName : moleculeNames) {
//			ProteinReference moleculeRef = model.getOrCreateEntityReference(ProteinReference.class, moleculeName);
//			
//			Protein molecule = model.getOrCreatePhysicalEntity(Protein.class, moleculeName, null, moleculeRef);
//			Protein moleculeOfComplex = model.getOrCreatePhysicalEntity(Protein.class, moleculeName, null, moleculeRef);
//			
//			addSideToConversion(complexAssembly, molecule, moleculeSide);
//			
//			complex.addComponent(moleculeOfComplex);
//		}
//	}
//	Add new small molecules to given side of conversion
//	private void addNewSmallMoleculesToConversion(Conversion conversion, List<EntityModel> moleculeModels, SideType sideType) {
//		for (EntityModel moleculeModel : moleculeModels) {
//			addNewSmallMoleculeToConversion(conversion, moleculeModel, sideType);
//		}
//	}
//	private static enum ComplexAssemblyType {
//		ASSOCIATION,
//		DISSOCIATION
//	}
