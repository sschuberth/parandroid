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
    public static final int PRE_WIDTH=240;
    public static final int PRE_HEIGHT=160;

    private Camera cam;
    private Camera.PreviewCallback callback;
    private SurfaceHolder mHolder;

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
                    // This is the default and must be supported on all hardware.
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

            try {
                // We cannot query the supported preview sizes in this API version
                // so just set something and check for the size in effect later.
                para.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
            } catch (IllegalArgumentException e) {
                Log.e("CAMERA", e.getMessage());
            }
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

    public void onPreviewFrame(byte[] data, Camera camera) {
        if (callback!=null) {
            callback.onPreviewFrame(data, camera);
        }
    }

    public void startPreview() {
        if (cam!=null) {
            cam.startPreview();
        }
    }
}
