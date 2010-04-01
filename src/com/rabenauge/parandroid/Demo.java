package com.rabenauge.parandroid;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.PowerManager;
import android.util.Log;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.*;

public class Demo extends GLSurfaceView implements Renderer {
    public static final String NAME="ParaNdroiD";

    private Activity activity;

    private PowerManager.WakeLock wl;
    private SensorManager sm;
    private MediaPlayer mp;

    private Long t_start;

    public static final long DURATION_PART_INTRO=43500;
    private IntroFade intro_fade;
    private IntroBlink intro_blink;

    public static final long DURATION_PART_STATIC=30000;
    private ColorFade fade_in_white;
    private StarField stars;
    private LogoChange logos;
    private BobsStatic bobs_static;
    private CopperBars bars;
    private Scroller scroller;

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

        PowerManager pm=(PowerManager)activity.getSystemService(Context.POWER_SERVICE);
        wl=pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP   |
            PowerManager.ON_AFTER_RELEASE        ,
            NAME
        );
        wl.acquire();

        sm=(SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);

        mp=MediaPlayer.create(activity, R.raw.track);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        mp.release();
        wl.release();
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

        fade_in_white=new ColorFade(this, (GL11)gl, 1000, true, 1, 1, 1);
        stars=new StarField(this, (GL11)gl, 400);
        logos=new LogoChange(this, (GL11)gl, 40, 20, 8000, 2000);
        bobs_static=new BobsStatic(this, (GL11)gl);
        bars=new CopperBars(this, (GL11)gl);
        scroller=new Scroller(this, (GL11)gl);
    }

    public Activity getActivity() {
        return activity;
    }

    public SensorManager getSensorManager() {
        return sm;
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
            mp.start();
            t_start=android.os.SystemClock.uptimeMillis();
        }

        long t=android.os.SystemClock.uptimeMillis()-t_start;

        // These parts run concurrently and render to the same frame!
        if (intro_fade.play(t)) {
            intro_blink.play(t);
        }
        else {
            // Reset the relative time for this part.
            t-=intro_fade.getDuration();

            gl.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            // These parts run concurrently and render to the same frame!
            stars.play(t);
            logos.play(t);
            bobs_static.play(t);
            bars.play(t);
            scroller.play(t);

            // This must come last as it needs to render on top of all other effects.
            fade_in_white.play(t);
        }
    }
}
