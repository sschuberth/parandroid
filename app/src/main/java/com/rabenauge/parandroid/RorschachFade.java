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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rabenauge.demo.*;
import com.rabenauge.gl.*;

import javax.microedition.khronos.opengles.GL11;

public class RorschachFade extends EffectManager {
    public RorschachFade(Demo demo, GL11 gl, long t, boolean in) {
        super(gl);

        // Load the Rorschach textures.
        int[] ids={R.drawable.rorschach_1, R.drawable.rorschach_2, R.drawable.rorschach_3};
        Texture2D rorschach[]=new Texture2D[ids.length];

        for (int i=0; i<ids.length; ++i) {
            Bitmap bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), ids[i]);
            rorschach[i]=new Texture2D(gl);
            rorschach[i].setData(bitmap);
            bitmap.recycle();
        }

        // Schedule the effects in this part.
        if (in) {
            add(new EffectManager.Wait(), Demo.DURATION_PART_STATIC-t);
            add(new EffectManager.TextureFade(rorschach[0], in), t);
        }
        else {
            add(new EffectManager.TextureTransition(rorschach[0], rorschach[1]), t/3);
            add(new EffectManager.TextureTransition(rorschach[1], rorschach[2]), t/3);
            add(new EffectManager.TextureFade(rorschach[2], in), t/3);
        }
    }
}
