package com.rabenauge.demo;

import com.rabenauge.gl.Helper;
import com.rabenauge.gl.Texture2D;
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

    /*
     * An effect to fade the current screen contents from / to a given texture.
     */
    public class TextureFade extends Effect {
        private Texture2D texture;
        private boolean in;

        public TextureFade(Texture2D texture, boolean in) {
            this.texture=texture;
            this.in=in;
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            float a=in?s:1-s;
            gl.glColor4f(1, 1, 1, a);
            Helper.drawScreenSpaceTexture(texture);

            // We need to restore this immediately for other concurrently running effects.
            gl.glColor4f(1, 1, 1, 1);
        }
    }

    /*
     * An effect to fade from one texture to another texture.
     */
    public class TextureTransition extends Effect {
        private Texture2D from, to;

        public TextureTransition(Texture2D from, Texture2D to) {
            this.from=from;
            this.to=to;
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            Helper.drawScreenSpaceTexture(from);

            // A simple linear fade-in looks unnatural.
            s*=s;

            gl.glColor4f(1, 1, 1, s);
            Helper.drawScreenSpaceTexture(to);
        }

        public void onStop(GL11 gl) {
            // Just in case onRender() was not called with exactly s=1.
            gl.glColor4f(1, 1, 1, 1);
        }
    }

    /*
     * An effect to keep showing the given texture.
     */
    public class TextureShow extends Effect {
        private Texture2D title;

        public TextureShow(Texture2D title) {
            this.title=title;
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            Helper.drawScreenSpaceTexture(title);
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
