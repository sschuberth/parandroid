package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/*
 * Wrapper class for 2D texture objects.
 */
public class Texture2D extends Texture {
    Texture2D(GL11 gl) {
        super(gl, GL10.GL_TEXTURE_2D, GL11.GL_TEXTURE_BINDING_2D);
    }

    public void setData(Bitmap bitmap, int level, boolean border) {
        makeCurrent();
        if (!isPOT(bitmap.getWidth()) || !isPOT(bitmap.getHeight())) {
            bitmap=Bitmap.createScaledBitmap(bitmap, ceilPOT(bitmap.getWidth()), ceilPOT(bitmap.getHeight()), false);
        }
        android.opengl.GLUtils.texImage2D(target, level, bitmap, border?1:0);
    }

    public void setData(Bitmap bitmap) {
        setData(bitmap, 0, false);
    }
}
