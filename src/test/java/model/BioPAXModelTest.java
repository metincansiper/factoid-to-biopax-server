package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.junit.Test;

public class BioPAXModelTest {
	
	// Assess private model field and make it accessible
	private static Field getModelField() {
		return getModelField(true);
	}
	
	private static Field getModelField(boolean accessible) {
		Field modelField = null;
		
		try {
			modelField = BioPAXModel.class.getDeclaredField("model");
			modelField.setAccessible(accessible);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return modelField;
	}
	
	// Accessor for private model proterty of given BioPAXModel instance
	private Model getInnerPaxtoolsModel(BioPAXModel biopaxModel) {
		Model innerModel = null;
		
		try {
			innerModel = (Model) modelField.get(biopaxModel);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return innerModel;
	}
	
	@Test
	public void addPhysicalEntityTest() throws NoSuchFieldException, SecurityException {
		
		BioPAXModel model = new BioPAXModel();
		
		// Underlying PAXTools model
		Model innerModel = getInnerPaxtoolsModel(model);
		
		String protName = "TP53";
		String cellularLocationName = "cytoplasm";
		String cellularLocationName2 = "cytoplasm2";
		
		Set<String> modificationTypes = new HashSet<String>();
		modificationTypes.add("active");
		
		Set<String> modificationTypes2 = new HashSet<String>();
		modificationTypes2.add("inactive");
		
		ProteinReference protRef = model.getOrCreateEntityReference(ProteinReference.class, protName);
		CellularLocationVocabulary cellularLocation = model.getOrCreateCellularLocationVocabulary(cellularLocationName);
		CellularLocationVocabulary cellularLocation2 = model.getOrCreateCellularLocationVocabulary(cellularLocationName2);
		
		Protein prot1 = model.getOrCreatePhysicalEntity(Protein.class, protName, cellularLocation, protRef, modificationTypes);
		
		assertTrue("Protein is added to the model", innerModel.contains(prot1));
		assertEquals("Protein name is set", prot1.getDisplayName(), protName);
		assertEquals("Protein cellular location is set", cellularLocation, prot1.getCellularLocation());
		assertEquals("Protein reference is set", protRef, prot1.getEntityReference());
		assertEquals("Protein modification types are set", modificationTypes.size(), prot1.getFeature().size());
		assertEquals("Protein reference has a new modification", 1, protRef.getEntityFeature().size());
		
		Protein prot2 = model.getOrCreatePhysicalEntity(Protein.class, protName, cellularLocation, protRef, modificationTypes);
		assertEquals("No duplication in adding the second Protein with same features", prot1, prot2);
		
		Protein prot3 = model.getOrCreatePhysicalEntity(Protein.class, protName, cellularLocation, protRef);
		assertNotEquals("A new protein is added with no modification", prot1, prot3);
		
		Protein prot4 = model.getOrCreatePhysicalEntity(Protein.class, protName, cellularLocation, protRef, modificationTypes2);
		assertNotEquals("A new protein is added with with different modifications", prot1, prot4);
		assertEquals("Protein reference has a new modification", 2, protRef.getEntityFeature().size());
		
		Protein prot5 = model.getOrCreatePhysicalEntity(Protein.class, protName, cellularLocation2, protRef, modificationTypes);
		assertNotEquals("A new protein is added with with different cellular location", prot1, prot5);
		assertEquals("Protein reference already had this modification", 2, protRef.getEntityFeature().size());
	}
	
	@Test
	public void addEntityReferenceTest() {
		
		BioPAXModel model = new BioPAXModel();
		
		// Underlying PAXTools model
		Model innerModel = getInnerPaxtoolsModel(model);
		
		String commonName = "Protein1";
		String uniqueName = "Protein2";
		
		ProteinReference protRef1 = model.getOrCreateEntityReference(ProteinReference.class, commonName);
		assertTrue("Protein reference is added to the model", innerModel.contains(protRef1));
		assertEquals("Protein reference name is set", commonName, protRef1.getDisplayName());
		
		ProteinReference protRef2 = model.getOrCreateEntityReference(ProteinReference.class, commonName);
		assertEquals("No duplication in adding second protein modification with same name", protRef1, protRef2);
		
		SmallMoleculeReference smRef = model.getOrCreateEntityReference(SmallMoleculeReference.class, commonName);
		assertNotEquals("A new small molecule reference is added that has an existing protein reference name", protRef2, smRef);
		
		ProteinReference protRef3 = model.getOrCreateEntityReference(ProteinReference.class, uniqueName);
		assertNotEquals("A new protein is added with a new name", protRef1, protRef3);
	}
	
	@Test
	public void addCellularLocationVocabularyTest() {
		
		BioPAXModel model = new BioPAXModel();
		
		// Underlying PAXTools model
		Model innerModel = getInnerPaxtoolsModel(model);
		
		String commonLocationName = "location1";
		String uniqueLocationName = "location2";
		
		CellularLocationVocabulary clv1 = model.getOrCreateCellularLocationVocabulary(commonLocationName);
		assertTrue("Cellular location vocabulary is added to the model", innerModel.contains(clv1));
		assertEquals("Cellular location vocabulary has the name", 1, clv1.getTerm().size());
		
		CellularLocationVocabulary clv2 = model.getOrCreateCellularLocationVocabulary(commonLocationName);
		assertEquals("No duplication in adding the second cellular location with the same name", clv1, clv2);
		
		CellularLocationVocabulary clv3 = model.getOrCreateCellularLocationVocabulary(uniqueLocationName);
		assertNotEquals("A new cellular location is added with a new name", clv1, clv3);
	}
	
	@Test
	public void addConversionTest() {
		
		BioPAXModel model = new BioPAXModel();
		
		// Underlying PAXTools model
		Model innerModel = getInnerPaxtoolsModel(model);
		
		ConversionDirectionType dir = ConversionDirectionType.LEFT_TO_RIGHT;
		Protein left = model.addNew(Protein.class);
		Protein right = model.addNew(Protein.class);
		
		Conversion conversion = model.addNewConversion(Conversion.class, left, right, dir);
		assertTrue("Conversion is added to the model", innerModel.contains(conversion));
		assertTrue("Coversions left side is set", conversion.getLeft().contains(left));
		assertTrue("Coversions right side is set", conversion.getRight().contains(right));
		assertEquals("Conversion direction is set", dir, conversion.getConversionDirection());
	}
	
	@Test
	public void addControlTest() {
		
		BioPAXModel model = new BioPAXModel();
		
		// Underlying PAXTools model
		Model innerModel = getInnerPaxtoolsModel(model);
		
		Conversion controlled = model.addNewConversion(Conversion.class);
		Protein controller = model.getOrCreatePhysicalEntity(Protein.class);
		ControlType controlType = ControlType.ACTIVATION;
		
		Control control = model.addNewControl(Control.class, controller, controlled, controlType);
		
		assertTrue("Control is added to the model", innerModel.contains(control));
		assertTrue("Controller is set", control.getController().contains(controller));
		assertTrue("Controlled is set", control.getControlled().contains(controlled));
		assertEquals("Control type is set", controlType, control.getControlType());
	}
	
	private static Field modelField = getModelField();
}
