package model;

import com.google.gson.JsonObject;

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
	
	public static EntityModel createFromJson(JsonObject jsonObj) {
		if (jsonObj == null) {
			return null;
		}
		
		String name = jsonObj.get("name").getAsString();
		
		XrefModel xref = null;
		
		if (jsonObj.has("xref")) {
			JsonObject xrefObj = jsonObj.get("xref").getAsJsonObject();
			xref = XrefModel.createFromJson(xrefObj);
		}
		
		return new EntityModel(name, xref);
	}
}
