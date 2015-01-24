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

import javax.microedition.khronos.opengles.GL11;

/*
 * Base class for all objects that can be bound to the OpenGL state, i.e.
 * textures, vertex buffers, frame buffers etc.
 */
public abstract class Bindable {
    public GL11 gl;
    protected int handle;

    protected Bindable(GL11 gl) {
        this.gl=gl;
        handle=create();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        destroy(handle);
    }

    protected abstract int create();
    protected abstract void destroy(int handle);

    public abstract int getCurrent();
    public abstract void setCurrent(int handle);

    public void makeCurrent() {
        setCurrent(handle);
    }

    public abstract boolean isValid(int handle);
}
