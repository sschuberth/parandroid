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

package com.rabenauge.gl;

import android.graphics.Bitmap;
import com.rabenauge.demo.DemoMath;
import javax.microedition.khronos.opengles.GL11;

/*
 * Wrapper class for 2D texture objects.
 */
@SuppressWarnings("SameParameterValue")
public class Texture2D extends Texture {
    public Texture2D(GL11 gl) {
        super(gl, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BINDING_2D);
    }

    public void setData(Bitmap bitmap, int level, boolean border) {
        int w=bitmap.getWidth(), h=bitmap.getHeight();
        int w2=DemoMath.ceilPOT(w), h2=DemoMath.ceilPOT(h);
        if (w!=w2 || h!=h2) {
            bitmap=Bitmap.createScaledBitmap(bitmap, w2, h2, true);
        }
        makeCurrent();
        android.opengl.GLUtils.texImage2D(target, level, bitmap, border?1:0);
    }

    public void setData(Bitmap bitmap) {
        setData(bitmap, 0, false);
    }
}
