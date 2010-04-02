package com.rabenauge.parandroid;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.*;

public class Demo extends GLSurfaceView implements Renderer, OnTouchListener {
    public static final String NAME="ParaNdroiD";

    private Activity activity;

    private SensorManager sm;
    private MediaPlayer mp;

    private Long t_start=null;
    private long t=0, t_credits=0;
    private boolean interactive=false;

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
    private BobsStatic bobs_static;
    private CopperBars bars;
    private Scroller scroller;

    public static final long DURATION_PART_STATIC=10*1000;
    private RorschachFade fade_in_rorschach, fade_out_rorschach;

    public Demo(Activity activity) {
        super(activity);

        this.activity=activity;

        //setDebugFlags(DEBUG_CHECK_GL_ERROR|DEBUG_LOG_GL_CALLS);
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
        setRenderer(this);

        sm=(SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
        setOnTouchListener(this);

        mp=MediaPlayer.create(activity, R.raw.track);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        mp.release();
    }

    public Activity getActivity() {
        return activity;
    }

    public SensorManager getSensorManager() {
        return sm;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Get some OpenGL information.
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
        bobs_static=new BobsStatic(this, (GL11)gl);
        bars=new CopperBars(this, (GL11)gl);
        scroller=new Scroller(this, (GL11)gl);
        fade_in_rorschach=new RorschachFade(this, (GL11)gl, 2*1000, true);
        fade_out_rorschach=new RorschachFade(this, (GL11)gl, 6*1000, false);

        credits=new Credits(this, (GL11)gl);
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
            //mp.start();
            t_start=android.os.SystemClock.uptimeMillis();
        }

        t=android.os.SystemClock.uptimeMillis()-t_start;

        // DEBUG: Uncomment to skip the intro part.
        t+=intro_fade.getDuration();

        // These parts run concurrently and render to the same frame!
        if (intro_fade.play(t)) {
            intro_blink.play(t);
        }
        else {
            // Reset the relative time for this part.
            long tp=t-intro_fade.getDuration();

            gl.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            // These parts run concurrently and render to the same frame!
            stars.play(tp);
            logos.play(tp);
            bobs_static.play(tp);
            bars.play(tp);
            scroller.play(tp);

            // These must come last as they need to render on top of all other effects.
            white_fade_in.play(tp);

            if (!fade_in_rorschach.play(tp)) {
                // Reset the relative time for this part.
                tp-=fade_in_rorschach.getDuration();

                // These must come last as they need to render on top of all other effects.
                if (!fade_out_rorschach.play(tp)) {
                    interactive=true;
                }
            }
        }

        long tc=t+t_credits;
        if (t_credits>0 && tc>DURATION_TOTAL-DURATION_PART_OUTRO) {
            // Because we are still rendering the main effects if we jumped
            // forward in time to the credits, we need to clear the screen for
            // the final fade-out.
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        }
        if (!credits.play(tc)) {
            activity.finish();
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (!interactive || event.getAction()!=MotionEvent.ACTION_DOWN) {
            return false;
        }

        // TODO: Do not hard-code these values, they come from the Milestone.
        final float MAX_X=600, MAX_Y=320;
        float x=Math.max(0, Math.min(event.getX()/MAX_X, 1)), y=Math.max(0, Math.min(event.getY()/MAX_Y, 1));

        if (y<0.5f) {
            if (x<0.33f) {
                Log.i(NAME, "TOUCH: Bobs");

                stars.flight.interactive=false;
                scroller.scroll.interactive=false;
            }
            else if (x<0.66f) {
                Log.i(NAME, "TOUCH: Logo");

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

                t_credits=DURATION_PART_INTRO+DURATION_MAIN_EFFECTS-DURATION_PART_OUTRO_FADE-t;

                stars.flight.interactive=false;
                scroller.scroll.interactive=false;
            }
        }

        return true;
    }
}
