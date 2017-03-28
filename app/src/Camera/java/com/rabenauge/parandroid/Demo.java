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
import android.hardware.*;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.*;

import com.rabenauge.cam.Preview;

public class Demo extends GLSurfaceView implements Renderer, Camera.PreviewCallback {
    public static final String NAME="ParaNdroiD";

    private Launcher activity;

    private MediaPlayer mp;
    private SensorManager sm;

    private Long t_start=null, t_start_cube=null;
    private long t_global=0, t_credits=0;

    private boolean interactive=false;
    public boolean shootem=false;
    private int shootem_counter=0;

    // The song has a duration of 5:28m (328s).
    public static final long DURATION_TOTAL=328*1000;

    // The song's "woosh" is approx. at 43s.
    public static final long DURATION_PART_INTRO=43500;
    private IntroFade intro_fade;
    private IntroBlink intro_blink;

    public static final long DURATION_PART_OUTRO=40*1000;
    public static final long DURATION_PART_OUTRO_FADE=6*1000;
    private Credits credits;

    public static final long DURATION_MAIN_EFFECTS=DURATION_TOTAL-DURATION_PART_INTRO-DURATION_PART_OUTRO;
    private WhiteFadeIn white_fade_in;
    private StarField stars;
    private LogoChange logos;
    private Bobs bobs;
    private CopperBars bars;
    private Scroller scroller;

    private CamCube cube;
    private boolean cube_zoom=true;

    private int cam_pre_width=-1;
    private int cam_pre_height=-1;

    public static final long DURATION_PART_STATIC=20*1000;
    private RorschachFade fade_in_rorschach, fade_out_rorschach;

    public Demo(Context context) {
        super(context);
    }

    public Demo(Launcher activity) {
        this((Context)activity);
        this.activity=activity;

        // Make sure we get a depth buffer. This is also required to get hardware acceleration on the Samsung Galaxy, see
        // http://www.anddev.org/viewtopic.php?p=29081#29081
        setEGLConfigChooser(new EGLConfigChooser() {
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                int[] attributes={
                     EGL10.EGL_DEPTH_SIZE,
                     16,
                     EGL10.EGL_NONE
                };
                EGLConfig[] configs=new EGLConfig[1];
                int[] result=new int[1];
                egl.eglChooseConfig(display, attributes, configs, 1, result);
                return configs[0];
           }
        });

        // This is required to receive onKeyDown() events.
        setFocusableInTouchMode(true);

        // DEBUG: Comment-in to see OpenGL calls in the log.
        //setDebugFlags(DEBUG_CHECK_GL_ERROR|DEBUG_LOG_GL_CALLS);
        setRenderer(this);

        sm=(SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
        mp=MediaPlayer.create(activity, R.raw.track);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        mp.release();
    }

    public Activity getActivity() {
        return activity;
    }

    public MediaPlayer getMediaPlayer() {
        return mp;
    }

