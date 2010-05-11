package com.rabenauge.parandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.*;
import android.opengl.GLU;
import android.util.FloatMath;
import com.rabenauge.demo.*;
import com.rabenauge.gl.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL11;

public class Scroller extends EffectManager {
    private static final int WIDTH=800, HEIGHT=480;
    private static final int SCROLLER_POS_Y=360;
    private static final int CHAR_SIZE=2+66+2;
    private static final int SHADOW_OFFSET=12;

    private static final String CHARS="abcdefghijklmnopqrstuvwxyz.:-,`´!»«+";
    private static final String TEXT=
        "               " +
        "This is the Party Version. Squeezed and tweaked in part at the hotel and hall in Bingen: " +
        "Another firstie for the pleasure of Breakpoint Nation:   Para´N`droiD.   The initial fully " +
        "interactive mobile Android platform cell phone demo. Presented to you by Rabenauge: Beauty lies in " +
        "the Eye of the Raven. Plus Tristar and Red Sector Incorporated: Twice the Fun - Double the Trouble. " +
        "     " +
        "Twenty Five Years of RSI. Twenty Years of TRSI. We are the sleeping Gods. " +
        "We salute all, who built the heritage. Values never die. Spotter says: Boogie down Brookline." +
        "This demo is for my Princess, the Beautiful and Irata, the Founder." +
        "     " +
        "Handshakes to Titus: Thanks man. And Happy Birthday to TRSI from Rabenauge! " +
        "     " +
        "Golden Greetings fly to: " +
        "ACS - ALCATRAZ - ANDROMEDA SOFTWARE DEVELOPMENT - ATE BIT - BAUKNECHT - BITFELLAS - " +
        "BIRDHOUSE PROJECTS - BRAINSTORM - CONSPIRACY - COOCOON - DANISH GOLD - DRIFTERS - " +
        "EQUINOX - FAIRLIGHT - FTC - GENESIS - GNUMPF POSSE - HAUJOBB - HITMEN - HOODLUM - HOTSTUFFERS - " +
        "KALISTO - K TWO - KEFRENS - NEOSCIENTISTS - PARADISE - PARADOX - POLKA BROTHERS - RGBA - " +
        "REBELS - RAZOR NINETEEN ELEVEN - REMEDY - ROYAL BELGIAN BEER SQUADRON - SCARAB - SCOOPEX - " +
        "SECTION EIGHT - SPACEBALLS - SPECKDRUMM - SPEEDQUEEN - TEK - STILL - THE BLACK LOTUS - " +
        "THE LIGHTFORCE - THE SILENTS - TITAN - THE PLANET OF LEATHER MOOMINS - UK ALLSTARS - and Irata." +
        " --- ";

    private Texture2D charset;
    private IntBuffer tex_coords;
    private ShortBuffer line_coords;

    private SensorManager sm;
    public Scroll scroll;

    private void calcTexCoords(String text, int tex_width, int tex_height, int chars_per_row, int chars_per_column) {
        text=text.toLowerCase();

        int char_width=tex_width/chars_per_row;
        int char_height=tex_height/chars_per_column;

        // Every char consists of char_width vertical lines which have
        // a start and end vertex with u / v coordinates each.
        int num_coords=text.length()*char_width*2*2;

        tex_coords=IntBuffer.allocate(num_coords);

        for (int i=0; i<text.length(); ++i) {
            int pos=CHARS.indexOf(text.charAt(i));
            if (pos>=0) {
                pos*=char_width;

                int x=pos%tex_width, y=pos/tex_width*char_height;
                float u, v0=(float)y/tex_height, v1=(float)(y+char_height-1)/tex_height;

                for (int c=0; c<char_width; ++c) {
                    u=(float)(x+c)/tex_width;

                    int ui=(int)(u*65536);
                    int v0i=(int)(v0*65536), v1i=(int)(v1*65536);

                    // Start vertex u / v.
                    tex_coords.put(ui);
                    tex_coords.put(v0i);

                    // End vertex u / v.
                    tex_coords.put(ui);
                    tex_coords.put(v1i);
                }
            }
            else {
                for (int c=0; c<char_width; ++c) {
                    // Start vertex u / v.
                    tex_coords.put(0);
                    tex_coords.put(0);

                    // End vertex u / v.
                    tex_coords.put(0);
                    tex_coords.put(0);
                }
            }
        }

        tex_coords.rewind();
    }

    public class Scroll extends Effect implements SensorEventListener {
        private IntBuffer sliding_tex_coords;
        private ShortBuffer wobbling_line_coords;

        public boolean interactive=false;

        private static final float TOLERANCE=1.2f;
        private long t_last=-1;

        private static final float DEF_SPEED=2.0f, DEF_AMP=25.0f;
        private float speed=DEF_SPEED, amp=DEF_AMP;
        private int pos=0;

