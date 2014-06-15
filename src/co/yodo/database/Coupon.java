package co.yodo.database;

public class Coupon {
	private long id;
	private String description;
	private String url;
	private String created;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
	    this.id = id;
	}

	public String getDescription() {
	    return description;
	}

	public void setDescription(String description) {
	    this.description = description;
	}
	
	public String getUrl() {
	    return url;
	}

	public void setUrl(String url) {
	    this.url = url;
	}
	
	public String getCreated() {
	    return created;
	}

	public void setCreated(String created) {
	    this.created = created;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return description + " - " + url;
	}
}
