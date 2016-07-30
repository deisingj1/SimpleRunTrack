package methamphibians.tk.simpleruntrack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class srtTimer extends Service {
    public srtTimer() {
    }
    int number = 0;
    double distance = 0;
    SharedPreferences prefs;
    SharedPreferences.Editor e;
    GPSHelper gps;
    Timer t = new Timer();

    private final IBinder mBinder = new MyBinder();
    private Messenger outMessenger;

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d("service","onBind");
        // Get messager from the Activity
        if (extras != null) {
            Log.d("service","onBind with extra");
            outMessenger = (Messenger) extras.get("MESSENGER");
        }
        return mBinder;
    }
    public class MyBinder extends Binder {
        srtTimer getService() {
            return srtTimer.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = getApplicationContext().getSharedPreferences("SRT", 0);
        e = prefs.edit();
        gps = new GPSHelper(this, Double.longBitsToDouble(prefs.getLong("distance", 0)));
        gps.startLocationUpdates();
        number = prefs.getInt("time", 0);
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runStats();
            }
        },0,1000);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        t.cancel();
    }
    private void runStats(){
        System.out.println("SRTTIMER: " + number + " Distance " + gps.distance);
        e.putInt("time",number++);
        e.putLong("distance", Double.doubleToRawLongBits(gps.distance));
        e.commit();
    }
}
