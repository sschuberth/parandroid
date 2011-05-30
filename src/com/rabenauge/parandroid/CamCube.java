/*
 * Copyright 2010-2011 bodo, eyebex, ralph, spotter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;

import com.rabenauge.cam.Preview;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

public class CamCube extends EffectManager {
    public static final long DURATION_PART_TRANSITION=2000;

    public static final int TEX_WIDTH=256;
    public static final int TEX_HEIGHT=256;

    public static final float TEX_COORD_X=((float)Preview.PRE_WIDTH)/TEX_WIDTH;
    public static final float TEX_COORD_Y=((float)Preview.PRE_HEIGHT)/TEX_HEIGHT;

    private static final float[] CAM_OBJ_COORDS={
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

    private static final float[] CAM_TEX_COORDS={
        // front
        0.0f        , 0.0f,
        TEX_COORD_X , 0.0f,
        0.0f        , TEX_COORD_Y,
        TEX_COORD_X , TEX_COORD_Y,
        // back
        TEX_COORD_X , 0.0f,
        TEX_COORD_X , TEX_COORD_Y,
        0.0f        , 0.0f,
        0.0f        , TEX_COORD_Y,
        // left
        TEX_COORD_X , 0.0f,
        TEX_COORD_X , TEX_COORD_Y,
        0.0f        , 0.0f,
        0.0f        , TEX_COORD_Y,
        // right
        TEX_COORD_X , 0.0f,
        TEX_COORD_X , TEX_COORD_Y,
        0.0f        , 0.0f,
        0.0f        , TEX_COORD_Y,
        // top
        0.0f        , 0.0f,
        TEX_COORD_X , 0.0f,
        0.0f        , TEX_COORD_Y,
        TEX_COORD_X , TEX_COORD_Y,
        // bottom
        TEX_COORD_X , 0.0f,
        TEX_COORD_X , TEX_COORD_Y,
        0.0f        , 0.0f,
        0.0f        , TEX_COORD_Y
    };

    private FloatBuffer camObjCoords,camTexCoords;

    private int frameCounter=0;
    private Texture2D camTexture;

    public byte[] camFrame=new byte[TEX_WIDTH*TEX_HEIGHT];

    private Texture2D frame_left, frame_right;

    private class Cube extends Effect {
        public void onStart(GL11 gl) {
            gl.glEnable(GL11.GL_CULL_FACE);

            gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glMatrixMode(GL11.GL_MODELVIEW);
            gl.glPushMatrix();

            // Draw the frames.
            float m=0.0f;
            if (s<0.5f) {
                m=0.5f-s;
            }
            else if (s>0.5f) {
                m=s-0.5f;
            }

            gl.glEnable(GL11.GL_BLEND);

            gl.glLoadIdentity();
            gl.glTranslatef(-m, 0, 0);
            Helper.drawScreenSpaceTexture(frame_left);
            gl.glMatrixMode(GL11.GL_MODELVIEW);

            gl.glLoadIdentity();
            gl.glTranslatef(m, 0, 0);
            Helper.drawScreenSpaceTexture(frame_right);
            gl.glMatrixMode(GL11.GL_MODELVIEW);

            gl.glDisable(GL11.GL_BLEND);

            // Draw the cube.
            gl.glLoadIdentity();
            GLU.gluLookAt(gl, 0, 0, 8.5f, 0, 0, 0, 0, 1, 0);

            if (s<0.5f) {
                gl.glScalef(s*2, s*2, 1.0f);
            }
            else if (s>0.5f) {
                s=1.0f-s;
                gl.glScalef(s*2, s*2, 1.0f);
            }

            gl.glRotatef((float)Math.sin(frameCounter/20.0f)*40,0,1,0);
            gl.glRotatef(frameCounter,1,0,0);
            gl.glRotatef((float)Math.cos(frameCounter/40.0f)*40,0,0,1);

            ++frameCounter;

            gl.glVertexPointer(3, GL11.GL_FLOAT, 0, camObjCoords);
            gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, camTexCoords);

            bindCameraTexture(gl);

            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP,  0, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP,  4, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP,  8, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 12, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 16, 4);
            gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 20, 4);

            gl.glPopMatrix();
        }

        public void onStop(GL11 gl) {
            gl.glDisable(GL11.GL_CULL_FACE);
        }

        void bindCameraTexture(GL11 gl) {
            synchronized(this) {
                camTexture.makeCurrent();
                gl.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_LUMINANCE, 256, 256, 0, GL11.GL_LUMINANCE, GL11.GL_UNSIGNED_BYTE, ByteBuffer.wrap(camFrame));
            }
        }
    }

    public CamCube(Demo demo, GL11 gl) {
        super(gl);

        // Initialize the gl-pointer arrays.
        camObjCoords=DirectBuffer.nativeFloatBuffer(CAM_OBJ_COORDS);
        camTexCoords=DirectBuffer.nativeFloatBuffer(CAM_TEX_COORDS);

        // Load the frame textures.
        Bitmap bitmap;

        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.camera_frame_left);
        frame_left=new Texture2D(gl);
        frame_left.setData(bitmap);
        bitmap.recycle();

        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.camera_frame_right);
        frame_right=new Texture2D(gl);
        frame_right.setData(bitmap);
        bitmap.recycle();

        camTexture=new Texture2D(gl);

        add(new Cube(), DURATION_PART_TRANSITION*2);
    }
}
