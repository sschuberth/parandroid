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
import android.util.FloatMath;

import com.rabenauge.demo.*;
import com.rabenauge.gl.*;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

public class Credits extends EffectManager {
    private static final int FACE_FRONT=0, FACE_RIGHT=1, FACE_BACK=2, FACE_LEFT=3, FACE_TOP=4, FACE_BOTTOM=5, FACE_COUNT=6;
    private static final int CREDITS_NAMES=0, CREDITS_RAB=1, CREDITS_TRSI=2, CREDITS_FINAL=3;

    private static final ShortBuffer vertices=DirectBuffer.nativeShortBuffer(6*4*3)
        // Front face
        .put((short)+1).put((short)+1).put((short)+1)
        .put((short)-1).put((short)+1).put((short)+1)
        .put((short)-1).put((short)-1).put((short)+1)
        .put((short)+1).put((short)-1).put((short)+1)

        // Right face
        .put((short)+1).put((short)+1).put((short)-1)
        .put((short)+1).put((short)+1).put((short)+1)
        .put((short)+1).put((short)-1).put((short)+1)
        .put((short)+1).put((short)-1).put((short)-1)

        // Back face
        .put((short)+1).put((short)-1).put((short)-1)
        .put((short)-1).put((short)-1).put((short)-1)
        .put((short)-1).put((short)+1).put((short)-1)
        .put((short)+1).put((short)+1).put((short)-1)

        // Left face
        .put((short)-1).put((short)+1).put((short)+1)
        .put((short)-1).put((short)+1).put((short)-1)
        .put((short)-1).put((short)-1).put((short)-1)
        .put((short)-1).put((short)-1).put((short)+1)

        // Top face
        .put((short)+1).put((short)+1).put((short)-1)
        .put((short)-1).put((short)+1).put((short)-1)
        .put((short)-1).put((short)+1).put((short)+1)
        .put((short)+1).put((short)+1).put((short)+1)

        // Bottom face
        .put((short)+1).put((short)-1).put((short)+1)
        .put((short)-1).put((short)-1).put((short)+1)
        .put((short)-1).put((short)-1).put((short)-1)
        .put((short)+1).put((short)-1).put((short)-1)
    ;

    static {
        // Reset the position before using the buffer!
        vertices.position(0);
    }

    private Demo demo;
    private Texture2D[] textures;

    @SuppressWarnings("SameParameterValue")
    private class Cubes extends Effect {
        private int num_cubes_x, num_cubes_y;
        private IntBuffer tex_coords;

        private float cubeRotXStart=0;
        private float xpos, ypos;

        public Cubes(int num_cubes_x, int num_cubes_y) {
            this.num_cubes_x=num_cubes_x;
            this.num_cubes_y=num_cubes_y;

            tex_coords=DirectBuffer.nativeIntBuffer(num_cubes_x*num_cubes_y*4*2);

            float step_x=65536.0f/num_cubes_x, step_y=65536.0f/num_cubes_y;
            for (int y=0; y<num_cubes_y; ++y) {
                int y0=(int)(y*step_y), y1=(int)((y+1)*step_y);

                for (int x=0; x<num_cubes_x; ++x) {
                    int x0=(int)(x*step_x), x1=(int)((x+1)*step_x);

                    tex_coords.put(x1);  tex_coords.put(y0);
                    tex_coords.put(x0);  tex_coords.put(y0);
                    tex_coords.put(x0);  tex_coords.put(y1);
                    tex_coords.put(x1);  tex_coords.put(y1);
                }
            }

            tex_coords.position(0);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            float cubeRotX=cubeRotXStart+s*90;

            gl.glEnable(GL11.GL_CULL_FACE);
            gl.glEnable(GL11.GL_DEPTH_TEST);
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            gl.glMatrixMode(GL11.GL_MODELVIEW);

            // Move the cube centers so the cubes will be centered in x-direction.
            xpos=-num_cubes_x+1;
            for(int x=0; x<num_cubes_x; ++x, xpos+=2.0f) {
                // Move the cube centers so the cubes will be centered in y-direction.
                ypos=num_cubes_y-1;

                for(int y=0; y<num_cubes_y; ++y, ypos-=2.0f) {
                    gl.glLoadIdentity();

                    // Position the cubes.
                    gl.glTranslatef(xpos, ypos, -8);

                    // Spread out the cubes.
                    float f=1-FloatMath.cos(s*2*DemoMath.PI);
                    gl.glTranslatef(xpos*f, ypos*f, -f*4);
                    gl.glRotatef(cubeRotX, 1, 0, 0);

                    tex_coords.position((y*num_cubes_x+x)*4*2);
                    gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, tex_coords.slice());

                    for (int i=0; i<FACE_COUNT; ++i) {
                        // Switch for the cubes faces in the order they are shown.
                        switch(i) {
                            case FACE_FRONT  : textures[CREDITS_NAMES].makeCurrent(); break;
                            case FACE_TOP    : textures[CREDITS_RAB  ].makeCurrent(); break;
                            case FACE_BACK   : textures[CREDITS_TRSI ].makeCurrent(); break;
                            case FACE_BOTTOM : textures[CREDITS_FINAL].makeCurrent(); break;

                            // Not shown:
                            case FACE_LEFT   : textures[CREDITS_RAB  ].makeCurrent(); break;
                            case FACE_RIGHT  : textures[CREDITS_TRSI ].makeCurrent(); break;
                        }

                        vertices.position(i*4*3);
                        gl.glVertexPointer(3, GL11.GL_SHORT, 0, vertices.slice());

                        gl.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 4);
                    }
                }
            }

