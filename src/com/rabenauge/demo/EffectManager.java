package com.rabenauge.demo;

import java.util.LinkedList;
import java.util.ListIterator;
import javax.microedition.khronos.opengles.GL11;

/*
 * A simple manager for a list of demo effects.
 */
public class EffectManager {
    /*
     * A helper effect that just clears the screen.
     */
    public class Clear extends Effect {
        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
    }

    /*
     * A dummy effect to wait for some time.
     */
    public class Wait extends Effect {
        public void onRender(GL11 gl, long t, long e, float s) {
            // Do nothing.
        }
    }

    private GL11 gl;
    private LinkedList<EffectEntry> effects;
    private int curr_index;
    private long t_last;

    public EffectManager(GL11 gl) {
        this.gl=gl;

        effects=new LinkedList<EffectEntry>();
        curr_index=-1;
        t_last=0;
    }

    public long getDuration() {
        long duration=0;
        if (!effects.isEmpty()) {
            duration=effects.getLast().start+effects.getLast().duration;
        }
        return duration;
    }

    public void add(Effect effect, long duration) {
        effects.add(new EffectEntry(effect, getDuration(), duration));
    }

    public boolean play(long t) {
        if (curr_index<0) {
            // There is no current effect yet, so start the first one.
            effects.getFirst().startRunning(gl);
            curr_index=0;
        }

        // Starting with the current one, iterate through the list of effects.
        ListIterator<EffectEntry> i=effects.listIterator(curr_index);
        while (i.hasNext()) {
            EffectEntry entry=i.next();
            if (entry.isCurrent(t)) {
                int index=effects.indexOf(entry);
                if (index!=curr_index) {
                    // It is time to change effects, so stop the current one ...
                    effects.get(curr_index).stopRunning(gl);
                    // ... and start the new one.
                    entry.startRunning(gl);

                    curr_index=index;
                }

                // After rendering the current effect state we are done.
                long t_local=t-entry.start;
                entry.effect.onRender(gl, t_local, t-t_last, (float)t_local/entry.duration);
                t_last=t;
                return true;
            }
        }

        // No match was found, so end the last effect.
        effects.getLast().stopRunning(gl);
        return false;
    }
}
