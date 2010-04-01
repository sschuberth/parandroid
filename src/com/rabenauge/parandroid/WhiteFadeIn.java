package com.rabenauge.parandroid;

import com.rabenauge.demo.*;
import javax.microedition.khronos.opengles.GL11;

public class WhiteFadeIn extends EffectManager {
    public WhiteFadeIn(Demo demo, GL11 gl, long t) {
        super(gl);

        // Schedule the effects in this part.
        add(new EffectManager.ColorFade(1, 1, 1, true), t);
    }
}
