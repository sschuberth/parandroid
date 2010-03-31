package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.util.FloatMath;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

public class BobsStatic extends EffectManager {
    private static final int WIDTH=800, HEIGHT=480;
    private static final int NUM_BOBS=10;
    private static final int NUM_POINTS=400;
    private static final int TEX_PER_ROW=4;
    private static final float TEX_WIDTH=107.25f, TEX_HEIGHT=128.0f;

    private Texture2D bobs;
    private int[] quad_coords, tex_coords;
    private short[] indices;
    private float[] points;

    public enum BobType {
        // This matches the texture order in the bitmap.
        Sentinel, Skull, Cyclope, Rabenauge
    }

    public static void calcBobTexture2D(int tex_per_row, BobType name, int[] coords, int offset) {
        float step=1.0f/tex_per_row;
        int min=(int)(name.ordinal()*step*65536);
        int max=min+(int)(step*65536);

        coords[offset    ]=min;
        coords[offset + 1]=(int)(0.0f*65536);

        coords[offset + 2]=min;
        coords[offset + 3]=(int)(1.0f*65536);

        coords[offset + 4]=max;
        coords[offset + 5]=(int)(0.0f*65536);

        coords[offset + 6]=max;
        coords[offset + 7]=(int)(1.0f*65536);
    }

    public static void calcBobVertex2D(float center_x, float center_y, float width, float height, int[] coords, int offset) {
        width/=2;
        height/=2;

        int cxmw=(int)((center_x-width)*65536);
        int cxpw=(int)((center_x+width)*65536);
        int cymh=(int)((center_y-height)*65536);
        int cyph=(int)((center_y+height)*65536);

        // UL
        coords[offset    ]=cxmw;
        coords[offset + 1]=cymh;

        // LL
        coords[offset + 2]=cxmw;
        coords[offset + 3]=cyph;

        // UR
        coords[offset + 4]=cxpw;
        coords[offset + 5]=cymh;

        // LR
        coords[offset + 6]=cxpw;
        coords[offset + 7]=cyph;
    }

    private class Swarm extends Effect {
        public void onRender(GL11 gl, long t, long e, float s) {
            // Move the bobs.
            int i, p=((int)t/40)&(~1);
            for (i=0; i<NUM_BOBS; ++i) {
                int offset=i*8, step=i*7;

                // Start moving the bobs one after the other, not all at the same time.
                float px=quad_coords[offset];
                int cx=((p+step)%NUM_POINTS)*2, cy=cx+1;
                calcBobVertex2D(points[cx], points[cy], TEX_WIDTH*0.6f, TEX_HEIGHT*0.6f, quad_coords, offset);
                if (px==0) {
                    break;
                }
            }

            // Set OpenGL states.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            bobs.makeCurrent();

            // Render the bobs.
            gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, IntBuffer.wrap(tex_coords));
            gl.glVertexPointer(2, GL11.GL_FIXED, 0, IntBuffer.wrap(quad_coords));
            gl.glDrawElements(GL11.GL_TRIANGLES, i*6, GL11.GL_UNSIGNED_SHORT, ShortBuffer.wrap(indices));

            // Restore OpenGL states.
            gl.glPopMatrix();
        }
    }

    public BobsStatic(Demo demo, GL11 gl) {
        super(gl);

        // Load the bob textures.
        Bitmap bitmap;

        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.bobs_static);
        bobs=new Texture2D(gl);
        bobs.setData(bitmap);
        bitmap.recycle();

        // Generate the geometry and texture coordinates.
        quad_coords=new int[NUM_BOBS*4*2];
        tex_coords=new int[NUM_BOBS*4*2];
        indices=new short[NUM_BOBS*6];

        int b=0, v=0;
        for (int i=0; i<NUM_BOBS; ++i) {
            int offset=i*8;

            // Vertices are calculated in the render loop.
            quad_coords[offset+0]=quad_coords[offset+1]=0;
            quad_coords[offset+2]=quad_coords[offset+3]=0;
            quad_coords[offset+4]=quad_coords[offset+5]=0;
            quad_coords[offset+6]=quad_coords[offset+7]=0;

            if (i<2) {
                calcBobTexture2D(TEX_PER_ROW, BobType.Skull, tex_coords, offset);
            }
            else if (i<5) {
                calcBobTexture2D(TEX_PER_ROW, BobType.Cyclope, tex_coords, offset);
            }
            else if (i<7) {
                calcBobTexture2D(TEX_PER_ROW, BobType.Skull, tex_coords, offset);
            }
            else {
                calcBobTexture2D(TEX_PER_ROW, BobType.Sentinel, tex_coords, offset);
            }

            indices[b+0]=(short)(v+0);
            indices[b+1]=(short)(v+1);
            indices[b+2]=(short)(v+2);
            indices[b+3]=(short)(v+3);
            indices[b+4]=(short)(v+2);
            indices[b+5]=(short)(v+1);
            b+=6;
            v+=4;
        }

        final float amp_x=WIDTH/2.0f-WIDTH/10.0f, amp_y=HEIGHT/3.0f-HEIGHT/10.0f;
        final float center_x=WIDTH/2.0f, center_y=HEIGHT/3.0f;

        points=new float[NUM_POINTS*2];

        int p=0;
        for (int i=0; i<NUM_POINTS; ++i) {
            float angle=(float)i/NUM_POINTS*DemoMath.PI*2;
            points[p++]=FloatMath.cos(angle*3)*amp_x+center_x;
            points[p++]=FloatMath.sin(angle*5)*amp_y+center_y;
        }

        // Schedule the effects in this part.
        add(new Swarm(), Demo.DURATION_PART_STATIC);
    }
}
