package models;

import java.util.Date;

public class POI {
	private final Integer id;
	private final Double latitude;
	private final Double longitude;
	private final String city;
	private final String country;
	private final String description;
	private final Date updatedDate;

	public POI(Integer id, Double latitude , Double longitude, String city, String country, String description, Date updatedDate) {
		super();
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.city = city;
		this.country = country;
		this.description = description;
		this.updatedDate = updatedDate;
	}

	public Integer getId() {
		return id;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getCity() {
		return city;
	}
	public String getCountry() {
		return country;
	}

	public String getDescription() {
		return description;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}
	

	@Override
	public String toString() {
		return "id=" + id + ", latitude=" + latitude + ", longitude=" + longitude + ", city='" + city + '\''
				+", country='" + country + '\''
				+ ", description='" + description + '\'' + ", updatedDate=" + updatedDate + '}';
	}

}
