package factoid.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.SmallMolecule;
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

	public void addControlsState(EntityModel controllerModel, EntityModel targetModel, String modification, ControlType controlType) {
		
		Set<String> leftMFTypes = new HashSet<String>();
		Set<String> rightMFTypes = new HashSet<String>();
		Set<String> leftNotMFTypes = new HashSet<String>();
		Set<String> rightNotMFTypes = new HashSet<String>();
		
		if (modification != null) {
			leftNotMFTypes.add(modification);
			rightMFTypes.add(modification);
		}
		
		addActiveNotActiveFeatures(controlType, leftMFTypes, rightMFTypes, leftNotMFTypes, rightNotMFTypes);
		
		String targetName = targetModel.getName();
		XrefModel targetXref = targetModel.getXref();
		
		String controllerName = controllerModel.getName();
		XrefModel controllerXref = controllerModel.getXref();
		
		Class<? extends PhysicalEntity> targetClass = targetModel.getEntityClass();
		Class<? extends EntityReference> targetRefClass = targetModel.getEntityRefClass();
		
		EntityReference targetRef = model.getOrCreateEntityReference(targetRefClass, targetName, targetXref);
		PhysicalEntity left = model.getOrCreatePhysicalEntity(targetClass, targetName, null, targetRef, leftMFTypes, leftNotMFTypes);
		PhysicalEntity right = model.getOrCreatePhysicalEntity(targetClass, targetName, null, targetRef, rightMFTypes, rightNotMFTypes);
		
		Class<? extends PhysicalEntity> controllerClass = targetModel.getEntityClass();
		Class<? extends EntityReference> controllerRefClass = targetModel.getEntityRefClass();
		
		EntityReference controllerRef = model.getOrCreateEntityReference(controllerRefClass, controllerName, controllerXref);
		PhysicalEntity controller = model.getOrCreatePhysicalEntity(controllerClass, controllerName, null, controllerRef);
		
		Conversion conversion = model.addNewConversion(Conversion.class, left, right);
		
		if (controllerClass == Protein.class) {	
			model.addNewControl(Catalysis.class, controller, conversion, ControlType.ACTIVATION);
		}
		else if (controllerClass == SmallMolecule.class) {
			Catalysis catalysis = model.addNewControl(Catalysis.class, null, conversion, ControlType.ACTIVATION);
			model.addNewControl(Modulation.class, controller, catalysis, ControlType.ACTIVATION);
		}
	}
	
	public void addMolecularInteraction(List<EntityModel> participantModels) {
		addInteractionWithParticipants(MolecularInteraction.class, participantModels);
	}
	
	public void addInteraction(List<EntityModel> participantModels) {
		addInteractionWithParticipants(Interaction.class, participantModels);
	}
	
	public void addExpressionRegulation(EntityModel controllerModel, EntityModel targetModel, ControlType controlType) {
		
		String controllerName = controllerModel.getName();
		XrefModel controllerXref = controllerModel.getXref();
		
		String targetName = targetModel.getName();
		XrefModel targetXref = targetModel.getXref();
		
		Class<? extends EntityReference> targetRefClass = targetModel.getEntityRefClass();
		Class<? extends EntityReference> controllerRefClass = targetModel.getEntityRefClass();
		Class<? extends PhysicalEntity> targetClass = targetModel.getEntityClass();
		Class<? extends PhysicalEntity> controllerClass = targetModel.getEntityClass();
		
		EntityReference targetRef = model.getOrCreateEntityReference(targetRefClass, targetName, targetXref);
		EntityReference controllerRef = model.getOrCreateEntityReference(controllerRefClass, controllerName, controllerXref);
		
		PhysicalEntity controller = model.getOrCreatePhysicalEntity(controllerClass, controllerName, null, controllerRef);
		PhysicalEntity product = model.getOrCreatePhysicalEntity(targetClass, targetName, null, targetRef);
		
		TemplateReaction reaction = model.addNew(TemplateReaction.class);
		reaction.addProduct(product);
		
		model.addNewControl(TemplateReactionRegulation.class, controller, reaction, controlType);
	}
	
	public void addControlSequence(EntityModel entityModel1, EntityModel entityModel2, ControlType controlType) {
		String name1 = entityModel1.getName();
		String name2 = entityModel2.getName();
		XrefModel xref1 = entityModel1.getXref();
		XrefModel xref2 = entityModel2.getXref();
		
		Class<? extends PhysicalEntity> entityClass1 = entityModel1.getEntityClass();
		Class<? extends EntityReference> entityRefClass1 = entityModel1.getEntityRefClass();
		Class<? extends PhysicalEntity> entityClass2 = entityModel2.getEntityClass();
		Class<? extends EntityReference> entityRefClass2 = entityModel2.getEntityRefClass();
		
		EntityReference entityRef1 = model.getOrCreateEntityReference(entityRefClass1, name1, xref1);
		EntityReference entityRef2 = model.getOrCreateEntityReference(entityRefClass2, name2, xref2);
		
		PhysicalEntity entity1 = model.getOrCreatePhysicalEntity(entityClass1, name1, null, entityRef1);
		PhysicalEntity entity2 = model.getOrCreatePhysicalEntity(entityClass2, name2, null, entityRef2);
		
		// Second entity is controller in somewhere where controlled is unknown
		// First entity controls the interaction above
		Control controlled = model.addNewControl(Control.class, entity2, null, controlType);
		model.addNewControl(Control.class, entity1, controlled, controlType);
	}
	
	public void addControlsConsumptionOrProduction(EntityModel controllerModel, EntityModel targetModel, ControlType controlType) {
		String controllerName = controllerModel.getName();
		XrefModel controllerXref = controllerModel.getXref();
		Class<? extends PhysicalEntity> controllerClass = controllerModel.getEntityClass();
		Class<? extends EntityReference> controllerRefClass = controllerModel.getEntityRefClass();
		SideType targetSide = controlType.equals(ControlType.ACTIVATION) ? SideType.RIGHT : SideType.LEFT;
		
		BiochemicalReaction reaction = model.addNewConversion(BiochemicalReaction.class);
		
		addNewEntityToConversion(reaction, targetModel, targetSide);
		
		EntityReference catalyzerRef = model.getOrCreateEntityReference(controllerRefClass, controllerName, controllerXref);
		PhysicalEntity catalyzer = model.getOrCreatePhysicalEntity(controllerClass, controllerName, null, catalyzerRef);
		
		model.addNewControl(Catalysis.class, catalyzer, reaction, null);
	}
	
	public void addConversion(EntityModel srcModel, EntityModel tgtModel, ControlType controlType) {
		Conversion conversion = model.addNewConversion(Conversion.class);
		SideType srcSide = SideType.LEFT;
		SideType tgtSide = SideType.RIGHT;
		
		// if controlType is inhibition switch the sides
		if (controlType == ControlType.INHIBITION) {
			srcSide = SideType.RIGHT;
			tgtSide = SideType.LEFT;
		}
		
		addNewEntityToConversion(conversion, srcModel, srcSide);
		addNewEntityToConversion(conversion, tgtModel, tgtSide);
	}
	
	// accessors
	
	public String convertToOwl() {
		return model.convertToOwl();
	}
	
	// Section: private helper methods
	
	private <T extends Interaction> void addInteractionWithParticipants(Class<T> c, List<EntityModel> participantModels) {
		T interaction = model.addNew(c);
		
		for(EntityModel participantModel : participantModels) {
			String participantName = participantModel.getName();
			XrefModel participantXref = participantModel.getXref();
			
			Class<? extends PhysicalEntity> participantClass = participantModel.getEntityClass();
			Class<? extends EntityReference> participantRefClass = participantModel.getEntityRefClass();
			
			EntityReference participantRef = model.getOrCreateEntityReference(participantRefClass, participantName, participantXref);
			PhysicalEntity participant = model.getOrCreatePhysicalEntity(participantClass, participantName, null, participantRef);
			interaction.addParticipant(participant);
		}
	}
	
	// Add a new entity to the given side of conversion
	private void addNewEntityToConversion(Conversion conversion, EntityModel entityModel, SideType sideType) {
		String entityName = entityModel.getName();
		XrefModel entityXref = entityModel.getXref();
		Class<? extends EntityReference> entityRefClass = entityModel.getEntityRefClass();
		Class<? extends PhysicalEntity> entityClass = entityModel.getEntityClass();
		
		EntityReference entityRef = model.getOrCreateEntityReference(entityRefClass, entityName, entityXref);
		PhysicalEntity entity = model.getOrCreatePhysicalEntity(entityClass, entityName, null, entityRef);
		addSideToConversion(conversion, entity, sideType);
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
	
	// Update types set by adding ACTIVE type or notType to the sides according to control type
	private static void addActiveNotActiveFeatures(ControlType controlType, Set<String> leftTypes, Set<String> rightTypes, Set<String> leftNotTypes, Set<String> rightNotTypes){
		if ( controlType == ControlType.ACTIVATION ) {
			rightTypes.add(ACTIVE);
			leftNotTypes.add(ACTIVE);
		}
		else if (controlType == ControlType.INHIBITION) {
			leftTypes.add(ACTIVE);
			rightNotTypes.add(ACTIVE);
		}
	}
	
	// Section: static variables
	
	private static final String ACTIVE = "active";
	
	private static enum SideType {
		LEFT,
		RIGHT
	}
}
