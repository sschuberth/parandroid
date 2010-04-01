package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.FloatMath;

import com.rabenauge.demo.*;
import com.rabenauge.gl.*;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class Credits extends EffectManager {
    private Texture2D[] textures;

    private class Cubes extends Effect {
        private final static int MAX_X=5;
        private final static int MAX_Y=3;

        private  float[][] cubeVertexCoords = new float[][] {
            new float[] { // top
                 1, 1,-1,
                -1, 1,-1,
                -1, 1, 1,
                 1, 1, 1
            },
            new float[] { // bottom
                 1,-1, 1,
                -1,-1, 1,
                -1,-1,-1,
                 1,-1,-1
            },
            new float[] { // front
                 1, 1, 1,
                -1, 1, 1,
                -1,-1, 1,
                 1,-1, 1
            },
            new float[] { // back
                 1,-1,-1,
                -1,-1,-1,
                -1, 1,-1,
                 1, 1,-1
            },
            new float[] { // left
                -1, 1, 1,
                -1, 1,-1,
                -1,-1,-1,
                -1,-1, 1
            },
            new float[] { // right
                 1, 1,-1,
                 1, 1, 1,
                 1,-1, 1,
                 1,-1,-1
            },
        };

        private float[] cubeTextureCoords = new float[8];
        private FloatBuffer[] cubeVertexBfr;

        private float cubeRotX;
        private float cubeRotY;
        private float cubeRotZ;

        private float cubeRotXStart=0;

        private float ypos;
        private float xpos;

        public void onStart(GL11 gl) {
            cubeVertexBfr = new FloatBuffer[6];
            for (int i = 0; i < 6; i++)
            {
                cubeVertexBfr[i] = FloatBuffer.wrap(cubeVertexCoords[i]);
            }

            gl.glMatrixMode(GL10.GL_MODELVIEW);

            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glEnable(GL10.GL_DEPTH_TEST);
        }

        @Override
        public void onRender(GL11 gl, long t, long e, float s) {
            cubeRotX=cubeRotXStart+s*90;

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();

            xpos=-6f;

            // draw the cubes
            for(int x=0;x<MAX_X;x++)
            {
                xpos+=2f;
                ypos=-4f;
                for(int y=0;y<MAX_Y;y++)
                {
                    gl.glLoadIdentity();
                    ypos+=2f;

                    float f=1-FloatMath.cos(s*2*DemoMath.PI);
                    gl.glTranslatef(xpos+(x-MAX_X/2)*f*2, ypos+(y-MAX_Y/2)*f*2, -8);

                    gl.glRotatef(cubeRotX, 1, 0, 0);
                    gl.glRotatef(cubeRotY, 0, 1, 0);
                    gl.glRotatef(cubeRotZ, 0, 0, 1);

                    for (int i = 0; i < 6; i++) // draw each face
                    {
                        switch (i)
                        {
                            case 0: textures[1].makeCurrent(); break; // top
                            case 1: textures[3].makeCurrent(); break; // bottom
                            case 2: textures[0].makeCurrent(); break; // front
                            case 3: textures[2].makeCurrent(); break; // back
                        }

                        setTextureCoords(x, y);

                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cubeVertexBfr[i]);
                        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, FloatBuffer.wrap(cubeTextureCoords));
                        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
                    }
                }
            }
        }

        public void onStop(GL11 gl) {
            gl.glLoadIdentity();

            gl.glDisable(GL10.GL_DEPTH_TEST);
            gl.glDisable(GL10.GL_CULL_FACE);

            cubeRotXStart+=90;
        }

        private void setTextureCoords(int x,int y)
        {
            float y2=2;
            y2=y2-y;

            // Hard-code the scaled POT texture size.
            float W=1024;
            float H=512;

            float xOL=(W/MAX_X)*x;
            float xOR=(W/MAX_X)*(x+1);
            float yO=(H/MAX_Y)*y2;
            float yU=(H/MAX_Y)*(y2+1);

            cubeTextureCoords[0]=(xOR)/W;    cubeTextureCoords[1]=(yO)/H;
            cubeTextureCoords[2]=(xOL)/W;    cubeTextureCoords[3]=(yO)/H;
            cubeTextureCoords[4]=(xOL)/W;    cubeTextureCoords[5]=(yU)/H;
            cubeTextureCoords[6]=(xOR)/W;    cubeTextureCoords[7]=(yU)/H;
        }
    }

    public Credits(Demo demo, GL11 gl) {
        super(gl);

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
        long overlap=6*1000;
        add(new EffectManager.Wait(), Demo.DURATION_TOTAL-Demo.DURATION_PART_OUTRO-overlap);
        add(new EffectManager.TextureFade(textures[0], true), overlap);

        long d=Demo.DURATION_PART_OUTRO/(2+(4+2)*ids.length+8);
        add(new EffectManager.TextureShow(textures[0]), 2*d);

        Cubes cubes=new Cubes();
        for (int i=1; i<ids.length; ++i) {
            add(cubes, 4*d);
            add(new EffectManager.TextureShow(textures[i]), 2*d);
        }
        add(new EffectManager.TextureFade(textures[3], false), 8*d);
    }
}
