package com.rabenauge.parandroid;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.EGLConfig;

public class Demo extends GLSurfaceView implements Renderer {
    private static final String TAG="ParaNdroiD";

    public Demo(Context context) {
        super(context);
        setRenderer(this);
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

        gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
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
    }
}
