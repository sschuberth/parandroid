package com.rabenauge.demo;

import java.nio.*;

/*
 * A class to create directly allocated buffers of native byte order.
 */
public class DirectBuffer {
    public static ByteBuffer nativeByteBuffer(int capacity) {
        ByteBuffer buffer=ByteBuffer.allocateDirect(capacity);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    public static ByteBuffer nativeByteBuffer(byte[] array) {
        ByteBuffer buffer=nativeByteBuffer(array.length);
        buffer.put(array).position(0);
        return buffer;
    }

    public static ShortBuffer nativeShortBuffer(int capacity) {
        return nativeByteBuffer(capacity*Short.SIZE>>3).asShortBuffer();
    }

    public static ShortBuffer nativeShortBuffer(short[] array) {
        ShortBuffer buffer=nativeShortBuffer(array.length);
        buffer.put(array).position(0);
        return buffer;
    }

    public static IntBuffer nativeIntBuffer(int capacity) {
        return nativeByteBuffer(capacity*Integer.SIZE>>3).asIntBuffer();
    }

    public static IntBuffer nativeIntBuffer(int[] array) {
        IntBuffer buffer=nativeIntBuffer(array.length);
        buffer.put(array).position(0);
        return buffer;
    }

    public static FloatBuffer nativeFloatBuffer(int capacity) {
        return nativeByteBuffer(capacity*Float.SIZE>>3).asFloatBuffer();
    }

    public static FloatBuffer nativeFloatBuffer(float[] array) {
        FloatBuffer buffer=nativeFloatBuffer(array.length);
        buffer.put(array).position(0);
        return buffer;
    }
}
