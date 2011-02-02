package com.rabenauge.cam;

import java.io.IOException;

import android.graphics.PixelFormat;
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

            Camera.Parameters para = cam.getParameters();

            int format=para.getPreviewFormat();
            String str="Preview format is ";
            switch (format) {
                case PixelFormat.A_8: {
                    str+="A_8";
                    break;
                }
                case PixelFormat.JPEG: {
                    str+="JPEG";
                    break;
                }
                case PixelFormat.LA_88: {
                    str+="LA_88";
                    break;
                }
                case PixelFormat.L_8: {
                    str+="L_8";
                    break;
                }
                case PixelFormat.OPAQUE: {
                    str+="OPAQUE";
                    break;
                }
                case PixelFormat.RGBA_4444: {
                    str+="RGBA_4444";
                    break;
                }
                case PixelFormat.RGBA_5551: {
                    str+="RGBA_5551";
                    break;
                }
                case PixelFormat.RGBA_8888: {
                    str+="RGBA_8888";
                    break;
                }
                case PixelFormat.RGBX_8888: {
                    str+="RGBX_8888";
                    break;
                }
                case PixelFormat.RGB_332: {
                    str+="RGB_332";
                    break;
                }
                case PixelFormat.RGB_565: {
                    str+="RGB_565";
                    break;
                }
                case PixelFormat.RGB_888: {
                    str+="RGB_888";
                    break;
                }
                case PixelFormat.TRANSLUCENT: {
                    str+="TRANSLUCENT";
                    break;
                }
                case PixelFormat.TRANSPARENT: {
                    str+="TRANSPARENT";
                    break;
                }
                case PixelFormat.UNKNOWN: {
                    str+="UNKNOWN";
                    break;
                }
                case PixelFormat.YCbCr_420_SP: {
                    str+="YCbCr_420_SP (NV21)";
                    break;
                }
                case PixelFormat.YCbCr_422_SP: {
                    str+="YCbCr_422_SP (NV16)";
                    break;
                }
            }
            str+=" using ";
            PixelFormat info=new PixelFormat();
            PixelFormat.getPixelFormatInfo(format,info);
            str+=String.valueOf(info.bitsPerPixel) + " bits per pixel";
            str+=" / " + String.valueOf(info.bytesPerPixel) + " byte(s) per pixel";
            Log.i("CAMERA",str);

            para.setPreviewSize(240, 160);  //240,160  don't change the values !
            cam.setParameters(para);

            try {
                cam.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e("CAMERA", e.getMessage());
            }

            cam.startPreview();
            cam.setPreviewCallback(this);
        }
    }

    public void stopPreview() {
        if (cam!=null) {
            cam.stopPreview();
        }
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
                Log.e("CAMERA", e.getMessage());
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
        if (cam!=null) {
            cam.startPreview();
        }
    }
}
