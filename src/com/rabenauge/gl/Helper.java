package com.rabenauge.gl;

import android.opengl.GLU;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/*
 * A class for various static helper methods.
 */
public class Helper {
    // Vertices and texture coordinates for rendering a bitmap in order LR, LL, UL, UR.
    private static final float w=1, h=1;
    private static final FloatBuffer v=FloatBuffer.allocate(8).put(w).put(h).put(0).put(h).put(0).put(0).put(w).put(0);

    public static void drawScreenSpaceTexture(Texture2D tex) {
        tex.makeCurrent();

        tex.gl.glMatrixMode(GL10.GL_PROJECTION);
        tex.gl.glLoadIdentity();
        GLU.gluOrtho2D(tex.gl, 0, w, h, 0);

        tex.gl.glVertexPointer(2, GL10.GL_FLOAT, 0, v);
        tex.gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, v);

        tex.gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
    }
}
