package com.rabenauge.parandroid;

import android.opengl.GLU;
import android.hardware.*;

import com.rabenauge.demo.*;
import java.nio.IntBuffer;
import javax.microedition.khronos.opengles.GL11;

public class StarField extends EffectManager {
    private static final int WIDTH=800*65536, HEIGHT=480*65536;

    private static final int DEF_CENTER_X=WIDTH/2, DEF_CENTER_Y=HEIGHT/2;
    private int center_x=DEF_CENTER_X, center_y=DEF_CENTER_Y;

    private IntBuffer star_coords;
    private IntBuffer star_speeds;

    private SensorManager sm;
    public Flight flight;

    public class Flight extends Effect implements SensorEventListener {
        public boolean interactive=false;

        private static final float TOLERANCE=1.2f;
        private long t_last=-1;

        public void onStart(GL11 gl) {
            gl.glEnable(GL11.GL_LINE_SMOOTH);

            // Using TYPE_ALL here does *not* work to listen to all sensors.
            Sensor sensor=sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            for (int i=0; i<star_coords.capacity(); i+=4) {
                float factor=star_speeds.get(i)*e/500;

                // Avoid flickering of small stars by making the lines at least two pixels long.
                int x=star_coords.get(i), y=star_coords.get(i+1);
                star_coords.put(i+2, x + (int)((x-center_x)*factor/65536.0f));
                star_coords.put(i+3, y + (int)((y-center_y)*factor/65536.0f));
            }

            // Set OpenGL states.
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            // Set the projection to match the star coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH/65536.0f, HEIGHT/65536.0f, 0);

            gl.glColorPointer(4, GL11.GL_FIXED, 0, star_speeds);
            gl.glVertexPointer(2, GL11.GL_FIXED, 0, star_coords);
            gl.glDrawArrays(GL11.GL_LINES, 0, star_coords.capacity()/2);

            // Restore OpenGL states.
            gl.glPopMatrix();

            gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
            gl.glEnable(GL11.GL_TEXTURE_2D);

            // Move the stars.
            for (int i=0; i<star_coords.capacity(); i+=4) {
                star_coords.put(i  , star_coords.get(i+2));
                star_coords.put(i+1, star_coords.get(i+3));

                int x=star_coords.get(i), y=star_coords.get(i+1);
                if (x<0 || x>=WIDTH || y<0 || y>=HEIGHT) {
                    star_coords.put(i  , (int)(DemoMath.randomize(WIDTH , center_x)));
                    star_coords.put(i+1, (int)(DemoMath.randomize(HEIGHT, center_y)));
                }
            }
        }

        public void onStop(GL11 gl) {
            sm.unregisterListener(this);

            gl.glDisable(GL11.GL_LINE_SMOOTH);
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
            v=DEF_CENTER_X-v*65536*25;
            center_x=(center_x+(int)v)/2;

            // Converge to the desired pitch.
            float h=event.values[2];
            if (Math.abs(h)<TOLERANCE) {
                h=0;
            }
            h=DEF_CENTER_Y+h*65536*15;
            center_y=(center_y+(int)h)/2;
        }
    }

    public StarField(Demo demo, GL11 gl, int count) {
        super(gl);

        sm=demo.getSensorManager();

        // Stores x, y per star vertex.
        star_coords=DirectBuffer.nativeIntBuffer(count*2*2);
        // Stores r, g, b, a per star vertex.
        star_speeds=DirectBuffer.nativeIntBuffer(count*4*2);

        for (int c=0, s=0; c<star_coords.capacity(); c+=4, s+=8) {
            star_coords.put(c  , (int)(DemoMath.randomize(WIDTH , center_x)));
            star_coords.put(c+1, (int)(DemoMath.randomize(HEIGHT, center_y)));

            // The speed is also used as the grayscale color.
            star_speeds.put(s  , (int)(DemoMath.randomize(1, 0)*65536));
            star_speeds.put(s+1, star_speeds.get(s));
            star_speeds.put(s+2, star_speeds.get(s));
            star_speeds.put(s+3, (int)(1.0f*65536));

            star_speeds.put(s+4, star_speeds.get(s  ));
            star_speeds.put(s+5, star_speeds.get(s+1));
            star_speeds.put(s+6, star_speeds.get(s+2));
            star_speeds.put(s+7, star_speeds.get(s+3));
        }

        // Schedule the effects in this part.
        flight=new Flight();
        add(flight, Demo.DURATION_MAIN_EFFECTS);
    }
}
