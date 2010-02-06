package com.rabenauge.parandroid;

import android.app.Activity;
import android.os.Bundle;

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
}
