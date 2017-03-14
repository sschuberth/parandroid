/*
 * Copyright 2010-2011 bodo, eyebex, ralph, spotter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rabenauge.parandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        wl=pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP |
            PowerManager.ON_AFTER_RELEASE      ,
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

    public void stopPreview() {
        preview.stopPreview();
    }

    public void startPreview() {
        preview.startPreview();
    }
}
