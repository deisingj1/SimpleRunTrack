package methamphibians.tk.simpleruntrack.math;

/**
 * Created by jesse on 8/3/16.
 */
public class math {
    public static double calcDistance(double lat1_d, double lon1_d, double lat2_d, double lon2_d) {
        double lat1 = Math.toRadians(lat1_d);
        double lon1 = Math.toRadians(lon1_d);
        double lat2 = Math.toRadians(lat2_d);
        double lon2 = Math.toRadians(lon2_d);
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a  = Math.pow(Math.sin(dlat/2),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);
        double c  = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a)); // great circle distance in radians
        double d = 3961 * c;
        return d;
    }
    public static String intToTime(long time) {
        long strHr = time / 3600;
        long strMin = (time % 3600) / 60;
        long strSec = (time % 3600) % 60;
        String timeValue = String.format("%1$02d:%2$02d:%3$02d", strHr, strMin, strSec);
        return timeValue;
    }
}
