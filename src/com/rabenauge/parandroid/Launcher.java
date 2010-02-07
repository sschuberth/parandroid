package com.rabenauge.parandroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;

public class Launcher extends Activity {
    private Demo demo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        demo=new Demo(this);
        setContentView(demo);
    }

    @Override
    protected void onPause() {
        super.onPause();
        demo.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
