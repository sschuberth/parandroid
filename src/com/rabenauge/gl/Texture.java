package com.rabenauge.gl;

import javax.microedition.khronos.opengles.GL11;

/*
 * Base class for OpenGL texture objects.
 */
public abstract class Texture extends Bindable {
    protected int target, pname;

    protected Texture(GL11 gl, int target, int pname) {
        super(gl);

        this.target=target;
        this.pname=pname;

        // The default is GL_NEAREST_MIPMAP_LINEAR, which will result in white
        // textures if we do not have mipmaps. As we rarely use mipmaps, change the default.
        makeCurrent();
        gl.glTexParameterx(target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
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
