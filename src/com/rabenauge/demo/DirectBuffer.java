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
