package com.rabenauge.parandroid;

import android.opengl.GLU;
import android.util.FloatMath;
import com.rabenauge.demo.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL11;

public class IntroBlink extends EffectManager {
    private static final int WIDTH=800, HEIGHT=480;
    private static final short[] JOINTS={
        269, 93,  231,131,  213,131,
        251,299,  269,299,
        185,357,
        403, 53,  403, 72,  421, 72,
        438,128,  438,147,
        335,339,  335,357,  335,376,  354,376,  373,376,
        479,288,  461,323,  461,341,
        531, 46,  549, 65,  549, 83,  549,100,
        556,397,  573,397,  573,378
    };

    private int[] colors;

    private class BlinkJoints extends Effect {
        public void onStart(GL11 gl) {
            gl.glPointSize(2.5f);
            gl.glColorPointer(4, GL11.GL_FIXED, 0, IntBuffer.wrap(colors));

            // Only enable point smoothing if we can draw sizes greater than 1,
            // else the points will be too small.
            int[] params=new int[2];
            gl.glGetIntegerv(GL11.GL_SMOOTH_POINT_SIZE_RANGE, params, 0);
            if (params[1]>1) {
                gl.glEnable(GL11.GL_POINT_SMOOTH);
            }
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            // Fade the colors.
            for (int i=3; i<colors.length; i+=4) {
                colors[i]=(int)((1.0f-FloatMath.cos(DemoMath.PI*s*i))/2.0f*65536);
            }

            // Set OpenGL states that differ from the concurrently running fading part.
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            // Set the projection to match the joint coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            gl.glVertexPointer(2, GL11.GL_SHORT, 0, ShortBuffer.wrap(JOINTS));
            gl.glDrawArrays(GL11.GL_POINTS, 0, JOINTS.length/2);

            // Restore OpenGL states for the fading part.
            gl.glPopMatrix();

            gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
            gl.glEnable(GL11.GL_TEXTURE_2D);

            // Yes, using color arrays seems to modify the color state!
            gl.glColor4f(1, 1, 1, 1);
        }

        public void onStop(GL11 gl) {
            gl.glDisable(GL11.GL_POINT_SMOOTH);
        }
    }

    public IntroBlink(Demo demo, GL11 gl) {
        super(gl);

        colors=new int[JOINTS.length/2*4];
        for (int i=0; i<colors.length; i+=4) {
            colors[i  ]=(int)(1.0f*65536);
            colors[i+1]=0;
            colors[i+2]=0;
            // Set alpha when rendering.
        }

        // Schedule the effects in this part.
        add(new BlinkJoints(), Demo.DURATION_PART_INTRO-IntroFade.DURATION_EFFECT_FADEOUT);
    }
}
