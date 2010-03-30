package com.rabenauge.parandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private static final String CHARS="abcdefghijklmnopqrstuvwxyz.:-,`´!»«+";
    private static final String TEXT=
        "Another firstie for the pleasure of Breakpoint Nation: Para´N`droiD. The initial fully " +
        "interactive mobile Android platform cell phone demo. Presented to you by Tristar and " +
        "Red Sector Incorporated: Twice the Fun - Double the Trouble. And Rabenauge: Beauty lies in " +
        "the Eye of the Raven. " +
        "     " +
        "Twenty Five Years RSI. Twenty Years TRSI. Online until bust. We are the sleeping Gods. " +
        "We salute all, who built the heritage. Values never die. Spotter says: Boogie down " +
        "Brookline. This demo is for my Princessive Princess Press, the Beautiful. Three ´P` high and " +
        "rising. Fly, Boston College Eagle, defy death. Regards to Irata, the Founder and Legend. " +
        "     " +
        "Handshakes to Titus: Thx man. And Happy Birtday to TRSI from Rabenauge! " +
        "     " +
        "Golden Greetings to: " +
        "ACS - ALCATRAZ - ANDROMEDA SOFTWARE DEVELOPMENT - ATE BIT - BAUKNECHT - BITFELLAS - " +
        "BIRDHOUSE PROJECTS - BRAINSTORM - CONSPIRACY - COOCOON - DANISH GOLD - DRIFTERS - " +
        "EQUINOX - FAIRLIGHT - FTC - GENESIS - GNUMPF POSSE - HAUJOBB - HITMEN - HOODLUM - HOTSTUFFERS - " +
        "KALISTO - K TWO - KEFRENS - NEOSCIENTISTS - PARADISE - PARADOX - POLKA BROTHERS - RGBA - " +
        "REBELS - RAZOR NINETEEN ELEVEN - REMEDY - ROYAL BELGIAN BEER SQUADRON - SCARAB - SCOOPEX - " +
        "SECTION EIGHT - SPACEBALLS - SPECKDRUMM - SPEEDQUEEN - TEK - STILL - THE BLACK LOTUS - " +
        "THE LIGHTFORCE - THE SILENTS - TITAN - THE PLANET OF LEATHER MOOMINS - UK ALLSTARS";

    private Texture2D charset;
    private IntBuffer tex_coords;
    private ShortBuffer line_coords;

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

    private class Scroll extends Effect {
        private IntBuffer sliding_tex_coords;
        private ShortBuffer wobbling_line_coords;

        public Scroll() {
            sliding_tex_coords=tex_coords.duplicate();
            sliding_tex_coords.put(tex_coords);

            wobbling_line_coords=ShortBuffer.allocate(line_coords.capacity());
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
        }

        public void onRender(GL11 gl, long t, long e, float s) {
            // Scroll at a speed of 2 character per second.
            int speed=2;
            int pos=(int)t*speed*CHAR_SIZE/1000*4;
            if (pos>=tex_coords.capacity()) {
                pos-=tex_coords.capacity();
            }
            sliding_tex_coords.position(pos);

            // Wobble the line coordinates.
            float amp=30.0f;
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
            float shadow=12;
            gl.glTranslatef(shadow, shadow, 0);

            // Choose to use texture combiners.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);

            gl.glDrawArrays(GL11.GL_LINES, 0, line_coords.capacity()/2);

            // Restore OpenGL states.
            gl.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

            gl.glPopMatrix();

            // Draw the text.
            gl.glDrawArrays(GL11.GL_LINES, 0, line_coords.capacity()/2);

            // Restore OpenGL states.
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glPopMatrix();
        }
    }

    public Scroller(Activity activity, GL11 gl) {
        super(gl);

        // Load the character set.
        Bitmap bitmap;

        bitmap=BitmapFactory.decodeResource(activity.getResources(), R.drawable.charset);
        charset=new Texture2D(gl);
        charset.setData(bitmap);
        bitmap.recycle();

        // Generate the geometry and texture coordinates.
        calcTexCoords(TEXT, bitmap.getWidth(), bitmap.getHeight(), 12, 3);

        line_coords=ShortBuffer.allocate(WIDTH*2*2);

        int y0=SCROLLER_POS_Y, y1=y0+CHAR_SIZE;
        for (int x=0; x<WIDTH; ++x) {
            // Start vertex x / y.
            line_coords.put((short)x);
            line_coords.put((short)y0);

            // End vertex x / y.
            line_coords.put((short)x);
            line_coords.put((short)y1);
        }
        line_coords.rewind();

        // Schedule the effects in this part.
        add(new Scroll(), Demo.DURATION_PART_STATIC);
    }
}
