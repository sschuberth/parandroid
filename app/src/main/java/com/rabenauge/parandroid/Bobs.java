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
import android.util.FloatMath;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

public class Bobs extends EffectManager {
    private static final int WIDTH=800, HEIGHT=480;
    private static final int NUM_BOBS=10;
    private static final float SPEED=0.9f;
    private static final float AMP_X=WIDTH/2.0f-WIDTH/10.0f, AMP_Y=HEIGHT/3.0f-HEIGHT/10.0f;
    private static final float CENTER_X=WIDTH/2.0f, CENTER_Y=HEIGHT/3.0f;
    private static final int TEX_PER_ROW=5, TEX_PER_COLUMN=2;
    private static final float TEX_WIDTH=107.25f, TEX_HEIGHT=128.0f;

    private Demo demo;
    private int draw_bobs=NUM_BOBS;

    private Texture2D bobs_static, bobs_dynamic;
    private boolean bob_toggle=false;

    private IntBuffer quad_coords, tex_coords;
    private ShortBuffer indices;

    public boolean isHidden() {
        return draw_bobs==0;
    }

    public void toggleBobs() {
        bob_toggle=!bob_toggle;
    }

    public void toggleBobs(boolean toggle) {
        bob_toggle=toggle;
    }

    public static void calcBobTexture2D(int tex_per_row, int tex_per_column, int index, IntBuffer coords, int offset) {
        float x_step=1.0f/tex_per_row,y_step=1.0f/tex_per_column;
        int x=index%tex_per_row, y=index/tex_per_row;
        int x_min=(int)(x*x_step*65536), x_max=x_min+(int)(x_step*65536);
        int y_min=(int)(y*y_step*65536), y_max=y_min+(int)(y_step*65536);

        coords.position(offset);

        coords.put(x_min);
        coords.put(y_min);

        coords.put(x_min);
        coords.put(y_max);

        coords.put(x_max);
        coords.put(y_min);

        coords.put(x_max);
        coords.put(y_max);

        coords.position(0);
    }

    public static void calcBobVertex2D(float center_x, float center_y, float width, float height, IntBuffer coords, int offset) {
        width/=2;
        height/=2;

        int cxmw=(int)((center_x-width)*65536);
        int cxpw=(int)((center_x+width)*65536);
        int cymh=(int)((center_y-height)*65536);
        int cyph=(int)((center_y+height)*65536);

        coords.position(offset);

        // UL
        coords.put(cxmw);
        coords.put(cymh);

        // LL
        coords.put(cxmw);
        coords.put(cyph);

        // UR
        coords.put(cxpw);
        coords.put(cymh);

        // LR
        coords.put(cxpw);
        coords.put(cyph);

        coords.position(0);
    }

    private class Swarm extends Effect {
        private int counter=0;

        public void onRender(GL11 gl, long t, long e, float s) {
            if (demo.shootem) {
                if (draw_bobs>0) {
                    --draw_bobs;
                }
                else {
                    // Do nothing if we are in the "Shoot'em!" mode and
                    // the effect is already completely hidden.
                    return;
                }
            }
            else {
                if (draw_bobs<NUM_BOBS) {
                    ++draw_bobs;
                }
            }

            int i;

            // Do not use the time here in order to have equidistant bob positions for
            // each frame and thus make the animation smooth and not juddering.
            float pos=counter*SPEED;

            // Move the bobs.
            for (i=0; i<draw_bobs; ++i) {
                int offset=i*8, step=i*4;

                // Start moving the bobs one after the other, not all at the same time.
                float px=quad_coords.get(offset);

                float angle=(pos+step)/360*DemoMath.PI*2;
                float x=FloatMath.cos(angle*3)*AMP_X+CENTER_X;
                float y=FloatMath.sin(angle*5)*AMP_Y+CENTER_Y;

                calcBobVertex2D(x, y, TEX_WIDTH*0.6f, TEX_HEIGHT*0.6f, quad_coords, offset);

                if (px==0) {
                    break;
                }
            }

            ++counter;

            // Set OpenGL states.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            if (bob_toggle) {
                bobs_dynamic.makeCurrent();
            }
            else {
                bobs_static.makeCurrent();
            }

            // Render the bobs.
            gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, tex_coords);
            gl.glVertexPointer(2, GL11.GL_FIXED, 0, quad_coords);
            gl.glDrawElements(GL11.GL_TRIANGLES, i*6, GL11.GL_UNSIGNED_SHORT, indices);

            // Restore OpenGL states.
            gl.glPopMatrix();
        }
    }

    public Bobs(Demo demo, GL11 gl) {
        super(gl);

        this.demo=demo;

        // Load the bob textures.
        Bitmap bitmap;

        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.bobs_static);
        bobs_static=new Texture2D(gl);
        bobs_static.setData(bitmap);
        bitmap.recycle();

        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.bobs_dynamic);
        bobs_dynamic=new Texture2D(gl);
        bobs_dynamic.setData(bitmap);
        bitmap.recycle();

        // Generate the geometry and texture coordinates.
        quad_coords=DirectBuffer.nativeIntBuffer(NUM_BOBS*4*2);
        tex_coords=DirectBuffer.nativeIntBuffer(NUM_BOBS*4*2);
        indices=DirectBuffer.nativeShortBuffer(NUM_BOBS*6);

        int v=0;
        for (int i=0; i<NUM_BOBS; ++i) {
            int offset=i*8;

            // Vertices are calculated in the render loop.

            calcBobTexture2D(TEX_PER_ROW, TEX_PER_COLUMN, NUM_BOBS-1-i, tex_coords, offset);

            indices.put((short)(v+0));
            indices.put((short)(v+1));
            indices.put((short)(v+2));
            indices.put((short)(v+3));
            indices.put((short)(v+2));
            indices.put((short)(v+1));

            v+=4;
        }
        indices.position(0);

        // Schedule the effects in this part.
        add(new Swarm(), Demo.DURATION_MAIN_EFFECTS);
    }
}
