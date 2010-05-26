package com.rabenauge.parandroid;

import android.opengl.GLU;

import com.rabenauge.demo.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

public class CamCube extends EffectManager {
    private Cube cube;
    private int frameCounter=1;
    private int[] camTexture;
    public byte[] camFrame=new byte[256*256];

    private class Cube extends Effect {
        public void onStart(GL11 gl) {
            gl.glEnable(GL11.GL_CULL_FACE);

            gl.glVertexPointer(3, GL11.GL_FLOAT, 0, FloatBuffer.wrap(camObjCoord));
            gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);

            gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, FloatBuffer.wrap(camTexCoords));
            gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            frameCounter++;

            gl.glEnable(GL11.GL_TEXTURE_2D);
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

            bindCameraTexture(gl);

            gl.glMatrixMode(GL11.GL_MODELVIEW);
            gl.glLoadIdentity();
            GLU.gluLookAt(gl, 0, 0, 8.5f, 0, 0, 0, 0, 1, 0);

            gl.glRotatef((float)Math.sin(frameCounter/20.0f)*40,0,1,0);
            gl.glRotatef(frameCounter,1,0,0);
            gl.glRotatef((float)Math.cos(frameCounter/40.0f)*40,0,0,1);

            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 4, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 8, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP,12, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP,16, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP,20, 4);
        }

        public void onStop(GL11 gl) {
            gl.glMatrixMode(GL11.GL_MODELVIEW);
            gl.glLoadIdentity();

            gl.glDisable(GL11.GL_CULL_FACE);
        }

        void bindCameraTexture(GL11 gl) {
            synchronized(this) {
                if (camTexture==null)
                    camTexture=new int[1];
                else
                    gl.glDeleteTextures(1, camTexture, 0);

                gl.glGenTextures(1, camTexture, 0);
                int tex = camTexture[0];
                gl.glBindTexture(GL11.GL_TEXTURE_2D, tex);
                gl.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_LUMINANCE, 256, 256, 0, GL11.GL_LUMINANCE, GL11.GL_UNSIGNED_BYTE, ByteBuffer.wrap(camFrame));
                gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            }
        }

        private final float[] camObjCoord={
            // front
             -2.0f, -1.5f,  2.0f,
              2.0f, -1.5f,  2.0f,
             -2.0f,  1.5f,  2.0f,
              2.0f,  1.5f,  2.0f,
             // back
             -2.0f, -1.5f, -2.0f,
             -2.0f,  1.5f, -2.0f,
              2.0f, -1.5f, -2.0f,
              2.0f,  1.5f, -2.0f,
             // left
             -2.0f, -1.5f,  2.0f,
             -2.0f,  1.5f,  2.0f,
             -2.0f, -1.5f, -2.0f,
             -2.0f,  1.5f, -2.0f,
             // right
              2.0f, -1.5f, -2.0f,
              2.0f,  1.5f, -2.0f,
              2.0f, -1.5f,  2.0f,
              2.0f,  1.5f,  2.0f,
             // top
             -2.0f,  1.5f,  2.0f,
              2.0f,  1.5f,  2.0f,
             -2.0f,  1.5f, -2.0f,
              2.0f,  1.5f, -2.0f,
             // bottom
             -2.0f, -1.5f,  2.0f,
             -2.0f, -1.5f, -2.0f,
              2.0f, -1.5f,  2.0f,
              2.0f, -1.5f, -2.0f,
        };

        private final float[] camTexCoords={
             0.0f, 0.0f,
             0.9375f, 0.0f,
             0.0f, 0.625f,
             0.9375f, 0.625f,

            // back
             0.9375f, 0.0f,
             0.9375f, 0.625f,
             0.0f, 0.0f,
             0.0f, 0.625f,
            // left
             0.9375f, 0.0f,
             0.9375f, 0.625f,
             0.0f, 0.0f,
             0.0f, 0.625f,
            // right
             0.9375f, 0.0f,
             0.9375f, 0.625f,
             0.0f, 0.0f,
             0.0f, 0.625f,
            // top
             0.0f, 0.0f,
             0.9375f, 0.0f,
             0.0f, 0.625f,
             0.9375f, 0.625f,
            // bottom
             0.9375f, 0.0f,
             0.9375f, 0.625f,
             0.0f, 0.0f,
             0.0f, 0.625f
        };
    }

    public CamCube(Demo demo, GL11 gl) {
        super(gl);

        cube=new Cube();
        add(cube, 1);
    }
}
