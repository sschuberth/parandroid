package com.rabenauge.parandroid;

import android.opengl.GLU;
import android.hardware.*;

import com.rabenauge.demo.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL11;

public class StarField extends EffectManager {
    private static final int WIDTH=800, HEIGHT=480;

    private static final int DEF_CENTER_X=WIDTH/2, DEF_CENTER_Y=HEIGHT/2;
    private int center_x=DEF_CENTER_X, center_y=DEF_CENTER_Y;

    private short[] star_coords;
    private int[] star_speeds;

    private SensorManager sm;
    public Flight flight;

    public class Flight extends Effect implements SensorEventListener {
        public boolean interactive=false;

        private static final float TOLERANCE=1.2f;
        private long t_last=-1;

        public void onStart(GL11 gl) {
            // Using TYPE_ALL here does *not* work to listen to all sensors.
            Sensor sensor=sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            gl.glEnable(GL11.GL_LINE_SMOOTH);

            for (int i=0; i<star_coords.length; i+=4) {
                float factor=star_speeds[i]/65536.0f*e/500.0f;

                // Avoid flickering of small stars by making the lines at least two pixels long.
                star_coords[i+2]=(short)(star_coords[i  ] + (star_coords[i  ]-center_x)*factor);
                star_coords[i+3]=(short)(star_coords[i+1] + (star_coords[i+1]-center_y)*factor);
            }

            // Set OpenGL states.
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            // Set the projection to match the star coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            gl.glColorPointer(4, GL11.GL_FIXED, 0, IntBuffer.wrap(star_speeds));
            gl.glVertexPointer(2, GL11.GL_SHORT, 0, ShortBuffer.wrap(star_coords));
            gl.glDrawArrays(GL11.GL_LINES, 0, star_coords.length/2);

            // Restore OpenGL states.
            gl.glPopMatrix();

            gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
            gl.glEnable(GL11.GL_TEXTURE_2D);

            // Move the stars.
            for (int i=0; i<star_coords.length; i+=4) {
                star_coords[i  ]=star_coords[i+2];
                star_coords[i+1]=star_coords[i+3];
                if (star_coords[i  ]<0 || star_coords[i  ]>=WIDTH
                 || star_coords[i+1]<0 || star_coords[i+1]>=HEIGHT) 
                {
                    star_coords[i  ]=(short)(DemoMath.randomize(WIDTH , center_x));
                    star_coords[i+1]=(short)(DemoMath.randomize(HEIGHT, center_y));
                }
            }

            gl.glDisable(GL11.GL_LINE_SMOOTH);
        }

        public void onStop(GL11 gl) {
            sm.unregisterListener(this);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            int type=event.sensor.getType();

            if (type!=Sensor.TYPE_ORIENTATION || !interactive) {
                return;
            }

            // Only allow one update every 100ms, otherwise updates
            // come way too fast and the phone gets bogged down
            // with garbage collection, see
            // http://stuffthathappens.com/blog/2009/03/15/android-accelerometer/
            long t=android.os.SystemClock.uptimeMillis(), t_diff=t-t_last;
            if (t_last!=-1 && t_diff<100) {
                return;
            }
            t_last=t;

            // Converge to the desired yaw.
            float v=event.values[1];
            if (Math.abs(v)<TOLERANCE) {
                v=0;
            }
            v=DEF_CENTER_X-v*25;
            center_x=(short)((center_x+v)/2);

            // Converge to the desired pitch.
            float h=event.values[2];
            if (Math.abs(h)<TOLERANCE) {
                h=0;
            }
            h=DEF_CENTER_Y+h*15;
            center_y=(short)((center_y+h)/2);
        }
    }

    public StarField(Demo demo, GL11 gl, int count) {
        super(gl);

        sm=demo.getSensorManager();

        // Stores x, y per star vertex.
        star_coords=new short[count*2*2];
        // Stores r, g, b, a per star vertex.
        star_speeds=new int[count*4*2];

        for (int c=0, s=0; c<star_coords.length; c+=4, s+=8) {
            star_coords[c  ]=(short)(DemoMath.randomize(WIDTH , center_x));
            star_coords[c+1]=(short)(DemoMath.randomize(HEIGHT, center_y));

            // The speed is also used as the grayscale color.
            int color=(int)(DemoMath.randomize(1, 0)*65536);

            star_speeds[s  ]=color/4;
            star_speeds[s+1]=color/4;
            star_speeds[s+2]=color/4;
            star_speeds[s+3]=(int)(1.0f*65536);

            star_speeds[s+4]=color;
            star_speeds[s+5]=color;
            star_speeds[s+6]=color;
            star_speeds[s+7]=star_speeds[s+3];
        }

        // Schedule the effects in this part.
        flight=new Flight();
        add(flight, Demo.DURATION_MAIN_EFFECTS);
    }
}
