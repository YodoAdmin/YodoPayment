package co.yodo.mobile.model.db;

import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Coupon extends SugarRecord {
	/** Date format */
	private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** Main attributes */
	private String description;
	private String url;
	private String created;

	@SuppressWarnings("unused")
	public Coupon() {}

	public Coupon( String url, String description ) {
		// Date formatter
		SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT, Locale.US );

		// Set coupon data
		this.url = url;
		this.description = description;
		this.created = sdf.format( new Date() );
	}

	public String getDescription() {
	    return description;
	}

	public void setDescription( String description ) {
	    this.description = description;
	}
	
	public String getUrl() {
	    return url;
	}
	
	public String getCreated() {
	    return created;
	}

	public void setCreated( String created ) {
	    this.created = created;
	}

	@Override
	public String toString() {
		return description + " - " + url;
	}
}
