package model;

public class EntityModel {
	
	private String name;
	private XrefModel xref;
	
	public EntityModel(String name, XrefModel xref) {
		this.name = name;
		this.xref = xref;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setXref(XrefModel xref) {
		this.xref = xref;
	}
	
	public String getName() {
		return name;
	}
	
	public XrefModel getXref() {
		return xref;
	}
}
