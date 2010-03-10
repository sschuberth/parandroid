package com.rabenauge.parandroid;

import android.app.Activity;
import android.opengl.GLU;
import android.util.FloatMath;
import com.rabenauge.demo.*;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL11;

public class IntroBlink extends EffectManager {
    private static final float[] joints={
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
    private float[] colors;

    private class BlinkJoints extends Effect {
        public void onStart(GL11 gl) {
            gl.glColorPointer(4, GL11.GL_FLOAT, 0, FloatBuffer.wrap(colors));
            gl.glPointSize(2.5f);
            gl.glEnable(GL11.GL_POINT_SMOOTH);
        }

        public void onRender(GL11 gl, float s) {
            // Set the projection to match the joint coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, 800, 480, 0);

            // Fade the colors.
            for (int i=3; i<colors.length; i+=4) {
                colors[i]=(1-FloatMath.cos(2*PI*s*i))/2;
            }

            // Set OpenGL states that differ from the concurrently running fading part.
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            gl.glVertexPointer(2, GL11.GL_FLOAT, 0, FloatBuffer.wrap(joints));
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

        colors=new float[joints.length/2*4];
        for (int i=0; i<colors.length; i+=4) {
            colors[i  ]=1;
            colors[i+1]=0;
            colors[i+2]=0;
            // Set alpha when rendering.
        }

        // Schedule the effects in this part.
        add(new BlinkJoints(), 1000 + 6*7*1000);
    }
}
