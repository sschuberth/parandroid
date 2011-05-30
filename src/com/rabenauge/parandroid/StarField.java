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

    private Demo demo;

    private ShortBuffer star_coords;
    private IntBuffer star_speeds;

    private int hidden_stars;

    private SensorManager sm;
    public Flight flight;

    public boolean isHidden() {
        return hidden_stars==star_coords.capacity()/4;
    }

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
            if (demo.shootem) {
                // Make the remaining stars move faster off-screen.
                e*=2;

                if (isHidden()) {
                    // Do nothing if we are in the "Shoot'em!" mode and
                    // the effect is already completely hidden.
                    return;
                }
            }

            gl.glEnable(GL11.GL_LINE_SMOOTH);

            // Move the stars.
            for (int i=0; i<star_coords.capacity(); i+=4) {
                float factor=star_speeds.get(i)/65536.0f*e/500.0f;

                if ((star_coords.get(i)==-1 && star_coords.get(i+1)==-1)
                 || (demo.shootem && star_speeds.get(i)<0.1f*65536))
                {
                    star_coords.put(i  ,(short)-1);
                    star_coords.put(i+1,(short)-1);
                    star_coords.put(i+2,(short)-1);
                    star_coords.put(i+3,(short)-1);
                }
                else {
                    star_coords.put(i+2,(short)(star_coords.get(i  ) + (star_coords.get(i  )-center_x)*factor));
                    star_coords.put(i+3,(short)(star_coords.get(i+1) + (star_coords.get(i+1)-center_y)*factor));
                }
            }

            // Set OpenGL states.
            gl.glDisable(GL11.GL_TEXTURE_2D);
            gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

            // Set the projection to match the star coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            gl.glColorPointer(4, GL11.GL_FIXED, 0, star_speeds);
            gl.glVertexPointer(2, GL11.GL_SHORT, 0, star_coords);
            gl.glDrawArrays(GL11.GL_LINES, 0, star_coords.capacity()/2);

            // Restore OpenGL states.
            gl.glPopMatrix();

            gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
            gl.glEnable(GL11.GL_TEXTURE_2D);

            // Discard hidden stars and regenerate visible stars.
            float rand_w=WIDTH, rand_h=HEIGHT;
            if (isHidden()){
                // If all stars are hidden, regenerate visible ones
                // only close to the center.
                rand_w/=10;
                rand_h/=10;
            }

            hidden_stars=0;

            for (int i=0; i<star_coords.capacity(); i+=4) {
                star_coords.put(i  ,star_coords.get(i+2));
                star_coords.put(i+1,star_coords.get(i+3));
                if (star_coords.get(i)<0 || star_coords.get(i)>=WIDTH
                 || star_coords.get(i+1)<0 || star_coords.get(i+1)>=HEIGHT)
                {
                    ++hidden_stars;

                    if (!demo.shootem) {
                        // Subtract some safety value from rand_{w|h} to not be out of bounds again
                        // immediately due to rounding errors.
                        star_coords.put(i  ,(short)(center_x + DemoMath.randomize(rand_w-5, rand_w/2)-rand_w/2));
                        star_coords.put(i+1,(short)(center_y + DemoMath.randomize(rand_h-5, rand_h/2)-rand_h/2));
                    }
                    else {
                        star_coords.put(i  ,(short)-1);
                        star_coords.put(i+1,(short)-1);
                    }
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

        this.demo=demo;

        sm=demo.getSensorManager();

        // Stores x, y per star vertex.
        star_coords=DirectBuffer.nativeShortBuffer(count*2*2);
        // Stores r, g, b, a per star vertex.
        star_speeds=DirectBuffer.nativeIntBuffer(count*4*2);

        for (int c=0, s=0; c<star_coords.capacity(); c+=4, s+=8) {
            star_coords.put(c  ,(short)(DemoMath.randomize(WIDTH , center_x)));
            star_coords.put(c+1,(short)(DemoMath.randomize(HEIGHT, center_y)));

            // The speed is also used as the grayscale color.
            int color=(int)(DemoMath.randomize(1, 0)*65536);

            star_speeds.put(s  ,color/4);
            star_speeds.put(s+1,color/4);
            star_speeds.put(s+2,color/4);
            star_speeds.put(s+3,(int)(1.0f*65536));

            star_speeds.put(s+4,color);
            star_speeds.put(s+5,color);
            star_speeds.put(s+6,color);
            star_speeds.put(s+7,star_speeds.get(s+3));
        }

        // Schedule the effects in this part.
        flight=new Flight();
        add(flight, Demo.DURATION_MAIN_EFFECTS);
    }
}
