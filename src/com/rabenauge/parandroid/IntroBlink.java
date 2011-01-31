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

public class IntroBlink extends EffectManager {
    private static final int WIDTH=800, HEIGHT=480;
    private static final short[] JOINTS={
        // First "column"
        270, 93,
        213,131,  232,131,
        251,299,  269,299,
        185,357,

        // Second "column"
        403, 53,  403, 72,  420, 72,
        439,128,  439,147,
        335,338,  335,356,  335,376,  354,376,  373,376,

        // Third "column"
        529, 46,  548, 65,  548, 83,  548,100,
        480,288,  461,322,  461,341,
        555,397,  574,378,  574,397
    };
    private static final float POINT_SIZE=7.0f;

    private PointSprite dot;
    private ShortBuffer joints;
    private IntBuffer colors;

    private class BlinkJoints extends Effect {
        public void onStart(GL11 gl) {
            dot.setSize(POINT_SIZE);
            gl.glColorPointer(4, GL11.GL_FIXED, 0, colors);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            // Fade the colors.
            for (int i=3; i<colors.capacity(); i+=4) {
                colors.put(i, (int)((1.0f-FloatMath.cos(DemoMath.PI*s*i))/2.0f*65536));
            }

            // Set OpenGL states that differ from the concurrently running fading part.
            Helper.toggleState(gl, GL11.GL_POINT_SPRITE_OES, true);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            // Set the projection to match the joint coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            dot.makeCurrent();

            gl.glVertexPointer(2, GL11.GL_SHORT, 0, joints);
            gl.glDrawArrays(GL11.GL_POINTS, 0, joints.capacity()/2);

            // Restore OpenGL states for the fading part.
            gl.glPopMatrix();

            gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
            Helper.toggleState(gl, GL11.GL_POINT_SPRITE_OES, false);

            // Yes, using color arrays seems to modify the color state!
            gl.glColor4f(1, 1, 1, 1);
        }
    }

    public IntroBlink(Demo demo, GL11 gl) {
        super(gl);

        // Load the dot texture.
        Bitmap bitmap;

        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.dot);
        dot=new PointSprite(gl);
        dot.setData(bitmap);
        bitmap.recycle();

        // Initialize the gl-pointer arrays.
        joints=DirectBuffer.nativeShortBuffer(JOINTS);

        colors=DirectBuffer.nativeIntBuffer(joints.capacity()/2*4);
        for (int i=0; i<colors.capacity(); i+=4) {
            colors.put(i  , (int)(1.0f*65536));
            colors.put(i+1,                 0);
            colors.put(i+2,                 0);
            // Set alpha when rendering.
        }

        // Schedule the effects in this part.
        add(new BlinkJoints(), Demo.DURATION_PART_INTRO-IntroFade.DURATION_EFFECT_FADEOUT);
    }
}
