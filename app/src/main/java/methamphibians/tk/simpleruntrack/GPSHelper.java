package methamphibians.tk.simpleruntrack;

/**
 * Created by jesse on 7/20/16.
 */
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import methamphibians.tk.simpleruntrack.math.math;

public class GPSHelper {
    LocationManager locationManager;
    Context context;
    public double distance = 0.0;
    public double accuracy = 0.0;

    public GPSHelper(Context c) {
        context = c;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    public GPSHelper(Context c, double initDistance) {
        context = c;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        distance = initDistance;
    }

    private static int GPS_FREQ = 1000;
    double latitude = -1;
    double longitude = -1;
    public double gps_acc = 1000;
    public double speed = 0.0;

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            makeUseOfNewLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    private void makeUseOfNewLocation(Location l) {
        if(gps_acc < 30 && latitude != -1) {
            distance += math.calcDistance(latitude, longitude, l.getLatitude(), l.getLongitude());
        }
        latitude = l.getLatitude();
        longitude = l.getLongitude();
        gps_acc = l.getAccuracy();
        speed = l.getSpeed();
    }


    public static int getGpsFreq() {
        return GPS_FREQ;
    }

    public static void setGpsFreq(int f) {
        GPS_FREQ = f;
    }
    public void cancelLocationUpdates() throws SecurityException {
        locationManager.removeUpdates(locationListener);
    }

    public void startLocationUpdates() throws SecurityException {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSHelper.getGpsFreq(), 3, locationListener);
    }
}