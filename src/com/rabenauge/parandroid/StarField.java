package com.rabenauge.parandroid;

import android.app.Activity;
import android.opengl.GLU;
import android.util.FloatMath;
import com.rabenauge.demo.*;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL11;

public class StarField extends EffectManager {
    private static final int WIDTH=800, HEIGHT=480;
    private int center_x, center_y;

    private float[] star_coords;
    private float[] star_speeds;

    private float randomize(float factor, float invalid) {
        float r;
        do {
            r=(float)(Math.random()*factor);
        }
        while (r==invalid); 
        return r;
    }

    private float move(float s) {
        if (Math.abs(s)<1) {
            if (s>0) {
                s=FloatMath.ceil(s);
            }
            else {
                s=FloatMath.floor(s);
            }
        }
        return s;
    }

    private class Flight extends Effect {
        public void onStart(GL11 gl) {
            gl.glColorPointer(4, GL11.GL_FLOAT, 0, FloatBuffer.wrap(star_speeds));
            gl.glEnable(GL11.GL_LINE_SMOOTH);
        }

        public void onRender(GL11 gl, float s, long t) {
            // Set the projection to match the star coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            for (int i=0; i<star_coords.length; i+=4) {
                float factor=star_speeds[i]*t/500;

                // Avoid flickering of small stars by making the lines at least two pixels long.
                star_coords[i+2]=star_coords[i  ] + move((star_coords[i  ]-center_x)*factor);
                star_coords[i+3]=star_coords[i+1] + move((star_coords[i+1]-center_y)*factor);
            }

            // Set OpenGL states.
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            gl.glVertexPointer(2, GL11.GL_FLOAT, 0, FloatBuffer.wrap(star_coords));
            gl.glDrawArrays(GL11.GL_LINES, 0, star_coords.length/2);

            // Restore OpenGL states.
            gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
            gl.glEnable(GL11.GL_TEXTURE_2D);

            // Move the stars.
            for (int i=0; i<star_coords.length; i+=4) {
                star_coords[i  ]=star_coords[i+2];
                star_coords[i+1]=star_coords[i+3];
                if (star_coords[i  ]<0 || star_coords[i  ]>=WIDTH
                 || star_coords[i+1]<0 || star_coords[i+1]>=HEIGHT) 
                {
                    star_coords[i  ]=randomize(WIDTH , center_x);
                    star_coords[i+1]=randomize(HEIGHT, center_y);
                }
            }
        }

        public void onStop(GL11 gl) {
            gl.glDisable(GL11.GL_LINE_SMOOTH);
        }
    }

    public StarField(Activity activity, GL11 gl, int count) {
        super(gl);

        center_x=WIDTH/2;
        center_y=HEIGHT/2;

        // Stores x, y per star vertex.
        star_coords=new float[count*2*2];
        // Stores r, g, b, a per star vertex.
        star_speeds=new float[count*4*2];

        for (int c=0, s=0; c<star_coords.length; c+=4, s+=8) {
            star_coords[c  ]=randomize(WIDTH , center_x);
            star_coords[c+1]=randomize(HEIGHT, center_y);

            // The speed is also used as the grayscale color.
            star_speeds[s  ]=randomize(1, 0);
            star_speeds[s+1]=star_speeds[s];
            star_speeds[s+2]=star_speeds[s];
            star_speeds[s+3]=1;

            star_speeds[s+4]=star_speeds[s  ];
            star_speeds[s+5]=star_speeds[s+1];
            star_speeds[s+6]=star_speeds[s+2];
            star_speeds[s+7]=star_speeds[s+3];
        }

        // Schedule the effects in this part.
        add(new Flight(), 45*1000);
    }
}
