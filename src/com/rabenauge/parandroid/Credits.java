package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rabenauge.demo.*;
import com.rabenauge.gl.*;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class Credits extends EffectManager {
    private Texture2D[] textures;

    private class Cubes extends Effect {
        private long WAIT=5000;
        private long startTime;
        private int effectState=0;

        public void onStart(GL11 gl) {
            cubeVertexBfr = new FloatBuffer[6];
            cubeTextureBfr = new FloatBuffer[6];
            for (int i = 0; i < 6; i++)
            {
                cubeVertexBfr[i] = FloatBuffer.wrap(cubeVertexCoords[i]);
                cubeTextureBfr[i] = FloatBuffer.wrap(cubeTextureCoords[i]);
            }

            gl.glShadeModel(GL10.GL_SMOOTH);
            gl.glClearColor(0, 0, 0, 0);

            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);

            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glCullFace(GL10.GL_BACK);

            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

            startTime=System.currentTimeMillis();
        }

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

        private  float[][] cubeTextureCoords = new float[6][8];
        private  FloatBuffer[] cubeVertexBfr;
        private  FloatBuffer[] cubeTextureBfr;

        private  float cubeRotX;
        private  float cubeRotY;
        private  float cubeRotZ;

        private float ypos;
        private float xpos;

        @Override
        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
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

                    setCubeSpace(gl,x, y);
                    //gl.glTranslatef(xpos, ypos, -8);
                    gl.glRotatef(cubeRotX, 1, 0, 0);
                    gl.glRotatef(cubeRotY, 0, 1, 0);
                    gl.glRotatef(cubeRotZ, 0, 0, 1);

                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                    for (int i = 0; i < 6; i++) // draw each face
                    {
                        switch (i)
                        {
                            case 0: textures[1].makeCurrent(); break; // top
                            case 1: textures[3].makeCurrent(); break; // bottom
                            case 2: textures[0].makeCurrent(); break; // front
                            case 3: textures[2].makeCurrent(); break; // back
                        }

                        setTextureCoords(0,x, y);
                        cubeTextureBfr[i] = FloatBuffer.wrap(cubeTextureCoords[0]);

                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cubeVertexBfr[i]);
                        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, cubeTextureBfr[i]);
                        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
                    }

                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                }
            }

            if(System.currentTimeMillis()-startTime>WAIT&&effectState==0)
            {
                effectState=1;
            }

            if(effectState==1)
            {
                cubeRotX += 1.0f;
                if(cubeRotX%90==0)
                {
                    effectState=3;
                    startTime=System.currentTimeMillis();
                }
            }
        }

        float xspace=0;
        float yspace=0;

        private void setCubeSpace(GL11 gl,int x, int y) {
            float xpos2=0;
            float ypos2=0;

            if(effectState==0)
            {
                xspace=0;
                yspace=0;
            }
            if(effectState==1)
            {
                xspace-=0.001f;
                yspace-=0.0015f;
                //if(xspace<=-0.8f)  // x space
                //  effectState=2;
            }
            if(effectState==3)
            {
                xspace+=0.001f;
                yspace+=0.0015f;
                if(xspace>=0.0f)
                    effectState=0;
            }

            switch(x)
            {
                case 0: xpos2=xpos+(xspace*2); break;
                case 1: xpos2=xpos+xspace; break;
                case 3: xpos2=xpos+(xspace*-1); break;
                case 4: xpos2=xpos+(xspace*-1*2); break;
            }
            switch(y)
            {
                case 0: ypos2=ypos+yspace; break;
                case 2: ypos2=ypos-yspace; break;
            }

//          if(effectState==2)
//          {
//              cubeRotX += 1.0f;
//              if(cubeRotX%90==0)
//              {
//                  effectState=3;
//                  startTime=System.currentTimeMillis();
//              }
//          }

            gl.glTranslatef(xpos2, ypos2, -8);
        }

        private void setTextureCoords (int i,int x,int y) // TODO remove i
        {
            float y2=2;
            y2=y2-y;

            float W=1024;  // TODO why does it work ?
            float H=512;   // TODO why does it work ?
            float xOL=(W/MAX_X)*x;
            float xOR=(W/MAX_X)*(x+1);
            float yO=(H/MAX_Y)*y2;
            float yU=(H/MAX_Y)*(y2+1);

            cubeTextureCoords[i][0]=(xOR)/W;    cubeTextureCoords[i][1]=(yO)/H ;
            cubeTextureCoords[i][2]=(xOL)/W;    cubeTextureCoords[i][3]=(yO)/H;
            cubeTextureCoords[i][4]=(xOL)/W ;   cubeTextureCoords[i][5]=(yU)/H;
            cubeTextureCoords[i][6]=(xOR)/W;    cubeTextureCoords[i][7]=(yU)/H;
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
        add(new Cubes(), 160*1000);
    }
}
