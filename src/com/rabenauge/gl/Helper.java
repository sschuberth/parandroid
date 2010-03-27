package com.rabenauge.gl;

import android.opengl.GLU;
import java.nio.ByteBuffer;
import javax.microedition.khronos.opengles.GL10;

/*
 * A class for various static helper methods.
 */
public class Helper {
    // Vertices and texture coordinates for rendering a bitmap in order LR, LL, UL, UR.
    private static final byte WIDTH=1, HEIGHT=1;
    private static final ByteBuffer VERTICES=ByteBuffer.allocateDirect(8).put(WIDTH).put(HEIGHT).put((byte)0).put(HEIGHT).put((byte)0).put((byte)0).put(WIDTH).put((byte)0);

    static {
        // Reset the buffer position before using the buffer!
        VERTICES.position(0);
    }

    public static void drawScreenSpaceQuad(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

        gl.glVertexPointer(2, GL10.GL_BYTE, 0, VERTICES);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);

        gl.glPopMatrix();
    }

    public static void drawScreenSpaceTexture(Texture2D tex) {
        tex.makeCurrent();
        tex.gl.glTexCoordPointer(2, GL10.GL_BYTE, 0, VERTICES);

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
