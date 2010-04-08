package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;
import javax.microedition.khronos.opengles.GL11;

public class IntroFade extends EffectManager {
    public static final long DURATION_EFFECT_DELAY=1000;
    public static final long DURATION_EFFECT_FADEOUT=500;

    private Texture2D title_droid, title_parandroid, title_trsinrab;

    // Fade-in the Droid title screen from black.
    private class FadeBlackToDroid extends Effect {
        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // A simple linear fade-in looks unnatural.
            s*=s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(title_droid);
        }

        public void onStop(GL11 gl) {
            // Just in case onRender() was not called with exactly s=1.
            gl.glColor4f(1, 1, 1, 1);
        }
    }

    // Fade-out the Para 'N' droiD title screen to white.
    private class FadeParaNdroiDToWhite extends Effect {
        public void onStart(GL11 gl) {
            gl.glClearColor(1, 1, 1, 1);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // A simple linear fade-in looks unnatural.
            s=1-s*s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(title_parandroid);
        }

        public void onStop(GL11 gl) {
            // Just in case onRender() was not called with exactly s=1.
            gl.glColor4f(1, 1, 1, 1);

            // Make sure the screen is finally full white
            // in case onRender() was not called with s=1.0.
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
            gl.glClearColor(0, 0, 0, 0);
        }
    }

    public IntroFade(Demo demo, GL11 gl) {
        super(gl);

        // Load the title screens.
        Bitmap bitmap;

        title_droid=new Texture2D(gl);
        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.title_droid);
        title_droid.setData(bitmap);
        bitmap.recycle();

        title_parandroid=new Texture2D(gl);
        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.title_parandroid);
        title_parandroid.setData(bitmap);
        bitmap.recycle();

        title_trsinrab=new Texture2D(gl);
        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.title_trsinrab);
        title_trsinrab.setData(bitmap);
        bitmap.recycle();

        // Set universal OpenGL states for all effects in this part.
        Helper.toggleState(gl, GL11.GL_TEXTURE_2D, true);

        gl.glEnable(GL11.GL_BLEND);
        gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        // Schedule the effects in this part.
        add(new Clear(), DURATION_EFFECT_DELAY);

        long duration=(Demo.DURATION_PART_INTRO-DURATION_EFFECT_DELAY-DURATION_EFFECT_FADEOUT)/6;
        add(new FadeBlackToDroid(), duration);
        add(new EffectManager.TextureShow(title_droid), duration);

        add(new EffectManager.TextureTransition(title_droid, title_trsinrab), duration);
        add(new EffectManager.TextureShow(title_trsinrab), duration);

        add(new EffectManager.TextureTransition(title_trsinrab, title_parandroid), duration);
        add(new EffectManager.TextureShow(title_parandroid), duration);

        add(new FadeParaNdroiDToWhite(), DURATION_EFFECT_FADEOUT);
    }
}
