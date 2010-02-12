package com.rabenauge.parandroid;

import javax.microedition.khronos.opengles.GL11;

/*
 * Base class for all objects that can be bound to the OpenGL state, i.e.
 * textures, vertex buffers, frame buffers etc.
 */
public abstract class Bindable {
    protected GL11 gl;
    protected int handle;

    Bindable(GL11 gl) {
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
