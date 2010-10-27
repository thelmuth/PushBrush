package com.unicellularcomic.PushBrush;

import processing.core.PApplet;

public class PopBar {
	PApplet parent;

	int x, y, w, h;
	int rectColor, buttonBorderColor, buttonColorPressed;
	boolean displayTicks;
	
	int centerY;
	int buttonLoc;
	int buttonColor;
	float buttonGreen;
	float buttonRed;
	
	boolean dragging;
	boolean clickedButton;

	PopBar(PApplet inParent, int inX, int inY, int inW, int inH,
			boolean inDisplayTicks) {
		parent = inParent;

		x = inX;
		y = inY;
		w = inW;
		h = inH;
		displayTicks = inDisplayTicks;
		
		rectColor = parent.color(50, 50, 0);
		buttonBorderColor = parent.color(0);
		buttonColorPressed = parent.color(0,180,0);
		
		centerY = y + (h / 2);
		buttonLoc = w / 2 + x;
		buttonColor = parent.color(128, 128, 0);
		buttonGreen = 128;
		buttonRed = 128;
		
		dragging = false;
		clickedButton = false;

	}

	void render() {
		parent.pushStyle();
		
		if(displayTicks){
			parent.stroke(rectColor);
			parent.line(x, y + 3, x, y + h - 3);
			parent.line(x + w, y + 3, x + w, y + h - 3);
			parent.line(x + w/2, y + 7, x + w/2, y + h - 7);
			parent.line(x + w/4, y + 12, x + w/4, y + h - 12);
			parent.line(x + (3 * w / 4), y + 12, x + (3 * w / 4), y + h - 12);
		}

		// Draw bar
		parent.noStroke();
		parent.fill(rectColor);
		parent.rect(x, centerY - 3, w, 6);

		// If hovering, change mouse location and change color
		if (mouseOverPopBar()) {
			buttonLoc = PApplet.constrain(parent.mouseX, x, x + w);

			buttonGreen = 255 * getValue();
			buttonRed = 255 - (255 * getValue());
			buttonColor = parent.color(buttonRed, buttonGreen, 0);
		} else {
			// Use more muted color
			buttonColor = parent.color(PApplet
					.constrain(buttonRed - 30, 0, 255), PApplet.constrain(
					buttonGreen - 30, 0, 255), 0);
		}

		parent.noStroke();
		parent.fill(buttonBorderColor);
		parent.ellipse(buttonLoc, centerY, h, h);

		parent.fill(buttonColor);
		parent.ellipse(buttonLoc, centerY, h - 10, h - 10);

		parent.popStyle();
	}
	

	/**
	 * Returns a value in [0,1] representing the normalized position of the bar
	 * on the slide.
	 */
	float getValue() {
		return ((float) buttonLoc - x) / (float) w;
	}

	void pressed() {
		if (mouseOverPopBar()) {
			dragging = true;
			clickedButton = false;
		}
	}

	void released() {
		if (mouseOverPopBar() && dragging) {
			clickedButton = true;
		}
		dragging = false;
	}
	
	public boolean clicked(){
		if(mouseOverPopBar() && clickedButton){
			clickedButton = false;
			return true;
		}
		clickedButton = false;
		
		return false;
	}
	
	boolean mouseOverPopBar() {
		return (parent.mouseX > x - (h/2)) && (parent.mouseX < x + w + (h/2))
				&& (parent.mouseY > y) && (parent.mouseY < y + h);
	}

}
