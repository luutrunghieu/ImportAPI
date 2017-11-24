package model;

public class Place {
	private String raw;
	private String city;
	private String lat;
	private String lng;
	
	public String getRaw() {
		return raw;
	}
	public void setRaw(String raw) {
		this.raw = raw;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		builder.append("\n\t\tRaw: ");
		builder.append(raw);
		builder.append("\n\t\tCity: ");
		builder.append(city);
		builder.append("\n\t\tLat: ");
		builder.append(lat);
		builder.append("\n\t\tLong: ");
		builder.append(lng);
		return builder.toString();
	}
}
