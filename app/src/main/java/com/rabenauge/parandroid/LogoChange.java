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
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL11;

@SuppressWarnings("SameParameterValue")
public class LogoChange extends EffectManager {
    private Demo demo;

    private static final float HIDE_MAX=1.0f;
    private static final float HIDE_SPEED=0.06f;
    private float hide=0.0f;

    private int grid_x, grid_y;
    private long serene_duration, ripple_duration;

    private long t_serene, t_ripple, t_fade;
    private boolean pingpong, updown;

    private Texture2D logo_trsi;

    private IntBuffer grid_coords, tex_coords;
    private ShortBuffer[] indices;
    private float[][] dists;

    private boolean change_now=false;

    public boolean isHidden() {
        return hide==HIDE_MAX;
    }

    public void changeNow() {
        change_now=true;
    }

    private class Change extends Effect {
        public void onStart(GL11 gl) {
            gl.glClientActiveTexture(GL11.GL_TEXTURE1);
            gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, tex_coords);
            gl.glClientActiveTexture(GL11.GL_TEXTURE0);

            gl.glActiveTexture(GL11.GL_TEXTURE1);

            // Choose to use texture combiners.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);

            // Choose to interpolate RGB as well as alpha.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_INTERPOLATE);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_INTERPOLATE);

            // The first argument to RGB / alpha interpolation is the previous texture's color / alpha.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_RGB, GL11.GL_PREVIOUS);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);

            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_ALPHA, GL11.GL_PREVIOUS);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);

            // The second argument to RGB / alpha interpolation is the current texture's color / alpha.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_RGB, GL11.GL_TEXTURE);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);

            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_ALPHA, GL11.GL_TEXTURE);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);

            // The third argument to RGB / alpha interpolation is the environment color's alpha.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_RGB, GL11.GL_CONSTANT);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_RGB, GL11.GL_ONE_MINUS_SRC_ALPHA);

            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_ALPHA, GL11.GL_CONSTANT);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            gl.glActiveTexture(GL11.GL_TEXTURE0);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            if (demo.shootem) {
                if (hide<HIDE_MAX) {
                    hide+=HIDE_SPEED;
                    if (hide>HIDE_MAX) {
                        hide=HIDE_MAX;
                    }
                }
                else {
                    // Do nothing if we are in the "Shoot'em!" mode and
                    // the effect is already completely hidden.
                    return;
                }
            }
            else {
                if (hide>0.0f) {
                    hide-=HIDE_SPEED;
                    if (hide<0.0f) {
                        hide=0.0f;
                    }
                }
            }

            long sd=serene_duration;
            if (change_now){
                sd=0;
                change_now=false;
            }

            if (pingpong) {
                // Make sure t_fade snaps to a multiple of ripple_duration;
                long t_snap1=t_fade/ripple_duration;
                t_fade+=e;
                long t_snap2=t_fade/ripple_duration;
                if (t_snap1!=t_snap2) {
                    t_fade=t_snap2*ripple_duration;
                }

                t_serene=0;

                // If rippling is done, switch to serene mode.
                t_ripple+=e;
                if (t_ripple>ripple_duration) {
                    t_ripple=ripple_duration;
                    pingpong=false;
                }
            }
            else {
                // Only reset fading every second time rippling is reset.
                if (updown) {
                    t_fade=0;
                    updown=false;
                }

                t_ripple=0;

                // If serening is done, switch to ripple mode.
                t_serene+=e;
                if (t_serene>=sd) {
                    t_serene=sd;
                    pingpong=true;
                }
            }

            float amp=(float)(Math.cos(DemoMath.PI*(float)t_ripple/ripple_duration*2-DemoMath.PI)+1)/2, freq=amp*2;

            int g=2;
            for (int y=0; y<grid_y; ++y) {
                for (int x=0; x<grid_x; ++x) {
                    grid_coords.put(g,(int)((Math.cos((dists[y][x]+amp*100)*freq)*amp/4-1)*65536));
                    g+=3;
                }
            }

            // http://mathworld.wolfram.com/TriangleWave.html
            float tri=0.5f*(float)t_fade/ripple_duration;
            float alpha=2*Math.abs(Math.round(tri)-tri);

            // Set OpenGL states.
            logo_trsi.makeCurrent();

            gl.glActiveTexture(GL11.GL_TEXTURE1);
            Helper.toggleState(gl, GL11.GL_TEXTURE_2D, true);

            float[] params={0, 0, 0, alpha};
            gl.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, params, 0);

            gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, tex_coords);
            gl.glVertexPointer(3, GL11.GL_FIXED, 0, grid_coords);

            gl.glTranslatef(0, hide, 0);
            for (int i=0; i<grid_y-1; ++i) {
                gl.glDrawElements(GL11.GL_TRIANGLE_STRIP, indices[i].capacity(), GL11.GL_UNSIGNED_SHORT, indices[i]);
            }
            gl.glTranslatef(0, -hide, 0);

            // Restore OpenGL states.
            Helper.toggleState(gl, GL11.GL_TEXTURE_2D, false);
            gl.glActiveTexture(GL11.GL_TEXTURE0);
        }
    }

    public LogoChange(Demo demo, GL11 gl, int grid_x, int grid_y, long serene_duration, long ripple_duration) {
        super(gl);

        this.demo=demo;

        this.grid_x=grid_x;
        this.grid_y=grid_y;

        this.serene_duration=serene_duration;
        this.ripple_duration=ripple_duration;

        t_serene=t_ripple=t_fade=0;
        pingpong=updown=false;

        // Load the title screens.
        Bitmap bitmap;

        gl.glActiveTexture(GL11.GL_TEXTURE1);
        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.logo_rab);
        Texture2D logo_rab=new Texture2D(gl);
        logo_rab.setData(bitmap);
        bitmap.recycle();

        gl.glActiveTexture(GL11.GL_TEXTURE0);
        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.logo_trsi);
        logo_trsi=new Texture2D(gl);
        logo_trsi.setData(bitmap);
        bitmap.recycle();

        // Generate the geometry and texture coordinates.
        grid_coords=DirectBuffer.nativeIntBuffer(grid_x*grid_y*3);
        tex_coords=DirectBuffer.nativeIntBuffer(grid_x*grid_y*2);
        indices=new ShortBuffer[grid_y];
        for (int y=0; y<grid_y; ++y) {
            indices[y]=DirectBuffer.nativeShortBuffer(grid_x*2);
        }
        dists=new float[grid_y][grid_x];

        float x0=-0.4f, x1=0.4f;
        float y0=0.35f, y1=-0.15f;

        int g=0;
        for (int y=0; y<grid_y; ++y) {
            float sy=(float)y/grid_y;

            int y2=grid_y/2-y;
            y2*=y2;

            for (int x=0; x<grid_x; ++x) {
                float sx=(float)x/grid_x;

                int x2=grid_x/2-x;
                x2*=x2;

                grid_coords.put(g++,(int)((x0+(x1-x0)*sx)*65536));
                grid_coords.put(g++,(int)((y0+(y1-y0)*sy)*65536));
                g++;  // Set z when rendering.

                tex_coords.put((int)(sx*65536));
                tex_coords.put((int)(sy*65536));

                int offset=y*grid_x+x;
                indices[y].put((short)offset);
                indices[y].put((short)(offset+grid_x));

                dists[y][x]=(float)Math.sqrt(x2+y2);
            }

            indices[y].position(0);
        }
        tex_coords.position(0);

        // Schedule the effects in this part.
        add(new Change(), Demo.DURATION_MAIN_EFFECTS);
    }
}