    public SensorManager getSensorManager() {
        return sm;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Get some Android device information.
        Log.i(NAME, "DEVICE        : "    + android.os.Build.DEVICE);
        Log.i(NAME, "MODEL         : "    + android.os.Build.MODEL);
        Log.i(NAME, "PRODUCT       : "    + android.os.Build.PRODUCT);

        Log.i(NAME, "GL_VENDOR     : "    + gl.glGetString(GL10.GL_VENDOR));
        Log.i(NAME, "GL_RENDERER   : "    + gl.glGetString(GL10.GL_RENDERER));
        Log.i(NAME, "GL_VERSION    : "    + gl.glGetString(GL10.GL_VERSION));
        Log.i(NAME, "GL_EXTENSIONS :\n  " + gl.glGetString(GL10.GL_EXTENSIONS).trim().replace(" ", "\n  "));

        int[] params=new int[2];

        gl.glGetIntegerv(GL10.GL_DEPTH_BITS, params, 0);
        Log.i(NAME, "GL_DEPTH_BITS               : " + String.valueOf(params[0]));
        gl.glGetIntegerv(GL10.GL_STENCIL_BITS, params, 0);
        Log.i(NAME, "GL_STENCIL_BITS             : " + String.valueOf(params[0]));

        gl.glGetIntegerv(GL10.GL_MAX_LIGHTS, params, 0);
        Log.i(NAME, "GL_MAX_LIGHTS               : " + String.valueOf(params[0]));
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, params, 0);
        Log.i(NAME, "GL_MAX_TEXTURE_SIZE         : " + String.valueOf(params[0]));
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_UNITS, params, 0);
        Log.i(NAME, "GL_MAX_TEXTURE_UNITS        : " + String.valueOf(params[0]));

        gl.glGetIntegerv(GL10.GL_ALIASED_LINE_WIDTH_RANGE, params, 0);
        Log.i(NAME, "GL_ALIASED_LINE_WIDTH_RANGE : " + String.valueOf(params[0]) + ", " + String.valueOf(params[1]));
        gl.glGetIntegerv(GL10.GL_SMOOTH_LINE_WIDTH_RANGE, params, 0);
        Log.i(NAME, "GL_SMOOTH_LINE_WIDTH_RANGE  : " + String.valueOf(params[0]) + ", " + String.valueOf(params[1]));

        gl.glGetIntegerv(GL10.GL_ALIASED_POINT_SIZE_RANGE, params, 0);
        Log.i(NAME, "GL_ALIASED_POINT_SIZE_RANGE : " + String.valueOf(params[0]) + ", " + String.valueOf(params[1]));
        gl.glGetIntegerv(GL10.GL_SMOOTH_POINT_SIZE_RANGE, params, 0);
        Log.i(NAME, "GL_SMOOTH_POINT_SIZE_RANGE  : " + String.valueOf(params[0]) + ", " + String.valueOf(params[1]));

        if (gl instanceof GL11) {
            Log.i(NAME, "Implements GL11");
        }
        else {
            Log.i(NAME, "Implements GL10");

            Log.e(NAME, "No GL11 available");
            activity.finish();
            return;
        }

        // Get some sensor information.
        List<Sensor> sensors=sm.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor:sensors) {
            Log.i(NAME, "Sensor: " + sensor.getName() + ", " + sensor.getVendor());
        }

        intro_fade=new IntroFade(this, (GL11)gl);
        intro_blink=new IntroBlink(this, (GL11)gl);

        white_fade_in=new WhiteFadeIn(this, (GL11)gl, 2*1000);
        stars=new StarField(this, (GL11)gl, 400);
        logos=new LogoChange(this, (GL11)gl, 40, 20, 8000, 2000);
        bobs=new Bobs(this, (GL11)gl);
        bars=new CopperBars(this, (GL11)gl);
        scroller=new Scroller(this, (GL11)gl);
        fade_in_rorschach=new RorschachFade(this, (GL11)gl, 2*1000, true);
        fade_out_rorschach=new RorschachFade(this, (GL11)gl, 6*1000, false);

        credits=new Credits(this, (GL11)gl);
        cube=new CamCube(this, (GL11)gl);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Adjust the viewport.
        gl.glViewport(0, 0, width, height);

        // Adjust the projection matrix.
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)width/height, 0.01f, 100.0f);
    }

    public void onDrawFrame(GL10 gl) {
        if (t_start==null) {
            // DEBUG: Comment-out to not play the music.
            mp.start();

            // Set to full volume initially (wraps at modulo 16).
            mp.setVolume(15.0f, 15.0f);

            t_start=android.os.SystemClock.uptimeMillis();
        }
        t_global=android.os.SystemClock.uptimeMillis()-t_start;

        // DEBUG: Comment-in to skip the intro part.
        //t_global+=intro_fade.getDuration();

        gl.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

        // Do not query the stars visibility as it is too inaccurate (worked on the Hero, but not on the Milestone).
        if (bobs.isHidden() && bars.isHidden() && logos.isHidden() && scroller.isHidden() /*&& stars.isHidden()*/) {
            if (t_start_cube==null) {
                t_start_cube=t_global;
            }
            long t_cube=t_global-t_start_cube;

            if (cube_zoom && t_cube>CamCube.DURATION_PART_TRANSITION) {
                t_cube=CamCube.DURATION_PART_TRANSITION;
            }

            if (cube.play(t_cube)) {
                return;
            }
            else {
                shootem=false;
                t_start_cube=null;
                activity.stopPreview();
            }
        }

        long tc=t_global+t_credits;

        // These parts run concurrently and render to the same frame!
        if (intro_fade.play(t_global)) {
            intro_blink.play(t_global);
        }
        else {
            // Reset the relative time for this part.
            long t_main=t_global-intro_fade.getDuration();

            if (t_credits==0 || tc<=DURATION_TOTAL-DURATION_PART_OUTRO) {
                // These parts run concurrently and render to the same frame!
                stars.play(t_main);
                logos.play(t_main);
                bobs.play(t_main);
                bars.play(t_main);
                scroller.play(t_main);

                // These must come last as they need to render on top of all other effects.
                white_fade_in.play(t_main);
            }

            if (!fade_in_rorschach.play(t_main)) {
                // Reset the relative time for this part.
                t_main-=fade_in_rorschach.getDuration();

                // These must come last as they need to render on top of all other effects.
                if (!fade_out_rorschach.play(t_main)) {
                    interactive=true;
                }
                else {
                    bobs.toggleBobs(true);
                }
            }
        }

        if (!credits.play(tc)) {
            activity.finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(NAME, "KEY: "+String.valueOf(keyCode));

        if (!interactive || event.getAction()!=KeyEvent.ACTION_DOWN) {
            return false;
        }

        if (keyCode==KeyEvent.KEYCODE_CAMERA
         || keyCode==KeyEvent.KEYCODE_DPAD_CENTER // Also allow to use the trackball button (e.g. for the HTC Hero).
         || keyCode==KeyEvent.KEYCODE_SEARCH      // Also allow to use the search button (e.g. for the Samsung Galaxy Tab).
        ) {
            if (!shootem) {
                cube_zoom=true;
                shootem=true;
                if (shootem_counter==0) {
                    activity.showPreview();
                }
                else {
                    activity.startPreview();
                }
                ++shootem_counter;
            }
            else {
                cube_zoom=false;
                t_start_cube=t_global-CamCube.DURATION_PART_TRANSITION;
            }
            return true;
        }

        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!interactive || event.getAction()!=MotionEvent.ACTION_DOWN) {
            return false;
        }

        // Normalize the event coordinates (precision seems to always be 0).
        float x=event.getX(), xp=event.getXPrecision();
        if (xp!=0) {
            x*=xp;
        }
        x/=getWidth();

        float y=event.getY(), yp=event.getYPrecision();
        if (yp!=0) {
            y*=yp;
        }
        y/=getHeight();

        if (y<0.5f) {
            if (x<0.33f) {
                Log.i(NAME, "TOUCH: Bobs");

                bobs.toggleBobs();

                stars.flight.interactive=false;
                scroller.scroll.interactive=false;
            }
            else if (x<0.66f) {
                Log.i(NAME, "TOUCH: Logo");

                logos.changeNow();

                stars.flight.interactive=false;
                scroller.scroll.interactive=false;
            }
            else {
                Log.i(NAME, "TOUCH: Stars");

                stars.flight.interactive=!stars.flight.interactive;
                scroller.scroll.interactive=false;
            }
        }
        else {
            if (x<0.5f) {
                Log.i(NAME, "TOUCH: Scroller");

                stars.flight.interactive=false;
                scroller.scroll.interactive=!scroller.scroll.interactive;
            }
            else {
                Log.i(NAME, "TOUCH: Exit");

                t_credits=DURATION_PART_INTRO+DURATION_MAIN_EFFECTS-DURATION_PART_OUTRO_FADE-t_global;

                stars.flight.interactive=false;
                scroller.scroll.interactive=false;
            }
        }

        return true;
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        if (cube!=null) {
            if (cam_pre_width<0 || cam_pre_height<0) {
                // Get the camera preview size in effect.
                Camera.Parameters params=camera.getParameters();
                Camera.Size size=params.getPreviewSize();
                cam_pre_width=size.width;
                cam_pre_height=size.height;
            }

            int bwCounter=0;
            int yuvsCounter=0;

            for (int y=0; y<Preview.PRE_HEIGHT; ++y) {
                System.arraycopy(data, yuvsCounter, cube.camFrame, bwCounter, Preview.PRE_WIDTH);

                yuvsCounter=yuvsCounter+cam_pre_width;
                bwCounter=bwCounter+CamCube.TEX_WIDTH;
            }
        }
    }
}
