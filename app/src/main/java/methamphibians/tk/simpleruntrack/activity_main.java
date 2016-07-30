package methamphibians.tk.simpleruntrack;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class activity_main extends AppCompatActivity implements OnClickListener {
    GPSHelper gps;
    boolean started = false;
    Timer t = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
        gps = new GPSHelper(this);


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(myConnection);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (!started) {
                    if(!isMyServiceRunning()) {
                        startService(new Intent(activity_main.this, srtTimer.class));
                    }
                    doBindService();
                    Button b = (Button) findViewById(R.id.button);
                    b.setText("Stop Run");
                    b = (Button) findViewById(R.id.pauseButton);
                    b.setEnabled(true);
                    started = true;
                    t.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String[] getStats = getStats();
                                    int time = Integer.parseInt(getStats[0]);
                                    double distance = Double.parseDouble(getStats[1]);
                                    TextView tv = (TextView) findViewById(R.id.timeView);
                                    tv.setText(intToTime(time));
                                    tv = (TextView) findViewById(R.id.milesView);
                                    tv.setText(String.format("%03.3f",distance));
                                    makeNotification("Time: " + intToTime(time),
                                            "Distance: " + String.format("%03.3f",distance));
                                }
                            });
                        }
                    }, 0, 1000);
                } else if (started) {
                    unbindService(myConnection);
                    stopService(new Intent(activity_main.this, srtTimer.class));
                    resetStats();
                    started = false;
                    t.cancel();
                    t = new Timer();
                    try {
                        gps.locationManager.removeUpdates(gps.locationListener);
                    }
                    catch(SecurityException e) {
                        Log.d("SimpleRunTrack", "Security Exception");
                    }
                    Button b = (Button) findViewById(R.id.button);
                    b.setText("Start Run");
                    b = (Button) findViewById(R.id.pauseButton);
                    b.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }
    public static String intToTime(int time) {
        int strHr = time / 3600;
        int strMin = (time % 3600) / 60;
        int strSec = (time % 3600) % 60;
        String timeValue = String.format("%1$02d:%2$02d:%3$02d", strHr, strMin, strSec);
        return timeValue;
    }

    public String[] getStats() {
        SharedPreferences prefs = getSharedPreferences("SRT", 0);
        return new String[]{ Integer.toString(prefs.getInt("time", 0)),
                String.valueOf(Double.longBitsToDouble(prefs.getLong("distance",0)))
        };
    }
    public void resetStats() {
        SharedPreferences prefs = getSharedPreferences("SRT", 0);
        SharedPreferences.Editor e = prefs.edit();
        e.remove("time");
        e.remove("distance");
        e.commit();
    }

    private srtTimer myServiceBinder;
    public ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            myServiceBinder = ((srtTimer.MyBinder) binder).getService();
            Log.d("ServiceConnection","connected");
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection","disconnected");
            myServiceBinder = null;
        }
    };
    public Handler myHandler = new Handler() {
        public void handleMessage(Message message) {
            Bundle data = message.getData();
        }
    };
    /*protected String[] getServiceData() {
        return myServiceBinder.getStats();
    }*/
    public void doBindService() {
        Intent intent = null;
        intent = new Intent(this, srtTimer.class);
        // Create a new Messenger for the communication back
        // From the Service to the Activity
        Messenger messenger = new Messenger(myHandler);
        intent.putExtra("MESSENGER", messenger);

        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (srtTimer.class.getName().equals(
                    service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
