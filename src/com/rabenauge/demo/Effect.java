package com.rabenauge.demo;

import javax.microedition.khronos.opengles.GL11;

/*
 * Base class for a single demo effect.
 */
public abstract class Effect {
    // Called once when the effects should start.
    public void onStart(GL11 gl) {}

    // Called with s in range [0, 1] to render that effect percentage.
    public abstract void onRender(GL11 gl, float s);

    // Called once when the effects should stop.
    public void onStop(GL11 gl) {}
}
