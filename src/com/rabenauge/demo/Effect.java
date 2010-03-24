package com.rabenauge.demo;

import javax.microedition.khronos.opengles.GL11;

/*
 * A single demo effect.
 */
public abstract class Effect {
    // Called once when the effects should start.
    public void onStart(GL11 gl) {}

    // Called each time the effect should render. Given are the total time, the elapsed time since the last call, and the effect percentage.
    public abstract void onRender(GL11 gl, long t, long e, float s);

    // Called once when the effects should stop.
    public void onStop(GL11 gl) {}
}
