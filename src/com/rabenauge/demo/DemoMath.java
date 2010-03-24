package com.rabenauge.demo;

/*
 * Math routines for demo effects.
 */
public class DemoMath {
    // PI as float.
    public static final float PI=(float)Math.PI;

    // Returns whether x is a power of two or not.
    public static boolean isPOT(int x) {
        return x!=0 && (x&(x-1))==0;
    }

    // Returns the smallest power of two that is greater than or equal to x.
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

    // Returns the largest power of two that is smaller than or equal to x.
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

    // Returns a random float number != invalid in range [0, factor[.
    public static float randomize(float factor, float invalid) {
        float r;
        do {
            r=(float)(Math.random()*factor);
        }
        while (r==invalid); 
        return r;
    }
}
