package com.rabenauge.gl;

import android.opengl.GLU;
import java.nio.IntBuffer;
import javax.microedition.khronos.opengles.GL10;

/*
 * A class for various static helper methods.
 */
public class Helper {
    // Vertices and texture coordinates for rendering a bitmap in order LR, LL, UL, UR.
    private static final int w=(int)(1.0f*65536), h=(int)(1.0f*65536);
    private static final IntBuffer v=IntBuffer.allocate(8).put(w).put(h).put(0).put(h).put(0).put(0).put(w).put(0);

    public static void drawScreenSpaceQuad(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluOrtho2D(gl, 0, w/65536.0f, h/65536.0f, 0);

        gl.glVertexPointer(2, GL10.GL_FIXED, 0, v);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
    }

    public static void drawScreenSpaceTexture(Texture2D tex) {
        tex.makeCurrent();
        tex.gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, v);

        drawScreenSpaceQuad(tex.gl);
    }

    public static void toggleState(GL10 gl, int cap, boolean state) {
        if (state) {
            gl.glEnable(cap);
        }
        else {
            gl.glDisable(cap);
        }
    }
}
