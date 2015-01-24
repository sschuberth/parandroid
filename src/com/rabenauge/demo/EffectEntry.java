/*
 * Copyright 2010-2011 bodo, eyebex, ralph, spotter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rabenauge.demo;

import javax.microedition.khronos.opengles.GL11;

/*
 * An entry in a list of demo effects.
 */
public class EffectEntry {
    protected Effect effect;
    protected long start, duration;
    protected boolean running;

    // Schedule an effect which is not running initially.
    public EffectEntry(Effect effect, long start, long duration) {
        this.effect=effect;
        this.start=start;
        this.duration=duration;
        running=false;
    }

    // Returns the effect.
    public Effect getEffect() {
        return effect;
    }

    // Returns the effect's start time.
    public long getStart() {
        return start;
    }

    // Returns the effect's duration.
    public long getDuration() {
        return duration;
    }

    // Returns whether the effect is current for the given time.
    public boolean isCurrent(long t) {
        return t>=start && t<start+duration;
    }

    // Returns whether the effect is currently running.
    public boolean isRunning() {
        return running;
    }

    // Tells the effect to start running.
    public void startRunning(GL11 gl) {
        if (!running) {
            effect.onStart(gl);
            running=true;
        }
    }

    // Tells the effect to stop running.
    public void stopRunning(GL11 gl) {
        if (running) {
            effect.onStop(gl);
            running=false;
        }
    }
}
