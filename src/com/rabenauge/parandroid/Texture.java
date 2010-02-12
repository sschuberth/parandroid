package com.rabenauge.parandroid;

import java.nio.IntBuffer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/*
 * Base class for OpenGL texture objects.
 */
public abstract class Texture extends Bindable {
    protected int target, pname;

    public static boolean isPOT(int x) {
        return x!=0 && (x&(x-1))==0;
    }

    public static int floorPOT(int x) {
        if (isPOT(x) || x==0) {
            return x;
        }

        long mask=(long)1<<31;
        while ((x&mask)==0 && mask>1) {
            mask>>=1;
        }
        return (int)mask;
    }

    public static int ceilPOT(int x) {
        if (isPOT(x) || x==0) {
            return x;
        }

        long mask=(long)1<<31;
        while ((x&mask)==0 && mask>1) {
            mask>>=1;
        }
        return (int)mask<<1;
    }

    Texture(GL11 gl, int target, int pname) {
        super(gl);

        this.target=target;
        this.pname=pname;

        // The default is GL_NEAREST_MIPMAP_LINEAR, which will result in white
        // textures if we do not have mipmaps. As we rarely use mipmaps, change the default.
        makeCurrent();
        gl.glTexParameterx(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    }

    public void enable(boolean state) {
        if (state) {
            gl.glEnable(target);
        }
        else {
            gl.glDisable(target);
        }
    }

    protected int create() {
        IntBuffer handle=IntBuffer.allocate(1);
        gl.glGenTextures(1, handle);
        return handle.get(0);
    }

    protected void destroy(int handle) {
        gl.glDeleteTextures(1, IntBuffer.allocate(1).put(handle));
    }

    public int getCurrent() {
        IntBuffer params=IntBuffer.allocate(1);
        gl.glGetIntegerv(pname, params);
        return params.get(0);
    }

    public void setCurrent(int handle) {
        gl.glBindTexture(target, handle);
    }

    public boolean isValid(int handle) {
        return gl.glIsTexture(handle);
    }
}