        public Scroll() {
            sliding_tex_coords=IntBuffer.allocate(tex_coords.capacity()+WIDTH*2*2);
            sliding_tex_coords.put(tex_coords);
            tex_coords.rewind();
            for (int i=0; i<WIDTH*2*2; ++i) {
                sliding_tex_coords.put(tex_coords.get(i));
            }
            // No need to rewind sliding_tex_coords.

            wobbling_line_coords=ShortBuffer.allocate(line_coords.capacity());

            int t=Math.round(TEXT.length()/speed);
            android.util.Log.i(Demo.NAME,
                "Scrolling will take " + String.valueOf(t/60) + ":" + String.valueOf(t%60) + "m (" + String.valueOf(t)+"s) for " +
                String.valueOf(TEXT.length()) + " chars at " + String.valueOf(speed) + " chars per second"
            );
        }

        public void onStart(GL11 gl) {
            float[] params={0, 0, 0, 0.5f};
            gl.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, params, 0);

            // Choose to replace RGB with RGB from the environment color.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_REPLACE);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_RGB, GL11.GL_CONSTANT);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);

            // Choose to replace alpha with a mixture of texture alpha and environment color alpha.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_ALPHA, GL11.GL_CONSTANT);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_ALPHA, GL11.GL_TEXTURE);
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);

            // Using TYPE_ALL here does *not* work to listen to all sensors.
            Sensor sensor=sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            pos+=(int)(CHAR_SIZE*speed*(float)e/1000)*2*2;
            while (pos<0) {
                pos+=tex_coords.capacity();
            }
            while (pos>=tex_coords.capacity()) {
                pos-=tex_coords.capacity();
            }
            sliding_tex_coords.position(pos);

            // Wobble the line coordinates.
            for (int i=0; i<line_coords.capacity(); i+=4) {
                float angle=(float)i/line_coords.capacity()*2*DemoMath.PI;
                angle+=(float)t/200;

                wobbling_line_coords.put(i  , line_coords.get(i  ));
                short y0=(short)(line_coords.get(i+1)+FloatMath.sin(angle)*amp);
                wobbling_line_coords.put(i+1, y0);

                wobbling_line_coords.put(i+2, line_coords.get(i+2));
                short y1=(short)(line_coords.get(i+3)+FloatMath.sin(angle+0.9f)*amp);
                wobbling_line_coords.put(i+3, y1);
            }

            // Set the projection to match the line coordinates.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, 0, WIDTH, HEIGHT, 0);

            charset.makeCurrent();

            gl.glTexCoordPointer(2, GL11.GL_FIXED, 0, sliding_tex_coords.slice());
            gl.glVertexPointer(2, GL11.GL_SHORT, 0, wobbling_line_coords);

            // Draw the text shadow.
            gl.glMatrixMode(GL11.GL_MODELVIEW);
            gl.glPushMatrix();

            // Choose to use texture combiners.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);

            gl.glTranslatef(0, SHADOW_OFFSET, 0);
            gl.glDrawArrays(GL11.GL_LINES, 0, line_coords.capacity()/2);

            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

            // Draw the text.
            gl.glTranslatef(-SHADOW_OFFSET, -SHADOW_OFFSET, 0);
            gl.glDrawArrays(GL11.GL_LINES, 0, line_coords.capacity()/2);

            // Restore OpenGL states.
            gl.glPopMatrix();
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPopMatrix();
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

            // Converge to the desired speed.
            float v=event.values[1];
            if (Math.abs(v)<TOLERANCE) {
                v=0;
            }
            v=DEF_SPEED+v/1.5f;
            speed=(speed+v)/2;

            // Converge to the desired amplitude.
            float h=event.values[2];
            if (Math.abs(h)<TOLERANCE) {
                h=0;
            }
            h=DEF_AMP-h*3.0f;
            h=Math.max(0, Math.min(h, 50));
            amp=(amp+h)/2;
        }
    }

    public Scroller(Demo demo, GL11 gl) {
        super(gl);

        sm=demo.getSensorManager();

        // Load the character set.
        Bitmap bitmap;

        bitmap=BitmapFactory.decodeResource(demo.getActivity().getResources(), R.drawable.charset);
        charset=new Texture2D(gl);
        charset.setData(bitmap);
        bitmap.recycle();

        // Generate the geometry and texture coordinates.
        calcTexCoords(TEXT, bitmap.getWidth(), bitmap.getHeight(), 12, 3);

        line_coords=ShortBuffer.allocate((WIDTH+SHADOW_OFFSET)*2*2);

        int y0=SCROLLER_POS_Y, y1=y0+CHAR_SIZE;
        for (int x=0; x<WIDTH+SHADOW_OFFSET; ++x) {
            // Start vertex x / y.
            line_coords.put((short)x);
            line_coords.put((short)y0);

            // End vertex x / y.
            line_coords.put((short)x);
            line_coords.put((short)y1);
        }
        line_coords.rewind();

        // Schedule the effects in this part.
        scroll=new Scroll();
        add(scroll, Demo.DURATION_MAIN_EFFECTS);
    }
}
