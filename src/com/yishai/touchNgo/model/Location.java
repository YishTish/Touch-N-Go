package com.yishai.touchNgo.model;

/**
 * Created by mrfonedo on 1/6/15.
 */
public class Location {

    private String key;
    private long longitude;
    private long latitude;
    private String type;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (latitude != location.latitude) return false;
        if (longitude != location.longitude) return false;
        if (!key.equals(location.key)) return false;
        if (type != null ? !type.equals(location.type) : location.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (int) (longitude ^ (longitude >>> 32));
        result = 31 * result + (int) (latitude ^ (latitude >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
