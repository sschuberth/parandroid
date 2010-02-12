package com.rabenauge.parandroid;

import javax.microedition.khronos.opengles.GL11;

/*
 * Wrapper class for point sprites (requires GL_OES_point_sprite).
 */
public class PointSprite extends Texture2D {
    PointSprite(GL11 gl) {
        super(gl);
    }

    public void enable(boolean state) {
        super.enable(state);
        if (state) {
            gl.glEnable(GL11.GL_POINT_SPRITE_OES);
            gl.glTexEnvx(GL11.GL_POINT_SPRITE_OES, GL11.GL_COORD_REPLACE_OES, 1);
        }
        else {
            gl.glDisable(GL11.GL_POINT_SPRITE_OES);
            gl.glTexEnvx(GL11.GL_POINT_SPRITE_OES, GL11.GL_COORD_REPLACE_OES, 0);
        }
    }

    public void setSize(float size) {
        gl.glPointSize(size);
    }
}
