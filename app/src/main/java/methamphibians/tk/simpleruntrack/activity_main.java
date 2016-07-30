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
        SharedPreferences prefs = getSharedPreferences("SRT", 0);
        ((TextView) findViewById(R.id.timeView)).setText(intToTime(prefs.getInt("time",0)));
        ((TextView) findViewById(R.id.milesView)).setText(String.valueOf(Double.longBitsToDouble(prefs.getLong("distance",0))));
        if(prefs.getInt("time",0) != 0) {
            t.scheduleAtFixedRate(new uiUpdate(), 0, 1000);
            started = true;
            Button b = (Button) findViewById(R.id.button);
            b.setText("Stop Run");
            doBindService();
        }
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
                    t.scheduleAtFixedRate(new uiUpdate(), 0, 1000);
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
    private class uiUpdate extends TimerTask {
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
                    tv = (TextView) findViewById(R.id.paceView);
                    int pace = 0;
                    try {
                        pace = (int) (time / distance);
                    }
                    catch(NullPointerException e) {

                    }
                    tv.setText(intToTime(pace));
                    tv = (TextView) findViewById(R.id.accView);
                    tv.setText(Double.toString(gps.gps_acc));
                }
            });
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


}
