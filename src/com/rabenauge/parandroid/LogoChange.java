package com.rabenauge.parandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.FloatMath;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL11;

public class LogoChange extends EffectManager {
    private int grid_x, grid_y;
    private long serene_duration, ripple_duration;

    private long t_serene, t_ripple, t_fade;
    private boolean pingpong, updown;

    private Texture2D logo_trsi, logo_rab;

    private int[] grid_coords, tex_coords;
    private short[][] indices;
    private float[][] dists;

    private class Change extends Effect {
        public void onStart(GL11 gl) {
            gl.glClientActiveTexture(GL11.GL_TEXTURE1);
            gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, IntBuffer.wrap(tex_coords));
            gl.glClientActiveTexture(GL11.GL_TEXTURE0);

            gl.glActiveTexture(GL11.GL_TEXTURE1);

            // Choose to use texture combiners.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);

            // Choose to interpolate RGB as well as alpha.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_INTERPOLATE);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_INTERPOLATE);

            // The first argument to RGB / alpha interpolation is the previous texture's color / alpha.
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_RGB, GL11.GL_PREVIOUS);
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);

            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_ALPHA, GL11.GL_PREVIOUS);
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);

            // The second argument to RGB / alpha interpolation is the current texture's color / alpha.
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_RGB, GL11.GL_TEXTURE);
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);

            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_ALPHA, GL11.GL_TEXTURE);
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);

            // The third argument to RGB / alpha interpolation is the environment color's alpha.
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_RGB, GL11.GL_CONSTANT);
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_RGB, GL11.GL_ONE_MINUS_SRC_ALPHA);

            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_SRC2_ALPHA, GL11.GL_CONSTANT);
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND2_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            gl.glActiveTexture(GL11.GL_TEXTURE0);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
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
                    pingpong=!pingpong;
                }
            }
            else {
                // Only reset fading every second time rippling is reset.
                if (updown) {
                    t_fade=0;
                    updown=!updown;
                }

                t_ripple=0;

                // If serening is done, switch to ripple mode.
                t_serene+=e;
                if (t_serene>=serene_duration) {
                    t_serene=serene_duration;
                    pingpong=!pingpong;
                }
            }

            float amp=(FloatMath.cos(DemoMath.PI*(float)t_ripple/ripple_duration*2-DemoMath.PI)+1)/2, freq=amp*2;

            int g=2;
            for (int y=0; y<grid_y; ++y) {
                for (int x=0; x<grid_x; ++x) {
                    grid_coords[g]=(int)((FloatMath.cos((dists[y][x]+amp*100)*freq)*amp/4-1)*65536);
                    g+=3;
                }
            }

            // http://mathworld.wolfram.com/TriangleWave.html
            float tri=0.5f*(float)t_fade/ripple_duration;
            float alpha=2*Math.abs(Math.round(tri)-tri);

            // Set OpenGL states.
            logo_trsi.makeCurrent();

            gl.glActiveTexture(GL11.GL_TEXTURE1);
            logo_rab.enable(true);

            float[] params={0, 0, 0, alpha};
            gl.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, params, 0);

            gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, IntBuffer.wrap(tex_coords));
            gl.glVertexPointer(3, GL11.GL_FIXED, 0, IntBuffer.wrap(grid_coords));
            for (int i=0; i<grid_y-1; ++i) {
                gl.glDrawElements(GL11.GL_TRIANGLE_STRIP, indices[i].length, GL11.GL_UNSIGNED_SHORT, ShortBuffer.wrap(indices[i]));
            }

            // Restore OpenGL states.
            logo_rab.enable(false);
            gl.glActiveTexture(GL11.GL_TEXTURE0);
        }
    }

    public LogoChange(Activity activity, GL11 gl, int grid_x, int grid_y, long serene_duration, long ripple_duration) {
        super(gl);

        this.grid_x=grid_x;
        this.grid_y=grid_y;

        this.serene_duration=serene_duration;
        this.ripple_duration=ripple_duration;

        t_serene=t_ripple=t_fade=0;
        pingpong=updown=false;

        // Load the title screens.
        Bitmap bitmap;

        gl.glActiveTexture(GL11.GL_TEXTURE1);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_rab);
        logo_rab=new Texture2D(gl);
        logo_rab.setData(bitmap);
        bitmap.recycle();

        gl.glActiveTexture(GL11.GL_TEXTURE0);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_trsi);
        logo_trsi=new Texture2D(gl);
        logo_trsi.setData(bitmap);
        bitmap.recycle();

        // Generate the geometry and texture coordinates.
        grid_coords=new int[grid_x*grid_y*3];
        tex_coords=new int[grid_x*grid_y*2];
        indices=new short[grid_y][grid_x*2];
        dists=new float[grid_y][grid_x];

        float x0=-0.4f, x1=0.4f;
        float y0=0.35f, y1=-0.15f;

        int g=0, t=0;
        for (int y=0; y<grid_y; ++y) {
            float sy=(float)y/grid_y;

            int y2=grid_y/2-y;
            y2*=y2;

            for (int x=0; x<grid_x; ++x) {
                float sx=(float)x/grid_x;

                int x2=grid_x/2-x;
                x2*=x2;

                grid_coords[g++]=(int)((x0+(x1-x0)*sx)*65536);
                grid_coords[g++]=(int)((y0+(y1-y0)*sy)*65536);
                g++;  // Set z when rendering.

                tex_coords[t++]=(int)(sx*65536);
                tex_coords[t++]=(int)(sy*65536);

                int offset=y*grid_x+x;
                indices[y][x*2]=(short)offset;
                indices[y][x*2+1]=(short)(offset+grid_x);

                dists[y][x]=FloatMath.sqrt(x2+y2);
            }
        }

        // Schedule the effects in this part.
        add(new Change(), Demo.DURATION_PART_STATIC);
    }
}
