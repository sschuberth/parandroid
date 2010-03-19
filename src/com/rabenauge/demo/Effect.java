package com.rabenauge.demo;

import javax.microedition.khronos.opengles.GL11;

/*
 * Base class for a single demo effect.
 */
public abstract class Effect {
    protected boolean running;

    // Initializes the effect to be not running.
    public Effect() {
        running=false;
    }

    // Returns whether the effect is currently running.
    public boolean isRunning() {
        return running;
    }

    // Tells the effect to start running.
    public final void startRunning(GL11 gl) {
        if (!running) {
            onStart(gl);
            running=true;
        }
    }

    // Called once when the effects should start.
    public void onStart(GL11 gl) {}

    // Called with s in range [0, 1] to render that effect percentage.
    public abstract void onRender(GL11 gl, float s);

    // Tells the effect to stop running.
    public final void stopRunning(GL11 gl) {
        if (running) {
            onStop(gl);
            running=false;
        }
    }

    // Called once when the effects should stop.
    public void onStop(GL11 gl) {}
}
