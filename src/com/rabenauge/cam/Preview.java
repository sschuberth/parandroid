package com.rabenauge.cam;

import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rabenauge.parandroid.Launcher;

public class Preview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
    Camera cam;
    boolean isPreviewRunning = false;
    Camera.PreviewCallback callback;
    SurfaceHolder mHolder;

    public Preview(Launcher launcher, Camera.PreviewCallback callback) {
        super(launcher);
        this.callback=callback;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        synchronized(this) {
            cam = Camera.open();
            System.out.println("Preview.surfaceCreated()");
            Camera.Parameters para = cam.getParameters();
            para.setPreviewSize(240,160);  //240,160  don't change the values !
            cam.setParameters(para);

            try {
                cam.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e("Camera", "cam.setPreviewDisplay(holder); "+e);
            }

            cam.startPreview();
            cam.setPreviewCallback(this);
        }
        System.out.println("Preview.enclosing_method()");
    }

    public void stopPreview() {
        cam.stopPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized(this) {
            try {
                if (cam!=null) {
                    cam.stopPreview();
                    isPreviewRunning=false;
                    cam.release();
                }
            } catch (Exception e) {
                Log.e("Camera: ", e.getMessage());
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    public void removeCallback(){
        if (mHolder!=null) {
            mHolder.removeCallback(this);
        }
    }

    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        if (callback!=null) {
            callback.onPreviewFrame(arg0, arg1);
        }
    }

    public void startPreview() {
        cam.startPreview();
    }
}
