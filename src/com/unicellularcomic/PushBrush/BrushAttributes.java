package com.unicellularcomic.PushBrush;

/**
 * A class for easily storing and passing brush attributes
 * @author Tom
 *
 */
public class BrushAttributes {
	public float x, y, radius, red, green, blue, alpha;
	public float t;
	
	public boolean MOUSE_INTERACTION_ENABLED;
	public float mouse_x, mouse_y;
	public boolean mouse_pressed;
	
	public BrushAttributes(){
		x = y = radius = red = green = blue = t = -1;
	}

	public BrushAttributes(float newx, float newy, float newradius, float newr,
			float newg, float newb) {

		x = newx;
		y = newy;
		radius = newradius;
		red = newr;
		green = newg;
		blue = newb;

		alpha = 255;
		t = 0;
		MOUSE_INTERACTION_ENABLED = false;
		mouse_x = 0;
		mouse_y = 0;
		mouse_pressed = false;
	}

	public BrushAttributes(float newx, float newy, float newradius, float newr,
			float newg, float newb, float newalpha, float newt) {

		x = newx;
		y = newy;
		radius = newradius;
		red = newr;
		green = newg;
		blue = newb;
		alpha = newalpha;
		t = newt;
		
		MOUSE_INTERACTION_ENABLED = false;
		mouse_x = 0;
		mouse_y = 0;
		mouse_pressed = false;
	}
	
	public BrushAttributes(float newx, float newy, float newradius, float newr,
			float newg, float newb, float newalpha, float newt,
			boolean newmouseinteraction) {

		x = newx;
		y = newy;
		radius = newradius;
		red = newr;
		green = newg;
		blue = newb;
		alpha = newalpha;
		t = newt;
		
		MOUSE_INTERACTION_ENABLED = newmouseinteraction;
	}
	
	public String toString(){
		String str = "Brush:\tx =\t" + x + "\n";
		str += "\ty =\t" + y + "\n";
		str += "\tradius =" + radius + "\n";
		str += "\tred =\t" + red + "\n";
		str += "\tgreen =\t" + green + "\n";
		str += "\tblue =\t" + blue + "\n";
		str += "\talpha =\t" + alpha + "\n";
		str += "\tt =\t" + t + "\n";
		
		return str;
	}
}
