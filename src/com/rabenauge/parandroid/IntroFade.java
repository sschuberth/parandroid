package com.rabenauge.parandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;
import javax.microedition.khronos.opengles.GL11;

public class IntroFade extends EffectManager {
    private Texture2D title_droid, title_parandroid, title_trsinrab;

    // Fade-in the Droid title screen from black.
    private class FadeBlackToDroid extends Effect {
        public void onRender(GL11 gl, float s, long t) {
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // A simple linear fade-in looks unnatural.
            s*=s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(title_droid);
        }
    }

    // Keep showing the Droid title screen.
    private class ShowDroidTitle extends Effect {
        public void onStart(GL11 gl) {
            gl.glColor4f(1, 1, 1, 1);
        }

        public void onRender(GL11 gl, float s, long t) {
            Helper.drawScreenSpaceTexture(title_droid);
        }
    }

    // Fade from the Droid to the TRSI 'N' RAB title screen.
    private class FadeDroidToTrsiNRab extends Effect {
        public void onRender(GL11 gl, float s, long t) {
            gl.glColor4f(1, 1, 1, 1);
            Helper.drawScreenSpaceTexture(title_droid);

            // A simple linear fade-in looks unnatural.
            s*=s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(title_trsinrab);
        }
    }

    // Keep showing the TRSI 'N' RAB title screen.
    private class ShowTrsiNRabTitle extends Effect {
        public void onStart(GL11 gl) {
            gl.glColor4f(1, 1, 1, 1);
        }

        public void onRender(GL11 gl, float s, long t) {
            Helper.drawScreenSpaceTexture(title_trsinrab);
        }
    }

    // Fade from the TRSI 'N' RAB to the Para 'N' droiD title screen.
    private class FadeTrsiNRabToParaNdroiD extends Effect {
        public void onRender(GL11 gl, float s, long t) {
            gl.glColor4f(1, 1, 1, 1);
            Helper.drawScreenSpaceTexture(title_trsinrab);

            // A simple linear fade-in looks unnatural.
            s*=s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(title_parandroid);
        }
    }

    // Keep showing the Para 'N' droiD title screen.
    private class ShowParaNdroiDTitle extends Effect {
        public void onStart(GL11 gl) {
            gl.glColor4f(1, 1, 1, 1);
        }

        public void onRender(GL11 gl, float s, long t) {
            Helper.drawScreenSpaceTexture(title_parandroid);
        }
    }

    // Fade-out the Para 'N' droiD title screen to white.
    private class FadeParaNdroiDToWhite extends Effect {
        public void onStart(GL11 gl) {
            gl.glClearColor(1, 1, 1, 1);
        }

        public void onRender(GL11 gl, float s, long t) {
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // A simple linear fade-in looks unnatural.
            s=1-s*s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(title_parandroid);
        }

        public void onStop(GL11 gl) {
            // Make sure the screen is finally full white
            // in case onRender() was not called with s=1.0.
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
    }

    public IntroFade(Activity activity, GL11 gl) {
        super(gl);

        // Load the title screens.
        title_droid=new Texture2D(gl);
        Bitmap bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.title_droid);
        title_droid.setData(bitmap);
        bitmap.recycle();

        title_parandroid=new Texture2D(gl);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.title_parandroid);
        title_parandroid.setData(bitmap);
        bitmap.recycle();

        title_trsinrab=new Texture2D(gl);
        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.title_trsinrab);
        title_trsinrab.setData(bitmap);
        bitmap.recycle();

        // Enable 2D texturing in general.
        title_droid.enable(true);

        // Set universal OpenGL states for all effects in this part.
        gl.glEnable(GL11.GL_BLEND);
        gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        // Schedule the effects in this part.
        add(new Clear(), 1000);

        add(new FadeBlackToDroid(), 7*1000);
        add(new ShowDroidTitle(), 7*1000);

        add(new FadeDroidToTrsiNRab(), 7*1000);
        add(new ShowTrsiNRabTitle(), 7*1000);

        add(new FadeTrsiNRabToParaNdroiD(), 7*1000);
        add(new ShowParaNdroiDTitle(), 7*1000);

        add(new FadeParaNdroiDToWhite(), 500);
    }
}
