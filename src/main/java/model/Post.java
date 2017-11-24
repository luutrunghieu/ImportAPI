package model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public class Post {
	private ObjectId id;
	private int dead;
	private int injured;
	private List<Integer> vehicles;
	private Place place;
	private Date time;
	private int level;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public int getDead() {
		return dead;
	}
	public void setDead(int dead) {
		this.dead = dead;
	}
	public int getInjured() {
		return injured;
	}
	public void setInjured(int injured) {
		this.injured = injured;
	}
	public List<Integer> getVehicles() {
		return vehicles;
	}
	public void setVehicles(List<Integer> vehicles) {
		this.vehicles = vehicles;
	}
	public Place getPlace() {
		return place;
	}
	public void setPlace(Place place) {
		this.place = place;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		builder.append("\nID: ");
		builder.append(id);
		builder.append("\nPlace: ");
		builder.append(place);
		builder.append("\nTime: ");
		builder.append(time);
		builder.append("\nDead: ");
		builder.append(dead);
		builder.append("\nInjured: ");
		builder.append(injured);
		builder.append("\nVehicles: ");
		builder.append(vehicles);
		builder.append("\nLevel: ");
		builder.append(level);
		builder.append("\n");
		
		return builder.toString();
	}
}
