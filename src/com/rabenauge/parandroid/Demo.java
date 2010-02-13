package com.rabenauge.parandroid;

import com.rabenauge.gl.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private Context context;
    private PowerManager.WakeLock wl;

    private FloatBuffer vertices;
    private PointSprite bob;
    private Texture2D logo_rab, logo_trsi;

    public Demo(Context context) {
        super(context);

        this.context=context;

        //setDebugFlags(DEBUG_CHECK_GL_ERROR|DEBUG_LOG_GL_CALLS);
        setRenderer(this);

        PowerManager pm=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        wl=pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP   |
            PowerManager.ON_AFTER_RELEASE        ,
            TAG
        );
        wl.acquire();
    }

    protected void finalize() throws Throwable {
        super.finalize();
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
        }

        GL11 gl11=(GL11)gl;

        // Enable the vertex array.
        vertices=FloatBuffer.wrap(data);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Create a point sprite from a bitmap resource.
        bob=new PointSprite(gl11);

        Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        bob.setData(bitmap);
        bitmap.recycle();

        bob.setSize(48.0f);

        // Load the logos.
        logo_rab=new Texture2D(gl11);
        bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_rab);
        logo_rab.setData(bitmap);
        bitmap.recycle();

        logo_trsi=new Texture2D(gl11);
        bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_trsi);
        logo_trsi.setData(bitmap);
        bitmap.recycle();

        // Enable 2D texturing in general.
        logo_rab.enable(true);
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
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        Helper.drawScreenSpaceTexture(logo_rab);
    }
}
