package com.unicellularcomic.PushBrush;

import processing.core.PApplet;
import processing.core.PConstants;

public class HorizontalSlider {
	PApplet parent;

	int x, y, w, h, barW;
	int lineColor, barColor, barColorHover, barColorPressed;

	int sliderLength;
	int barPos;

	boolean dragging;
	int draggingPointOnBar;

	public HorizontalSlider(PApplet inParent, int inX, int inY, int inW, int inH,
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

	public void render() {
		parent.pushStyle();

		// Draw slider lines
		parent.stroke(lineColor);
		parent.fill(200);
		parent.rectMode(PConstants.CORNER);
		parent.rect(x, y, w, h);
		
		//parent.line(x, y, x, y + h);
		//parent.line(x + w, y, x + w, y + h);
		//parent.line(x, y + (h / 2), x + w, y + (h / 2));

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
		
		// Draw lines on bar to indicate slider
		parent.fill(0, 50);
		parent.noStroke();
		
		float barCenter = barPos + (barW / 2);
		parent.rectMode(PConstants.CENTER);
		parent.rect(x + barCenter, y + (h / 2), 3, (0.6f * h));
		parent.rect(x + barCenter - 5, y + (h / 2), 3, (0.6f * h));
		parent.rect(x + barCenter + 5, y + (h / 2), 3, (0.6f * h));
		
		parent.popStyle();
	}

	/**
	 * Returns a value in [0,1] representing the normalized position of the bar
	 * on the slide.
	 */
	public float getValue() {
		return (float) barPos / (float) sliderLength;
	}
	
	/**
	 * Resets the slider's bar to half.
	 */
	public void resetSlider(){
		barPos = sliderLength / 2;
	}

	public void pressed() {
		if (mouseOverBar()) {
			dragging = true;
			draggingPointOnBar = parent.mouseX - x - barPos;
		} else if (mouseOverSlideBar()) {
			barPos = PApplet.constrain(parent.mouseX - x - (barW / 2), 0, w
					- barW);
			dragging = true;
			draggingPointOnBar = parent.mouseX - x - barPos;
		}
	}

	public void released() {
		dragging = false;
	}

	public void dragged() {
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
	
	boolean mouseOverSlideBar() {
		return (parent.mouseX > x) && (parent.mouseX < x + w)
				&& (parent.mouseY > y) && (parent.mouseY < y + h);
	}

}
