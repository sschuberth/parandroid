package com.rabenauge.parandroid;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.PowerManager;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.EGLConfig;

public class Demo extends GLSurfaceView implements Renderer {
    private static final String TAG="ParaNdroiD";

    private Activity activity;
    private PowerManager.WakeLock wl;
    private MediaPlayer mp;
    private Long t_start;

    private IntroFade intro_fade;
    private IntroBlink intro_blink;

    private ColorFade fade_in_white;
    private StarField stars;

    public Demo(Activity activity) {
        super(activity);

        this.activity=activity;

        //setDebugFlags(DEBUG_CHECK_GL_ERROR|DEBUG_LOG_GL_CALLS);
        setRenderer(this);

        PowerManager pm=(PowerManager)activity.getSystemService(Context.POWER_SERVICE);
        wl=pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP   |
            PowerManager.ON_AFTER_RELEASE        ,
            TAG
        );
        wl.acquire();

        mp=MediaPlayer.create(activity, R.raw.track);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        mp.release();
        wl.release();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Get some OpenGL information.
        Log.i(TAG, "GL_VENDOR     : "    + gl.glGetString(GL10.GL_VENDOR));
        Log.i(TAG, "GL_RENDERER   : "    + gl.glGetString(GL10.GL_RENDERER));
        Log.i(TAG, "GL_VERSION    : "    + gl.glGetString(GL10.GL_VERSION));
        Log.i(TAG, "GL_EXTENSIONS :\n  " + gl.glGetString(GL10.GL_EXTENSIONS).trim().replace(" ", "\n  "));

        int[] params=new int[2];
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, params, 0);
        Log.i(TAG, "GL_MAX_TEXTURE_SIZE        : " + String.valueOf(params[0]));
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_UNITS, params, 0);
        Log.i(TAG, "GL_MAX_TEXTURE_UNITS       : " + String.valueOf(params[0]));
        gl.glGetIntegerv(GL10.GL_SMOOTH_POINT_SIZE_RANGE, params, 0);
        Log.i(TAG, "GL_SMOOTH_POINT_SIZE_RANGE : " + String.valueOf(params[0]) + ", " + String.valueOf(params[1]));

        if (gl instanceof GL11) {
            Log.i(TAG, "Implements GL11");
        }
        else {
            Log.i(TAG, "Implements GL10");

            Log.e(TAG, "No GL11 available");
            activity.finish();
        }

        intro_fade=new IntroFade(activity, (GL11)gl);
        intro_blink=new IntroBlink(activity, (GL11)gl);

        fade_in_white=new ColorFade(activity, (GL11)gl, 1000, true, 1, 1, 1);
        stars=new StarField(activity, (GL11)gl, 400);
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
            t-=intro_fade.getDuration();

            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

            stars.play(t);

            // This must come last as it needs to render on top of all other effects.
            fade_in_white.play(t);
        }
    }
}
