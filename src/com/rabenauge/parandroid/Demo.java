package com.rabenauge.parandroid;

import com.rabenauge.gl.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.PowerManager;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.EGLConfig;
import java.nio.FloatBuffer;

public class Demo extends GLSurfaceView implements Renderer {
    private static final String TAG="ParaNdroiD";

    private static final float[] data={
         0.2f,  0.2f, -1.0f, // UR (LR in portrait mode)
        -0.2f,  0.2f, -1.0f, // UL (UR in portrait mode)
        -0.2f, -0.2f, -1.0f, // LL (UL in portrait mode)
         0.2f, -0.2f, -1.0f  // LR (LL in portrait mode)
    };

    private Activity activity;
    private PowerManager.WakeLock wl;
    private MediaPlayer mp;

    private FloatBuffer vertices;
    private PointSprite bob;
    private Texture2D title_parandroid, title_trsinrab;
    private Texture2D logo_rab, logo_trsi;

    private Long t_start;

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
        Log.i(TAG, gl.glGetString(GL10.GL_VENDOR));
        Log.i(TAG, gl.glGetString(GL10.GL_RENDERER));
        Log.i(TAG, gl.glGetString(GL10.GL_VERSION));
        Log.i(TAG, gl.glGetString(GL10.GL_EXTENSIONS).replace(' ', '\n'));
        if (gl instanceof GL11) {
            Log.i(TAG, "Implements GL11");
        }
        else {
            Log.i(TAG, "Implements GL10");

            Log.e(TAG, "No GL11 available");
            activity.finish();
        }

        GL11 gl11=(GL11)gl;

        // Enable the vertex array.
        vertices=FloatBuffer.wrap(data);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Create a point sprite from a bitmap resource.
        bob=new PointSprite(gl11);

        Bitmap bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
        bob.setData(bitmap);
        bitmap.recycle();

        bob.setSize(48.0f);

        // Load the title screens.
        title_parandroid=new Texture2D(gl11);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.title_parandroid);
        title_parandroid.setData(bitmap);
        bitmap.recycle();

        title_trsinrab=new Texture2D(gl11);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.title_trsinrab);
        title_trsinrab.setData(bitmap);
        bitmap.recycle();

        // Load the logos.
        logo_rab=new Texture2D(gl11);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_rab);
        logo_rab.setData(bitmap);
        bitmap.recycle();

        logo_trsi=new Texture2D(gl11);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_trsi);
        logo_trsi.setData(bitmap);
        bitmap.recycle();

        // Enable 2D texturing in general.
        title_trsinrab.enable(true);
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
        long t_offset=0, t_part;

        // Just clear the screen for a few seconds to make sure the display mode initializes etc.
        t_part=3*1000;
        if (t>=t_offset && t<=t_offset+t_part) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            return;
        }
        t_offset+=t_part;

        // Fade in the first title screen from black.
        t_part=10*1000;
        if (t>=t_offset && t<=t_offset+t_part) {
            float a=(float)(t-t_offset)/t_part;

            // A simple linear fade-in looks unnatural.
            a*=a;

            gl.glColor4f(a, a, a, a);
            Helper.drawScreenSpaceTexture(title_trsinrab);
        }
        t_offset+=t_part;

        // Keep the screen contents for a few seconds.
        t_part=10*1000;
        if (t>=t_offset && t<=t_offset+t_part) {
            return;
        }
        t_offset+=t_part;

        // Fade to the second title screen from the first one.
        t_part=10*1000;
        if (t>=t_offset && t<=t_offset+t_part) {
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

            gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            Helper.drawScreenSpaceTexture(title_trsinrab);

            float a=(float)(t-t_offset)/t_part;
            gl.glColor4f(a, a, a, a);
            Helper.drawScreenSpaceTexture(title_parandroid);

            gl.glDisable(GL10.GL_BLEND);
        }
        t_offset+=t_part;

        // Keep the screen contents for a few seconds.
        t_part=10*1000;
        if (t>=t_offset && t<=t_offset+t_part) {
            return;
        }
        t_offset+=t_part;

        // Fade out the second title screen to white.
        t_part=100;
        if (t>=t_offset && t<=t_offset+t_part) {
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

            gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            Helper.drawScreenSpaceTexture(title_parandroid);

            // Dirty trick: Render a textured quad without a texture, so the white
            // default texture will be used.
            title_parandroid.enable(false);

            float a=(float)(t-t_offset)/t_part;
            gl.glColor4f(a, a, a, a);
            Helper.drawScreenSpaceTexture(title_parandroid);

            title_parandroid.enable(true);

            gl.glDisable(GL10.GL_BLEND);
        }
        t_offset+=t_part;
    }
}
