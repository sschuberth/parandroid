package com.rabenauge.gl;

import javax.microedition.khronos.opengles.GL11;

/*
 * Wrapper class for point sprites (requires GL_OES_point_sprite).
 */
public class PointSprite extends Texture2D {
    public PointSprite(GL11 gl) {
        super(gl);

        gl.glTexEnvx(GL11.GL_POINT_SPRITE_OES, GL11.GL_COORD_REPLACE_OES, 1);

        gl.glTexParameterx(target, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(target, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
    }

    public void setCoordReplace(boolean state) {
        makeCurrent();
        gl.glTexEnvx(GL11.GL_POINT_SPRITE_OES, GL11.GL_COORD_REPLACE_OES, state?1:0);
    }

    public void setSize(float size) {
        gl.glPointSize(size);
    }
}
