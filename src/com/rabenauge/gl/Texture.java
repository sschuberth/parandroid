package com.rabenauge.gl;

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

    protected Texture(GL11 gl, int target, int pname) {
        super(gl);

        this.target=target;
        this.pname=pname;

        // The default is GL_NEAREST_MIPMAP_LINEAR, which will result in white
        // textures if we do not have mipmaps. As we rarely use mipmaps, change the default.
        makeCurrent();
        gl.glTexParameterx(target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
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
        int[] textures=new int[1];
        gl.glGenTextures(1, textures, 0);
        return textures[0];
    }

    protected void destroy(int handle) {
        int[] textures={handle};
        gl.glDeleteTextures(1, textures, 0);
    }

    public int getCurrent() {
        int[] params=new int[1];
        gl.glGetIntegerv(pname, params, 0);
        return params[0];
    }

    public void setCurrent(int handle) {
        gl.glBindTexture(target, handle);
    }

    public boolean isValid(int handle) {
        return gl.glIsTexture(handle);
    }
}