            gl.glLoadIdentity();

            gl.glDisable(GL11.GL_DEPTH_TEST);
            gl.glDisable(GL11.GL_CULL_FACE);
        }

        public void onStop(GL11 gl) {
            cubeRotXStart+=90;
        }
    }

    public class TextureShake extends Effect {
        private Texture2D title;

        public TextureShake(Texture2D title) {
            this.title=title;
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glMatrixMode(GL11.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            float f=FloatMath.sin(s*DemoMath.PI)/50;
            gl.glTranslatef((float)(Math.random()-0.5)*f, (float)(Math.random()-0.5)*f, 0);
            Helper.drawScreenSpaceTexture(title);
            gl.glPopMatrix();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public class TextureFadeSound extends Effect {
        private Texture2D texture;
        private boolean in;

        public TextureFadeSound(Texture2D texture, boolean in) {
            this.texture=texture;
            this.in=in;
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            float a=in?s:1-s;
            gl.glColor4f(1, 1, 1, a);
            Helper.drawScreenSpaceTexture(texture);

            // We need to restore this immediately for other concurrently running effects.
            gl.glColor4f(1, 1, 1, 1);

            // Fade out the sound.
            float dB=(1-s)*15;
            demo.getMediaPlayer().setVolume(dB, dB);
        }
    }

    public Credits(Demo demo, GL11 gl) {
        super(gl);

        this.demo=demo;

        // Load the end screens.
        int[] ids={R.drawable.credits_names, R.drawable.credits_rab, R.drawable.credits_trsi, R.drawable.credits_final};
        textures=new Texture2D[ids.length];

        for (int i=0; i<ids.length; ++i) {
            Bitmap bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), ids[i]);
            textures[i]=new Texture2D(gl);
            textures[i].setData(bitmap);
            bitmap.recycle();
        }

        // Schedule the effects in this part.
        add(new EffectManager.Wait(), Demo.DURATION_TOTAL-Demo.DURATION_PART_OUTRO-Demo.DURATION_PART_OUTRO_FADE);
        add(new EffectManager.TextureFade(textures[0], true), Demo.DURATION_PART_OUTRO_FADE);

        long d=Demo.DURATION_PART_OUTRO/(2+(1+2)*ids.length+8);
        add(new EffectManager.TextureShow(textures[0]), 2*d);

        Cubes cubes=new Cubes(5, 3);
        for (int i=1; i<ids.length; ++i) {
            add(cubes, d);
            add(new TextureShake(textures[i]), 300);
            add(new EffectManager.TextureShow(textures[i]), 2*d-300);
        }
        add(new TextureFadeSound(textures[3], false), 8*d);
    }
}
