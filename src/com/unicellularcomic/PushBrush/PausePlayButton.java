package com.unicellularcomic.PushBrush;

import processing.core.PApplet;
import processing.core.PConstants;

public class PausePlayButton {
	PApplet parent;
	
	int x, y, W;
	int borderColor, pauseColor, playColor, backgroundColor, backgroundColorHover;
	int backgroundColorPress;
	
	private boolean dragging, clickedButton;
	private boolean paused;

	PausePlayButton(PApplet inParent, int inX, int inY, int inBorderColor,
			int inPauseColor, int inPlayColor, int inBackgroundColor,
			int inBackgroundColorHover, int inBackgroundColorPress) {

		parent = inParent;

		x = inX;
		y = inY;
		W = 40;
		
		borderColor = inBorderColor;
		pauseColor = inPauseColor;
		playColor = inPlayColor;
		backgroundColor = inBackgroundColor;
		backgroundColorHover = inBackgroundColorHover;
		backgroundColorPress = inBackgroundColorPress;
		
		dragging = false;
		clickedButton = false;
		
		paused = false;
	}

	void render() {
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
		parent.rect(x, y, W, W);
		
		// Draw text
		if(paused){
			int tl = (int) (W * 0.6);
			int th = (int) (tl * 0.866);

			parent.fill(playColor);
			parent.noStroke();
			parent.triangle(x + ((W - th) / 2), y + ((W - tl) / 2),
					x + ((W - th) / 2), y + ((W + tl) / 2),
					x + ((W + th) / 2), y + (W / 2));
		}
		else{
			parent.fill(pauseColor);
			parent.noStroke();
			parent.rectMode(PConstants.CENTER);
			parent.rect(x + (W * (1f/3f)), y + W / 2, W / 5, W * (7f/10f));
			parent.rect(x + (W * (2f/3f)), y + W / 2, W / 5, W * (7f/10f));
			parent.rectMode(PConstants.CORNER);
		}

		parent.popStyle();
	}

	void pressed() {
		if (mouseHover()) {
			dragging = true;
			clickedButton = false;
		}
	}

	void released() {
		if (mouseHover() && dragging) {
			clickedButton = true;
		}
		dragging = false;
	}

	public void checkForClick() {
		if(clicked()){
			if(paused){
				play();
			}
			else{
				pause();
			}
		}
	}

	public boolean mouseHover() {
		return (parent.mouseX > x) && (parent.mouseX < x + W)
				&& (parent.mouseY > y) && (parent.mouseY < y + W);
	}

	public boolean clicked(){
		if(mouseHover() && clickedButton){
			clickedButton = false;
			return true;
		}
		clickedButton = false;
		
		return false;
	}
	
	public void pause(){
		paused = true;
	}
	
	public void play(){
		paused = false;
	}
	
	public boolean isPaused(){
		return paused;
	}

}
