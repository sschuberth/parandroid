package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rabenauge.demo.*;
import com.rabenauge.gl.*;

import javax.microedition.khronos.opengles.GL11;

public class RorschachFade extends EffectManager {
    private boolean in;
    private Texture2D rorschach[];

    // Fade from / to a Rorschach texture.
    private class FadeRorschach extends Effect {
        private Texture2D texture;

        public FadeRorschach(Texture2D texture) {
            this.texture=texture;
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            float a=in?s:1-s;
            gl.glColor4f(1, 1, 1, a);
            Helper.drawScreenSpaceTexture(texture);
            gl.glColor4f(1, 1, 1, 1);
        }
    }

    // Fade from one texture to another texture.
    private class FadeTexture extends Effect {
        private Texture2D from, to;

        public FadeTexture(Texture2D from, Texture2D to) {
            this.from=from;
            this.to=to;
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glColor4f(1, 1, 1, 1);
            Helper.drawScreenSpaceTexture(from);

            // A simple linear fade-in looks unnatural.
            s*=s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(to);
        }
    }

    public RorschachFade(Demo demo, GL11 gl, long t, boolean in) {
        super(gl);

        this.in=in;

        // Load the Rorschach textures.
        int[] ids={R.drawable.rorschach_1, R.drawable.rorschach_2, R.drawable.rorschach_3};
        rorschach=new Texture2D[ids.length];

        for (int i=0; i<ids.length; ++i) {
            Bitmap bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), ids[i]);
            rorschach[i]=new Texture2D(gl);
            rorschach[i].setData(bitmap);
            bitmap.recycle();
        }

        // Schedule the effects in this part.
        if (in) {
            add(new EffectManager.Wait(), Demo.DURATION_PART_STATIC-t);
            add(new FadeRorschach(rorschach[0]), t);
        }
        else {
            add(new FadeTexture(rorschach[0], rorschach[1]), t/3);
            add(new FadeTexture(rorschach[1], rorschach[2]), t/3);
            add(new FadeRorschach(rorschach[2]), t/3);
        }
    }
}
