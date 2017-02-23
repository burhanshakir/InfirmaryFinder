package gcm.burhan.android.infirmaryfinder.model;

/**
 * Created by burha on 23-02-2017.
 */

public class Hospital {

    private String name;
    private String vicinity;
    private double latitude;
    private double longitude;

    public Hospital(String name, String vicinity, double latitude, double longitude) {
        this.name = name;
        this.vicinity = vicinity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Hospital() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
