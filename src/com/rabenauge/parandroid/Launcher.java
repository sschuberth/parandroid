package com.rabenauge.parandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;

public class Launcher extends Activity {
    private PowerManager.WakeLock wl;
    private Demo demo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        wl=pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP   |
            PowerManager.ON_AFTER_RELEASE        ,
            "ScreenBrightKeyboardOff"
        );

        demo=new Demo(this);
        setContentView(demo);
    }

    @Override
    protected void onPause() {
        super.onPause();

        demo.onPause();
        wl.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        wl.acquire();
        demo.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure the process is killed immediately to free resources and
        // not make it reusable.
        Process.killProcess(Process.myPid());
    }
}
