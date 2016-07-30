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
import android.widget.TextView;

public class GPSHelper {
    LocationManager locationManager;
    Context context;
    public double distance = 0.0;


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
    static double latitude = -1;
    static double longitude = -1;
    double gps_acc = 1000;

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
            distance += calcDistance(l.getLatitude(), l.getLongitude());
        }
        latitude = l.getLatitude();
        longitude = l.getLongitude();
        gps_acc = l.getAccuracy();
        /*Activity a = (Activity) context;
        TextView tv = (TextView) a.findViewById(R.id.milesView);
        tv.setText(String.format("%03.3f",distance));
        System.out.println("GPS MakeUseOf");
        tv = (TextView) a.findViewById(R.id.accView);
        tv.setText(String.format("%f",gps_acc));
        tv = (TextView) a.findViewById(R.id.paceView);
        //FIXME fix pace
        int pace = (int) (50 / distance);
        String timeValue = activity_main.intToTime(pace);
        tv.setText(timeValue);
        */
    }

    public double calcDistance(double lat2_d, double lon2_d) {
        double lat1 = Math.toRadians(latitude);
        double lon1 = Math.toRadians(longitude);
        double lat2 = Math.toRadians(lat2_d);
        double lon2 = Math.toRadians(lon2_d);
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a  = Math.pow(Math.sin(dlat/2),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);
        double c  = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a)); // great circle distance in radians
        double d = 3961 * c;
        return d;
    }
    public static int getGpsFreq() {
        return GPS_FREQ;
    }

    public static void setGpsFreq(int f) {
        GPS_FREQ = f;
    }

    public void startLocationUpdates() throws SecurityException {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSHelper.getGpsFreq(), 0, locationListener);
    }
}