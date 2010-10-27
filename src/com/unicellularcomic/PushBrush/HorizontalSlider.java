package com.unicellularcomic.PushBrush;

import processing.core.PApplet;

public class HorizontalSlider {
	PApplet parent;

	int x, y, w, h, barW;
	int lineColor, barColor, barColorHover, barColorPressed;

	int sliderLength;
	int barPos;

	boolean dragging;
	int draggingPointOnBar;

	HorizontalSlider(PApplet inParent, int inX, int inY, int inW, int inH,
			int inBarW, int inLineColor, int inBarColor, int inBarColorHover,
			int inBarColorPressed) {
		parent = inParent;

		x = inX;
		y = inY;
		w = inW;
		h = inH;
		barW = inBarW;
		lineColor = inLineColor;
		barColor = inBarColor;
		barColorHover = inBarColorHover;
		barColorPressed = inBarColorPressed;

		sliderLength = w - barW;
		barPos = sliderLength / 2;

		dragging = false;
	}

	/**
	 * Returns a value in [0,1] representing the normalized position of the bar
	 * on the slide.
	 */
	float getValue() {
		return (float) barPos / (float) sliderLength;
	}
	
	/**
	 * Resets the slider's bar to half.
	 */
	void resetSlider(){
		barPos = sliderLength / 2;
	}

	void render() {
		parent.pushStyle();

		// Draw slider lines
		parent.stroke(lineColor);
		parent.line(x, y, x, y + h);
		parent.line(x + w, y, x + w, y + h);
		parent.line(x, y + (h / 2), x + w, y + (h / 2));

		// Draw bar
		parent.stroke(lineColor);
		if (dragging) {
			parent.fill(barColorPressed);
		} else if (mouseOverBar()) {
			parent.fill(barColorHover);
		} else {
			parent.fill(barColor);
		}
		parent.rect(x + barPos, y, barW, h);

		parent.popStyle();
	}

	void pressed() {
		if (mouseOverBar()) {
			dragging = true;
			draggingPointOnBar = parent.mouseX - x - barPos;
		}
	}

	void released() {
		dragging = false;
	}

	void dragged() {
		if (dragging) {
			barPos = PApplet.constrain(parent.mouseX - x - draggingPointOnBar,
					0, w - barW);
		}
	}

	boolean mouseOverBar() {
		int barLeft = x + barPos;
		int barRight = x + barPos + barW;
		return (parent.mouseX > barLeft) && (parent.mouseX < barRight)
				&& (parent.mouseY > y) && (parent.mouseY < y + h);
	}

}
