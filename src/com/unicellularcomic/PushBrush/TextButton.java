package com.unicellularcomic.PushBrush;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class TextButton {
	PApplet parent;
	PFont font;

	public String textStr;
	
	public int x, y;
	public int w;
	int h, xPadding, yPadding;
	int borderColor, textColor, backgroundColor, backgroundColorHover;
	int backgroundColorPress;
	
	private boolean dragging, clickedButton ;

	public TextButton(PApplet inParent, PFont inFont, String inText, int inX, int inY,
			int inXPadding, int inYPadding, int inTextHeight,
			int inBorderColor, int inTextColor, int inBackgroundColor,
			int inBackgroundColorHover, int inBackgroundColorPress) {
		
		parent = inParent;
		textStr = inText;
		font = inFont;
		parent.textFont(font);

		x = inX;
		y = inY;
		xPadding = inXPadding;
		yPadding = inYPadding;
		w = ((int)parent.textWidth(textStr)) + (2 * xPadding);
		h = inTextHeight + (2 * yPadding);
		
		borderColor = inBorderColor;
		textColor = inTextColor;
		backgroundColor = inBackgroundColor;
		backgroundColorHover = inBackgroundColorHover;
		backgroundColorPress = inBackgroundColorPress;
		
		dragging = false;
		clickedButton = false;
	}

	public void render() {
		parent.pushStyle();

		// Draw box
		parent.stroke(borderColor);
		if (dragging) {
			parent.fill(backgroundColorPress);
		} else if (mouseHover()) {
			parent.fill(backgroundColorHover);
		} else {
			parent.fill(backgroundColor);
		}
		parent.rect(x, y, w, h);
		
		// Draw text
		parent.fill(textColor);
		parent.textFont(font);
		parent.textAlign(PConstants.CENTER);
		parent.text(textStr, x + (w / 2), y + h - yPadding - 2);

		parent.popStyle();
	}

	public void pressed() {
		if (mouseHover()) {
			dragging = true;
			clickedButton = false;
		}
	}

	public void released() {
		if (mouseHover() && dragging) {
			clickedButton = true;
		}
		dragging = false;
	}

	public boolean mouseHover() {
		return (parent.mouseX > x) && (parent.mouseX < x + w)
				&& (parent.mouseY > y) && (parent.mouseY < y + h);
	}

	public boolean clicked(){
		if(mouseHover() && clickedButton){
			clickedButton = false;
			return true;
		}
		clickedButton = false;
		
		return false;
	}

}
