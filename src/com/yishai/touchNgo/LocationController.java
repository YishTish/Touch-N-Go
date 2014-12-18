package com.yishai.touchNgo;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class LocationController implements LocationListener{
	
	
	private double latitude;
	private double longitude;
	private long timestamp;

	private String locationProvider;
	private LocationManager locationManager;
	
	
	public LocationController(LocationManager lm) {
		locationManager = lm;
		locationProvider = LocationManager.NETWORK_PROVIDER;
		locationManager.requestLocationUpdates(locationProvider,1000,1000,this);
		pingProvider();
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.e("LocationController", "Location change");
		pingProvider();
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
 	}
	
	//Initiate a check against provider to get the current time and location
	//@TODO: The time retrieval methodology implemented here uses the time on the device. Need
	//		to implement a getTime service which will retrieve time from http://www.earthtools.org/timezone/40.71417/-74.00639
	//	    When implementing that, fall-back to device for cases where there's no data connection.
	public void pingProvider(){
		Log.e("LocationController", "Activated Location change");
		Location loc = locationManager.getLastKnownLocation(locationProvider);
		setLongitude(loc.getLongitude());
		setLatitude(loc.getLatitude());
		setTimestamp(loc.getTime());
		Log.e("LocationController", "Activated Location change. Timestamp: "+getTimestamp());
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
	
	//The core timestamp is saved in milliseconds. We want seconds since 1/1/1970
	public long getTimestampSeconds() {
		return timestamp/1000;
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
		Log.e("LocationManager", "status changes");
		pingProvider();
		if(status == LocationProvider.OUT_OF_SERVICE){
			if(provider.equals(LocationManager.GPS_PROVIDER)){
				provider = LocationManager.NETWORK_PROVIDER;
			}
			else if(provider.equals(LocationManager.NETWORK_PROVIDER)){
				provider = LocationManager.GPS_PROVIDER;
			}
		}
		
	}

}
