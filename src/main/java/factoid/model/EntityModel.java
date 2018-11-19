package factoid.model;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;

public class EntityModel {
	
	private String name;
	private XrefModel xref;
	private String type;
	
	public EntityModel(String name, XrefModel xref, String type) {
		this.name = name;
		this.xref = xref;
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setXref(XrefModel xref) {
		this.xref = xref;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public XrefModel getXref() {
		return xref;
	}
	
	public String getType() {
		return type;
	}
	
	public Class<? extends PhysicalEntity> getEntityClass() {
		if ("protein".equals(type)) {
			return Protein.class;
		}
		else if("chemical".equals(type)) {
			return SmallMolecule.class;
		}
		else {
			return PhysicalEntity.class;
		}
	}
	
	public Class<? extends EntityReference> getEntityRefClass() {
		if (type.equals("protein")) {
			return ProteinReference.class;
		}
		else if(type.equals("chemical")) {
			return SmallMoleculeReference.class;
		}
		else {
			return EntityReference.class;
		}
	}
}
