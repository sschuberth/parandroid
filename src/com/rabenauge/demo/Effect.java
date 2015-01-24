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
