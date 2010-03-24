package com.rabenauge.parandroid;

import android.app.Activity;
import android.opengl.GLU;
import android.util.FloatMath;
import com.rabenauge.demo.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL11;

public class IntroBlink extends EffectManager {
    private static final short[] joints={
        270, 93,  231,130,  213,130,
        251,298,  269,298,
        185,357,
        402, 52,  402, 72,  420, 72,
        439,128,  439,147,
        335,339,  335,357,  335,375,  355,375,  373,375,
        480,288,  461,322,  461,340,
        530, 45,  548, 64,  548, 83,  548,100,
        555,397,  574,397,  574,378
    };
    private int[] colors;

    private class BlinkJoints extends Effect {
        public void onStart(GL11 gl) {
            gl.glPointSize(2.5f);
            gl.glColorPointer(4, GL11.GL_FIXED, 0, IntBuffer.wrap(colors));
            gl.glEnable(GL11.GL_POINT_SMOOTH);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            // Set the projection to match the joint coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, 800, 480, 0);

            // Fade the colors.
            for (int i=3; i<colors.length; i+=4) {
                colors[i]=(int)((1.0f-FloatMath.cos(PI*s*i))/2.0f*65536);
            }

            // Set OpenGL states that differ from the concurrently running fading part.
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            gl.glVertexPointer(2, GL11.GL_SHORT, 0, ShortBuffer.wrap(joints));
            gl.glDrawArrays(GL11.GL_POINTS, 0, joints.length/2);

            // Restore OpenGL states for the fading part.
            gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
            gl.glEnable(GL11.GL_TEXTURE_2D);
        }

        public void onStop(GL11 gl) {
            gl.glDisable(GL11.GL_POINT_SMOOTH);
        }
    }

    public IntroBlink(Activity activity, GL11 gl) {
        super(gl);

        colors=new int[joints.length/2*4];
        for (int i=0; i<colors.length; i+=4) {
            colors[i  ]=(int)(1.0f*65536);
            colors[i+1]=0;
            colors[i+2]=0;
            // Set alpha when rendering.
        }

        // Schedule the effects in this part.
        add(new BlinkJoints(), 1000 + 6*7*1000);
    }
}
