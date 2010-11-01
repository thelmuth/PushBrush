package com.unicellularcomic.PushBrush;

import processing.core.*;

import java.util.Random;
import org.spiderland.Psh.*;
import org.spiderland.Psh.PushBrush.BrushAttributes;
import org.spiderland.Psh.PushBrush.PushBrushPC;

public class PushBrush extends PApplet {
	private static final long serialVersionUID = 1L;
	
	// Setup constants
	static int canvasWidth = 700;
	static int canvasHeight = 500;
	static int headerHeight = 100;
	static int footerHeight = 70;
	static int canvasYStart = headerHeight;
	static int footerYStart = headerHeight + canvasHeight;
	
	static int canvasBackgroundColor = 0;
	static int headerBackgroundColor;
	
	static float minFitness = 0;
	static float maxFitness = 500;
	
	// Instructions screen
	static int instructionsBackgroundColor;
	boolean instructionsScreen, mainScreen;
	
	// Random number generator
	Random RNG;
	
	// //////// Brush properties /////////
	BrushAttributes brush;
	
	// Location and velocity of the brush
	float initx;
	float inity; // Note: y is the y-coord within the canvas, not on the applet
	
	// Radius of brush
	float initradius;
	
	// Color of brush (red-green-blue)
	float initr;
	float initg;
	float initb;
	
	// Time step of current individual
	int inittimeStep;
	
	// Sliders and buttons
	PopBar fitnessBar;
	// HorizontalSlider fitnessSlider; //No longer used, but keep just in case
	
	TextButton helpButton;
	TextButton mainScreenButton;
	TextButton showBestBrushButton;
	TextButton showBestCodeButton;
	
	int buttonxPadding;
	int buttonyPadding;
	int buttonborderColor;
	int buttontextColor;
	int buttonbackgroundColor;
	int buttonbackgroundColorHover;
	int buttonbackgroundColorPress;
	
	// The GA
	PushBrushPC ga;
	
	// Fonts
	PFont fontTitle;
	PFont fontText;
	PFont fontTextBold;
	
	// PImage to hold the canvas when not on the screen
	PImage imgCanvas;
	
	public void setup() {
		frameRate(200);
		
		// Background colors
		instructionsBackgroundColor = color(255, 255, 180);
		headerBackgroundColor = color(255, 255, 180);
	
		// General setup
		size(canvasWidth, headerHeight + canvasHeight + footerHeight);
		background(instructionsBackgroundColor);
	
		// Fonts
		fontTitle = loadFont("Leelawadee-Bold-28.vlw");
		fontText = loadFont("Leelawadee-18.vlw");
		fontTextBold = loadFont("Leelawadee-Bold-18.vlw");
	
		// Initialize RNG
		RNG = new Random();
	
		// Initialize instructions screen
		instructionsScreen = true;
		mainScreen = false;
	
		// New brush parameters, so that each brush starts the same
		initradius = 15;
		initx = 0;
		inity = 0;
	
		initr = initg = initb = 128;
		inittimeStep = 0;
	
		brush = new BrushAttributes(initx, inity, initradius, initr, initg,
				initb, inittimeStep);
	
		/*
		 * // For now, use non-random initial values. x =
		 * RNG.nextInt(canvasWidth - (2 * radius)) + radius; y =
		 * RNG.nextInt(canvasHeight - (2 * radius)) + radius;
		 * 
		 * r = RNG.nextInt(255); g = RNG.nextInt(255); b = RNG.nextInt(255);
		 */
	
		// Button parameters
		buttonxPadding = 10;
		buttonyPadding = 5;
		buttonborderColor = 0;
		buttontextColor = color(6, 3, 40);
		buttonbackgroundColor = color(187, 234, 246);
		buttonbackgroundColorHover = color(144, 207, 223);
		buttonbackgroundColorPress = color(93, 163, 181);
	
		// Setup sliders and buttons
		// setupSlider();
		setupFitnessBar();
		setupHelpButton();
		setupMainScreenButton();
		setupshowBestBrushButton();
		setupshowBestCodeButton();
	
		// Setup canvas image
		imgCanvas = createImage(canvasWidth, canvasHeight, RGB);
	
		// Setup PushBrushPC
		try {
			String[] parameterStrings = loadStrings("pushbrush.pushgp");
	
			String parameters = "";
			for (int i = 0; i < parameterStrings.length; i++) {
				parameters += parameterStrings[i] + '\n';
			}
	
			GA gaInitial = GA.GAWithParameters(Params.Read(parameters));
	
			if (!(gaInitial instanceof PushBrushPC)) {
				throw new Exception(
						"ERROR: The problem-class must inherit from PushBrushPC.");
			}
	
			ga = (PushBrushPC) gaInitial;
	
			// Use of bogus fitness encouraged here, just to prime painting
			// individual
			ga.RunUntilHumanEvaluation(-16.1616f);
	
		} catch (Exception e) {
			println("There was a problem with GA initialization:");
			println(e);
		}
	
	}
	
