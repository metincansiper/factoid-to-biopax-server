package factoid.model;

public class XrefModel {
	
	private String id;
	private String db;
	
	public XrefModel(String id, String db) {
		this.id = id;
		this.db = db;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setDb(String db) {
		this.db = db;
	}
	
	public String getId() {
		return id;
	}
	
	public String getDb() {
		return db;
	}
}
