package com.rabenauge.demo;

import java.util.LinkedList;
import java.util.ListIterator;
import javax.microedition.khronos.opengles.GL11;

/*
 * Simple class to manage a list of effects.
 */
public class EffectManager {
    public static final float PI=(float)Math.PI;

    /*
     * A helper effect that just clears the screen.
     */
    public class Clear extends Effect {
        public void onRender(GL11 gl, float s, long t) {
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
    }

    /*
     * A dummy effect to wait for some time.
     */
    public class Wait extends Effect {
        public void onRender(GL11 gl, float s, long t) {
            // Do nothing.
        }
    }

    /*
     * An entry in the list of effects.
     */
    private class Entry {
        private Effect effect;
        private long start, duration;

        public Entry(Effect effect, long start, long duration) {
            this.effect=effect;
            this.start=start;
            this.duration=duration;
        }

        public boolean isCurrent(long t) {
            return t>=start && t<start+duration;
        }
    }

    private GL11 gl;
    private LinkedList<Entry> effects;
    private int curr_index;
    private long t_last;

    public EffectManager(GL11 gl) {
        this.gl=gl;

        effects=new LinkedList<Entry>();
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
        effects.add(new Entry(effect, getDuration(), duration));
    }

    public boolean play(long t) {
        if (curr_index<0) {
            // There is no current effect yet, so start the first one.
            effects.getFirst().effect.startRunning(gl);
            curr_index=0;
        }

        // Starting with the current one, iterate through the list of effects.
        ListIterator<Entry> i=effects.listIterator(curr_index);
        while (i.hasNext()) {
            Entry entry=i.next();
            if (entry.isCurrent(t)) {
                int index=effects.indexOf(entry);
                if (index!=curr_index) {
                    // It is time to change effects, so stop the current one ...
                    effects.get(curr_index).effect.stopRunning(gl);
                    // ... and start the new one.
                    entry.effect.startRunning(gl);

                    curr_index=index;
                }

                // After rendering the current effect state we are done.
                entry.effect.onRender(gl, (float)(t-entry.start)/entry.duration, t-t_last);
                t_last=t;
                return true;
            }
        }

        // No match was found, so end the last effect.
        effects.getLast().effect.stopRunning(gl);
        return false;
    }
}
