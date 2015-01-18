package com.yishai.touchNgo.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mrfonedo on 1/6/15.
 */
public class CheckIn {

    private String key;
    private long timestamp;
    private String userKey;
    private String locationKey;
    private String code;
    private String comments;
    private double longitude;
    private double latitude;

    public static final String LOGCAT = "CheckIn Class";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckIn checkIn = (CheckIn) o;

        if (Double.compare(checkIn.latitude, latitude) != 0) return false;
        if (Double.compare(checkIn.longitude, longitude) != 0) return false;
        if (timestamp != checkIn.timestamp) return false;
        if (code != null ? !code.equals(checkIn.code) : checkIn.code != null) return false;
        if (comments != null ? !comments.equals(checkIn.comments) : checkIn.comments != null)
            return false;
        if (key != null ? !key.equals(checkIn.key) : checkIn.key != null) return false;
        if (locationKey != null ? !locationKey.equals(checkIn.locationKey) : checkIn.locationKey != null)
            return false;
        if (userKey != null ? !userKey.equals(checkIn.userKey) : checkIn.userKey != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = key != null ? key.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (userKey != null ? userKey.hashCode() : 0);
        result = 31 * result + (locationKey != null ? locationKey.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public CheckIn(){}

    public CheckIn(JSONObject activityJSON){

        try{
            if(activityJSON.has("key")){
                this.key = activityJSON.getString("key");
            }
            if(activityJSON.has("timestamp")){
                this.timestamp = activityJSON.getLong("timestamp");
            }
            else{
                this.timestamp = System.currentTimeMillis()/1000;
            }
            if(activityJSON.has("userKey")){
                this.userKey = activityJSON.getString("userKey");
            }
            if(activityJSON.has("locationKey")){
                this.locationKey = activityJSON.getString("locationKey");
            }
            if(activityJSON.has("code")){
                this.code = activityJSON.getString("code");
            }
            if(activityJSON.has("comments")){
                this.comments = activityJSON.getString("comments");
            }
            if(activityJSON.has("longitude")){
                this.longitude = activityJSON.getLong("longitude");
            }
            if(activityJSON.has("latitude")){
                this.latitude = activityJSON.getLong("latitude");
            }
         }
        catch(JSONException je){
            Log.e(LOGCAT, "Failed to generate CheckIn class: " + je.getMessage());
        }
    }
}
