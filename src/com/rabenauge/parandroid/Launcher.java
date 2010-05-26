package com.rabenauge.parandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.view.ViewGroup.LayoutParams;

import com.rabenauge.cam.Preview;

public class Launcher extends Activity {
    private PowerManager.WakeLock wl;
    private Demo demo;

    private Preview preview;
    private Runnable showCamPreview = new Runnable() {
        public void run() {
            addContentView(preview, new LayoutParams(100, 100));
        }
    };

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
        preview=new Preview(this, demo);

        setContentView(demo);
    }

    @Override
    protected void onPause() {
        super.onPause();

        preview.removeCallback();
        demo.onPause();
        wl.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        wl.acquire();
        demo.onResume();
    }

    public void showPreview() {
        runOnUiThread(showCamPreview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure the process is killed immediately to free resources and
        // not make it reusable.
        Process.killProcess(Process.myPid());
    }
}
