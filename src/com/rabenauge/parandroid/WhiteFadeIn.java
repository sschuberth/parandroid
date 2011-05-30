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