	public void draw() {
	
		// Display instructions screen if necessary
		if (instructionsScreen) {
			background(instructionsBackgroundColor);
	
			// Title
			fill(0);
			textAlign(CENTER);
			textFont(fontTitle);
			text("PushBrush Instructions", width / 2, 40);
	
			// Instructions
			textAlign(LEFT);
			textFont(fontTextBold);
			String insString = getInstructions();
			text(insString, 20, 60, width - 40, height - 100);
	
			// Main Screen button
			mainScreenButton.render();
	
			if (mainScreenButton.clicked()) {
				instructionsScreen = false;
				mainScreen = true;
	
				image(imgCanvas, 0, canvasYStart);
			}
			return;
		}
	
		if (mainScreen && helpButton.clicked()) {
			// Capture canvas image before going to instructions screen
			imgCanvas = get(0, canvasYStart, canvasWidth, canvasHeight);
	
			instructionsScreen = true;
			mainScreen = false;
			return;
		}
	
		if (mainScreen && fitnessBar.clicked()) {
			// NOTE: Fitness ranges from minFitness to maxFitness, where lower
			// is better. This is because PshGP uses errors instead of fitness,
			// where lower error is better.
	
			float fitness = maxFitness
					- ((fitnessBar.getValue() * (maxFitness - minFitness)) + minFitness);
	
			try {
				ga.RunUntilHumanEvaluation(fitness);
			} catch (Exception e) {
				println("There was a problem:");
				println(e);
			}
	
			// Reset some necessary parameters
			background(0);
			imgCanvas = createImage(canvasWidth, canvasHeight, RGB);
	
			// Reset brush
			brush = new BrushAttributes(initx, inity, initradius, initr, initg,
					initb, inittimeStep);
		}
		
		// Get the next brush from the current individual
		BrushAttributes newBrush = ga.getNextBrush(brush);
	
		// Update and paint the next brush
		updateBrush(newBrush);
		paintBrush();
	
		// //////// Create the header //////////
		fill(headerBackgroundColor);
		noStroke();
		rect(0, 0, width, headerHeight);
	
		// Footer background
		fill(headerBackgroundColor);
		noStroke();
		rect(0, footerYStart, width, footerHeight);
	
		// Title
		fill(0);
		textFont(fontTitle);
		textAlign(CENTER);
		text("PushBrush", width / 2, 30);
	
		// Fitness slider
		fitnessBar.render();
		textAlign(LEFT);
		textFont(fontTextBold);
		text("Rate:", 10, 65);
	
		// Slider "best" and "worst"
		textAlign(CENTER);
		textFont(fontText);
		text("worst", 80, headerHeight - 5);
		text("best", width - 80, headerHeight - 5);
	
		/*
		 * // Used to display current rating value
		 * float normalizedSliderPos =
		 * fitnessBar.getValue(); int displaySliderPos = (int)
		 * ((normalizedSliderPos * (maxFitness - minFitness)) + minFitness);
		 * textAlign(RIGHT); textFont(fontTextBold); text(displaySliderPos, 580,
		 * 88);
		 */
	
		// //////// Create the footer //////////
		// Display information
		textAlign(LEFT);
		textFont(fontTextBold);
		text("Generation: " + ga.GetGenerationCount(), 15, footerYStart + 23);
	
		textAlign(LEFT);
		text("Individual: " + ga.currentIndividualIndex + "/"
				+ ga.GetPopulationSize(), 220, footerYStart + 23);
	
		textAlign(LEFT);
		text("Time Step: " + ((int)brush.t), 450, footerYStart + 23);
	
		// Buttons
		helpButton.render();
		showBestBrushButton.render();
		showBestCodeButton.render();
	
	
	}
	
	public void mousePressed() {
		if (mainScreen) {
			// fitnessSlider.pressed();
			fitnessBar.pressed();
			helpButton.pressed();
			showBestBrushButton.pressed();
			showBestCodeButton.pressed();
		}
		if (instructionsScreen) {
			mainScreenButton.pressed();
		}
	}
	
	public void mouseReleased() {
		if (mainScreen) {
			// fitnessSlider.released();
			fitnessBar.released();
			helpButton.released();
			showBestBrushButton.released();
			showBestCodeButton.released();
		}
		if (instructionsScreen) {
			mainScreenButton.released();
		}
	}
	
	public void mouseDragged() {
		/*
		 * if (mainScreen) { fitnessSlider.dragged(); }
		 */
	}
	
