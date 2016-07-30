package methamphibians.tk.simpleruntrack;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class srtTimer extends Service {
    public srtTimer() {
    }
    int number = 0;
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
                makeNotification("Time: " + activity_main.intToTime(number),
                        "Distance: " + String.format("%03.3f",gps.distance));
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
    private void makeNotification(String title, String text) {
        int mId = 1;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, activity_main.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(activity_main.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setOngoing(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }
}
