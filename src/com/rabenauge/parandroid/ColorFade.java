package com.rabenauge.parandroid;

import android.app.Activity;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;
import javax.microedition.khronos.opengles.GL11;

public class ColorFade extends EffectManager {
    private boolean in;
    private float r, g, b;

    private class Fade extends Effect {
        public void onRender(GL11 gl, long t, long e, float s) {
            // Set OpenGL states.
            gl.glDisable(GL11.GL_TEXTURE_2D);

            float a=in?1-s:s;
            gl.glColor4f(r, g, b, a);
            Helper.drawScreenSpaceQuad(gl);
            gl.glColor4f(1, 1, 1, 1);

            // Restore OpenGL states.
            gl.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    public ColorFade(Activity activity, GL11 gl, long t, boolean in, float r, float g, float b) {
        super(gl);

        this.in=in;

        this.r=r;
        this.g=g;
        this.b=b;

        add(new Fade(), t);
    }
}
