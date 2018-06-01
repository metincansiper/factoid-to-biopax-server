package model;

import com.google.gson.JsonObject;

public class XrefModel {
	
	private String id;
	private String namespace;
	
	public XrefModel(String id, String namespace) {
		this.id = id;
		this.namespace = namespace;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getId() {
		return id;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public static XrefModel createFromJson(JsonObject jsonObj) {
		if (jsonObj == null) {
			return null;
		}
		
		String id = jsonObj.get("id").getAsString();
		String namespace = jsonObj.get("namespace").getAsString();
		
		return new XrefModel(id, namespace);
	}
}