	/**
	 * Updates brush for the next time step.
	 * 
	 * @param newBrush
	 */
	private void updateBrush(BrushAttributes newBrush) {
		
		// Use this if you want random brush wiggles.
		/*newBrush.x += RNG.nextInt(3) - 1;
		newBrush.y += RNG.nextInt(3) - 1;*/
	
		// Define some constants
		float minRadius = 1;
		float maxRadius = 100;
	
		// Update brush attributes
		//brush.radius = constrain(newBrush.radius, minRadius, maxRadius);
		while(newBrush.radius < minRadius){
			newBrush.radius += maxRadius - minRadius;
		}
		brush.radius = ((newBrush.radius - minRadius) % (maxRadius - minRadius)) + minRadius;
		
		float minX = -(canvasWidth / 2);
		float maxX = canvasWidth - (canvasWidth / 2);
		//brush.x = constrain(newBrush.x, minX, maxX);
		while(newBrush.x < minX){
			newBrush.x += maxX - minX;
		}
		brush.x = ((newBrush.x - minX) % (maxX - minX)) + minX;
	
		float minY =  -(canvasHeight / 2);
		float maxY = canvasHeight - (canvasHeight / 2);
		//brush.y = constrain(newBrush.y, minY, maxY);
		while(newBrush.y < minY){
			newBrush.y += maxY - minY;
		}
		brush.y = ((newBrush.y - minY) % (maxY - minY)) + minY;
		
		// Use these if you want wrapping colors
		while (newBrush.red < 0) {
			newBrush.red += 256;
		}
		brush.red = newBrush.red % 256;
		while (newBrush.green < 0) {
			newBrush.green += 256;
		}
		brush.green = newBrush.green % 256;
		while (newBrush.blue < 0) {
			newBrush.blue += 256;
		}
		brush.blue = newBrush.blue % 256;
	
		/*
		 * // Use these if you want non-wrapping colors brush.red =
		 * constrain(newBrush.red, 0, 255); brush.green =
		 * constrain(newBrush.green, 0, 255); brush.blue =
		 * constrain(newBrush.blue, 0, 255);
		 */
	
		brush.t++;
	}
	
	/**
	 * Paints the brush to the screen.
	 */
	private void paintBrush() {
		pushStyle();
	
		fill(brush.red, brush.green, brush.blue);
		noStroke();
		ellipseMode(CENTER);
		ellipse(brush.x + (canvasWidth / 2), brush.y + canvasYStart
				+ (canvasHeight / 2), brush.radius * 2, brush.radius * 2);
	
		popStyle();
	}
	
	private String getInstructions() {
		String ins = "   Welcome to PushBrush, a program for ";
		ins += "evolving painter individuals!\n\n";
	
		ins += "    As you will see, the main screen of PushBrush ";
		ins += "is dominated by the canvas, where evolved painters ";
		ins += "will paint. At the beginning, the painters are ";
		ins += "entirely random (and likely boring), and will need ";
		ins += "your assistance to become interesting!\n\n";
	
		ins += "    In order for painters to evolve improvements, ";
		ins += "they need to be evaluated to tell how interesting ";
		ins += "they are. This is where you come in. You will rate ";
		ins += "each individual based on how much you like its ";
		ins += "characteristics. This gives a rating, or fitness, ";
		ins += "to the individual, so that evolution knows how ";
		ins += "frequently to use it while creating the next";
		ins += "generation.\n\n";
	
		ins += "    To rate the painter that is currently painting ";
		ins += "on the canvas, select the rating you wish to give ";
		ins += "it by clicking on the slider. The painter will be given a ";
		ins += "rating corresponding with the slider's position when clicked.";
		ins += " Then, that rating will be given to that ";
		ins += "painter, and the next painter will start painting.\n\n";
	
		return ins;
	}
	
	/*
	 * private void setupSlider() { int sliderX = 20; int sliderY = 70; int
	 * sliderW = 520; int sliderH = 20; int sliderBarW = 30; int sliderLineColor
	 * = 0; int sliderBarColor = color(255, 255, 0); int sliderBarColorHover =
	 * color(230, 230, 0); int sliderBarColorPressed = color(180, 180, 0);
	 * fitnessSlider = new HorizontalSlider(this, sliderX, sliderY, sliderW,
	 * sliderH, sliderBarW, sliderLineColor, sliderBarColor,
	 * sliderBarColorHover, sliderBarColorPressed); }
	 */
	
	private void setupFitnessBar() {
		int sliderX = 80;
		int sliderY = 40;
		int sliderW = width - (2 * sliderX);
		int sliderH = 40;
		boolean displayTicks = true;
		fitnessBar = new PopBar(this, sliderX, sliderY, sliderW, sliderH,
				displayTicks);
	}
	
	private void setupHelpButton() {
		String textHelp = "Help";
	
		int x = 10;
		int y = height - 35;
		int textH = 18;
	
		helpButton = new TextButton(this, fontTextBold, textHelp, x, y,
				buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupshowBestBrushButton() {
		String textHelp = "Show Best Evolved Brush";
	
		int x = helpButton.x + helpButton.w + 10;
		int y = height - 35;
		int textH = 18;
	
		showBestBrushButton = new TextButton(this, fontTextBold, textHelp, x,
				y, buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupshowBestCodeButton() {
		String textHelp = "Show Best Evolved Code";
	
		int x = showBestBrushButton.x + showBestBrushButton.w + 10;
		int y = height - 35;
		int textH = 18;
	
		showBestCodeButton = new TextButton(this, fontTextBold, textHelp, x, y,
				buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupMainScreenButton() {
		String textMain = "Go Evolve Painters!";
	
		int x = width - 200;
		int y = height - 40;
		int textH = 18;
	
		mainScreenButton = new TextButton(this, fontTextBold, textMain, x, y,
				buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}

}
