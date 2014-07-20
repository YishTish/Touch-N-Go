package com.yishai.sep_patrol;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class LocationController implements LocationListener{
	
	
	private double latitude;
	private double longitude;
	private long timestamp;

	private String locationProvider;
	private String currentProvider;
	
	
	
	
	public LocationController() {
		currentProvider = LocationManager.GPS_PROVIDER;
	}

	@Override
	public void onLocationChanged(Location location) {
		setLongitude(location.getLongitude());
		setLatitude(location.getLatitude());
		setTimestamp(location.getTime());
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
 	}
	
	
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getLocationProvider() {
		return locationProvider;
	}

	public void setLocationProvider(String locationProvider) {
		this.locationProvider = locationProvider;
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if(status == LocationProvider.OUT_OF_SERVICE){
			if(provider.equals(LocationManager.GPS_PROVIDER)){
				currentProvider = LocationManager.NETWORK_PROVIDER;
			}
			else if(provider.equals(LocationManager.NETWORK_PROVIDER)){
				currentProvider = LocationManager.GPS_PROVIDER;
			}
		}
		
	}

}
