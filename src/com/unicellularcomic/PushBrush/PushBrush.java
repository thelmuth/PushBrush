package com.unicellularcomic.PushBrush;

import processing.core.*;

import java.util.Random;
import java.awt.TextArea;
import org.spiderland.Psh.*;
import org.spiderland.Psh.PushBrush.BrushAttributes;
import org.spiderland.Psh.PushBrush.PushBrushPC;

//TODO (500 exec.rand)

public class PushBrush extends PApplet {
	private static final long serialVersionUID = 1L;
	
	static boolean MOUSE_INTERACTION_ENABLED = false;
	
	// Setup constants
	static int canvasWidth = 700;
	static int canvasHeight = 500;
	static int headerHeight = 100;
	static int footerHeight = 105;
	static int canvasYStart = headerHeight;
	static int footerYStart = headerHeight + canvasHeight;
	
	static int canvasBackgroundColor = 0;
	static int headerBackgroundColor;
	
	static float minFitness = 0;
	static float maxFitness = 500;
	
	int paintsPerFrame;
	
	// Different screen variables
	boolean instructionsScreen, mainScreen, textScreen, freeDrawScreen;
	
	// TextArea for input and output of individual code
	TextArea codeTextArea;
	boolean codeTextAreaVisible;
	
	// Program to be used by free draw
	Program freeDrawProgram;
	
	// Random number generator
	Random RNG;
	
	// //////// Brush properties /////////
	BrushAttributes brush;
	BrushAttributes freeDrawBrush;
	
	// Location and velocity of the brush
	float initx;
	float inity; // Note: y is the y-coord within the canvas, not on the applet
	
	// Radius of brush
	float initradius;
	
	// Color of brush (red-green-blue)
	float initr;
	float initg;
	float initb;
	float initalpha;
	
	// Time step of current individual
	int inittimeStep;
	
	// Sliders and buttons
	PopBar fitnessBar;
	TextButton helpButton;
	TextButton mainScreenButton;
	TextButton brushCodeButton;
	TextButton enterCodeButton;
	TextButton bestBrushPaintButton;
	TextButton bestBrushCodeButton;
	boolean genZeroError;
	
	TextButton codeViewBrushButton;
	TextButton codeBackToMainButton;
	TextButton freeBrushCodeButton;
	boolean illegalBrushError;
	
	PausePlayButton pausePlayButton;
	PausePlayButton freeDrawPausePlayButton;
	
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
		// General setup
		size(700, 705);
		frameRate(500);
		paintsPerFrame = 100;
		
		// Background colors
		headerBackgroundColor = color(255, 255, 180);
		background(headerBackgroundColor);
	
		// Fonts
		fontTitle = loadFont("Leelawadee-Bold-28.vlw");
		fontText = loadFont("Leelawadee-18.vlw");
		fontTextBold = loadFont("Leelawadee-Bold-18.vlw");
	
		// Initialize RNG
		RNG = new Random();
	
		// Initialize instructions screen
		instructionsScreen = true;
		mainScreen = false;
		textScreen = false;
		freeDrawScreen = false;
		
		genZeroError = false;
		illegalBrushError = false;
	
		// New brush parameters, so that each brush starts the same
		initradius = 15;
		initx = 0;
		inity = 0;
	
		initr = initg = initb = 128;
		initalpha = 255;
		inittimeStep = 0;
	
		if(MOUSE_INTERACTION_ENABLED){
			brush = new BrushAttributes(initx, inity, initradius, initr, initg,
					initb, initalpha, inittimeStep, true);
		}
		else {
			brush = new BrushAttributes(initx, inity, initradius, initr, initg,
					initb, initalpha, inittimeStep);
		}
	
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
		setupMainScreenButton();
		setupbrushCodeButton();
		setupenterCodeButton();
		setupHelpButton();
		setupbestBrushPaintButton();
		setupbestBrushCodeButton();
		
		setupcodeViewBrushButton();
		setupcodeBackToMainButton();
		setupfreeBrushCodeButton();
	
		setuppausePlayButton();
		setupfreeDrawPausePlayButton();
		
		// Setup canvas image
		imgCanvas = createImage(canvasWidth, canvasHeight, RGB);
		
		// Setup textArea
		codeTextArea = new TextArea("Starting text here 2999", 30, 60,
				TextArea.SCROLLBARS_VERTICAL_ONLY);
	
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
			drawInstructionsScreen();
			return;
		}
	
		// Display text area screen if necessary
		if(textScreen){
			drawTextScreen();
			return;
		}
		
		if(freeDrawScreen){
			drawFreeDrawScreen();
			return;
		}
	
		// Display main screen
		if (mainScreen) {
			// Check for main screen button clicks
			if (mainScreenButtonListener()) {
				return;
			}
	
			// Draw main screen
			drawNormalMainScreen();
		}
		
	}
	
	public void mousePressed() {
		if (instructionsScreen) {
			mainScreenButton.pressed();
		}
		if (mainScreen) {
			fitnessBar.pressed();
			helpButton.pressed();
			brushCodeButton.pressed();
			enterCodeButton.pressed();
			bestBrushPaintButton.pressed();
			bestBrushCodeButton.pressed();
			pausePlayButton.pressed();
		}
		if (textScreen){
			codeViewBrushButton.pressed();
			codeBackToMainButton.pressed();
		}
		if (freeDrawScreen){
			codeBackToMainButton.pressed();
			freeBrushCodeButton.pressed();
			freeDrawPausePlayButton.pressed();
		}
	}
	
	public void mouseReleased() {
		if (instructionsScreen) {
			mainScreenButton.released();
		}
		if (mainScreen) {
			fitnessBar.released();
			helpButton.released();
			brushCodeButton.released();
			enterCodeButton.released();
			bestBrushPaintButton.released();
			bestBrushCodeButton.released();
			pausePlayButton.released();
		}
		if (textScreen){
			codeViewBrushButton.released();
			codeBackToMainButton.released();
		}
		if (freeDrawScreen){
			codeBackToMainButton.released();
			freeBrushCodeButton.released();
			freeDrawPausePlayButton.released();
		}
	}
	
	public void mouseDragged() {
	}
	
	
	/**
	 * Updates brush for the next time step.
	 * 
	 * @param brushToUpdate - Brush to update with newBrush attributes
	 * @param newBrush - New brush attributes
	 */
	private void updateBrush(BrushAttributes brushToUpdate, BrushAttributes newBrush) {
		// Define some constants
		float minRadius = 1;
		float maxRadius = 100;
	
		// Update brush attributes
		//brush.radius = constrain(newBrush.radius, minRadius, maxRadius);
		brushToUpdate.radius = ((newBrush.radius - minRadius) % (maxRadius - minRadius)) + minRadius;
		if(brushToUpdate.radius < minRadius){
			brushToUpdate.radius += maxRadius - minRadius;
		}
	
		float minX = -(canvasWidth / 2);
		float maxX = canvasWidth - (canvasWidth / 2);
		//brush.x = constrain(newBrush.x, minX, maxX);
		brushToUpdate.x = ((newBrush.x - minX) % (maxX - minX)) + minX;
		if(brushToUpdate.x < minX){
			brushToUpdate.x += maxX - minX;
		}
	
		float minY =  -(canvasHeight / 2);
		float maxY = canvasHeight - (canvasHeight / 2);
		//brush.y = constrain(newBrush.y, minY, maxY);
		brushToUpdate.y = ((newBrush.y - minY) % (maxY - minY)) + minY;
		if(brushToUpdate.y < minY){
			brushToUpdate.y += maxY - minY;
		}
	
		// Use these if you want wrapping colors
		brushToUpdate.red = newBrush.red % 256;
		if(brushToUpdate.red < 0){
			brushToUpdate.red += 256;
		}
		brushToUpdate.green = newBrush.green % 256;
		if(brushToUpdate.green < 0){
			brushToUpdate.green += 256;
		}
		brushToUpdate.blue = newBrush.blue % 256;
		if(brushToUpdate.blue < 0){
			brushToUpdate.blue += 256;
		}
		brushToUpdate.alpha = newBrush.alpha % 256;
		if(brushToUpdate.alpha < 0){
			brushToUpdate.alpha += 256;
		}
	
		/*
		 * // Use these if you want non-wrapping colors brush.red =
		 * constrain(newBrush.red, 0, 255); brush.green =
		 * constrain(newBrush.green, 0, 255); brush.blue =
		 * constrain(newBrush.blue, 0, 255);
		 */
	
		brushToUpdate.t++;
	}
	
	/**
	 * Paints the brush to the screen.
	 * 
	 * @param paintBrush
	 */
	private void paintBrush(BrushAttributes paintBrush) {
		pushStyle();
		smooth();

		fill(paintBrush.red, paintBrush.green, paintBrush.blue,
				paintBrush.alpha);
		noStroke();
		ellipseMode(CENTER);
		ellipse(paintBrush.x + (canvasWidth / 2), paintBrush.y + canvasYStart
				+ (canvasHeight / 2), paintBrush.radius * 2,
				paintBrush.radius * 2);

		noSmooth();
		popStyle();
	}
	
	
	/**
	 * Tests each main screen button to see if it was clicked.
	 * 
	 * @return true if draw() needs to return
	 * 		   false if draw() can continue
	 */
	private boolean mainScreenButtonListener() {
		if(brushCodeButton.clicked()){
			// Capture canvas image before going to text area screen
			imgCanvas = get(0, canvasYStart, canvasWidth, canvasHeight);
			
			codeTextArea.setText(ga.GetCurrentIndividualCode());
			textScreen = true;
			mainScreen = false;
			return true;
		}
		
		if(enterCodeButton.clicked()){
			// Capture canvas image before going to text area screen
			imgCanvas = get(0, canvasYStart, canvasWidth, canvasHeight);
			
			codeTextArea.setText("");
			textScreen = true;
			mainScreen = false;
			return true;
		}
		
		if(bestBrushPaintButton.clicked()){
			if(ga.GetGenerationCount() <= 0){
				showGenerationZeroError();
				return false;
			}
			
			// Capture canvas image before going to text area screen
			imgCanvas = get(0, canvasYStart, canvasWidth, canvasHeight);
			freeDrawProgram = ga.GetPrevGenBestIndividual()._program;
			
			// Reset some necessary parameters
			background(0);
			if(MOUSE_INTERACTION_ENABLED){
				freeDrawBrush = new BrushAttributes(initx, inity, initradius, initr, initg,
						initb, initalpha, inittimeStep, true);
			}
			else {
				freeDrawBrush = new BrushAttributes(initx, inity, initradius, initr, initg,
						initb, initalpha, inittimeStep);
			}
			
			freeDrawScreen = true;
			mainScreen = false;
			return true;
		}
		
		if(bestBrushCodeButton.clicked()){
			if(ga.GetGenerationCount() <= 0){
				showGenerationZeroError();
				return false;
			}
			
			// Capture canvas image before going to text area screen
			imgCanvas = get(0, canvasYStart, canvasWidth, canvasHeight);
			
			codeTextArea.setText(ga.GetPrevGenBestIndividual().toString());
			textScreen = true;
			mainScreen = false;
			return true;
		}
		
		if (helpButton.clicked()) {
			// Capture canvas image before going to instructions screen
			imgCanvas = get(0, canvasYStart, canvasWidth, canvasHeight);
			
			instructionsScreen = true;
			mainScreen = false;
			return true;
		}
		
		pausePlayButton.checkForClick();
	
		if (fitnessBar.clicked()) {
			drawFitnessBarClicked();
		}
	
		// If here, no buttons clicked that need to return, so return false.
		return false;
	}
	
	/**
	 * The main drawing method for drawing to the main screen.
	 */
	private void drawNormalMainScreen() {
		// Print brush number of times equal to paintsPerFrame
		
		if (!pausePlayButton.isPaused()) {
			for (int i = 0; i < paintsPerFrame; i++) {
				if (MOUSE_INTERACTION_ENABLED) {
					if (mouseX >= 0 && mouseX < width && mouseY >= canvasYStart
							&& mouseY <= canvasYStart + canvasHeight) {
						brush.mouse_x = mouseX - (width / 2);
						brush.mouse_y = mouseY - canvasYStart
								- (canvasHeight / 2);
						brush.mouse_pressed = mousePressed;
					} else {
						brush.mouse_x = 0;
						brush.mouse_y = 0;
						brush.mouse_pressed = false;
					}
				}
				
				// Get the next brush from the current individual
				BrushAttributes newBrush = ga.GetNextBrush(brush);
	
				// Update and paint the next brush
				updateBrush(brush, newBrush);
				paintBrush(brush);
			}
		}
		
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
		
		// Text for best brush
		textAlign(LEFT);
		textFont(fontTextBold);
		text("Best Brush of Previous Generation: ", 15, height - 14);
	
		// Buttons
		helpButton.render();
		brushCodeButton.render();
		enterCodeButton.render();
		bestBrushPaintButton.render();
		bestBrushCodeButton.render();
		
		pausePlayButton.render();
		
		// Generation Zero Error
		if(genZeroError && ga.GetGenerationCount() <= 0){
			showGenerationZeroError();
		}
	}
	
	private void drawInstructionsScreen() {
		if (mainScreenButton.clicked()) {
			instructionsScreen = false;
			mainScreen = true;
	
			image(imgCanvas, 0, canvasYStart);
			return;
		}
		
		background(headerBackgroundColor);
		
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
	}
	
	private void drawFitnessBarClicked() {			
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
		pausePlayButton.play();
		imgCanvas = createImage(canvasWidth, canvasHeight, RGB);
		if(MOUSE_INTERACTION_ENABLED){
			brush = new BrushAttributes(initx, inity, initradius, initr, initg,
					initb, initalpha, inittimeStep, true);
		}
		else {
			brush = new BrushAttributes(initx, inity, initradius, initr, initg,
					initb, initalpha, inittimeStep);
		}
	}
	
	private void drawTextScreen() {
		if(codeBackToMainButton.clicked()){
			textScreen = false;
			mainScreen = true;
			illegalBrushError = false;
	
			codeTextAreaVisible = false;
			this.remove(codeTextArea);
			this.validate();
			
			image(imgCanvas, 0, canvasYStart);
			return;
		}
		if(codeViewBrushButton.clicked()){
			// First, test that input text is a legal Push program
			String inputProgram = codeTextArea.getText();
			boolean programIsLegal = checkForLegalPushProgram(inputProgram);
			if(!programIsLegal){
				illegalBrushError = true;	
			}
			else{
				illegalBrushError = false;
				textScreen = false;
				freeDrawScreen = true;
	
				codeTextAreaVisible = false;
				this.remove(codeTextArea);
				this.validate();
	
				try {
					freeDrawProgram = new Program(inputProgram);
				} catch (Exception e) {
					System.out
							.println("There was an error initializing a user-input program.");
					System.out.println(e);
				}
				
				// Reset some necessary parameters
				background(0);
				if(MOUSE_INTERACTION_ENABLED){
					freeDrawBrush = new BrushAttributes(initx, inity, initradius, initr, initg,
							initb, initalpha, inittimeStep, true);
				}
				else {
					freeDrawBrush= new BrushAttributes(initx, inity, initradius, initr, initg,
							initb, initalpha, inittimeStep);
				}
				
				return;
			}
		}
		
		background(headerBackgroundColor);
		
		if(!codeTextAreaVisible){
			this.add(codeTextArea);
			this.validate();
			codeTextAreaVisible = true;
		}
	
		// Notes
		String notesText = "    You can copy and paste Push code here to save" +
				" and load brush code. This is useful if you display the code" +
				" of a brush you like, and then later want to see it again" +
				" or show someone. Just paste in the code, and click \"Paint" +
				" Using This Code\".";
		textFont(fontTextBold);
		textAlign(LEFT);
		fill(0);
		text(notesText, 20, 500, width - 40, 150);
		
		// Illegal Push Code Error
		if(illegalBrushError){
			textFont(fontTextBold);
			textAlign(CENTER);
			fill(255,0,0);
			text("Entered text is not a legal Push program.", width / 2, 650);
		}
	
		// Buttons
		codeViewBrushButton.render();
		codeBackToMainButton.render();
		
	}
	
	private void drawFreeDrawScreen() {
		if (codeBackToMainButton.clicked()) {
			freeDrawScreen = false;
			mainScreen = true;
			freeDrawPausePlayButton.play();
	
			image(imgCanvas, 0, canvasYStart);
			return;
		}
		if (freeBrushCodeButton.clicked()) {
			freeDrawScreen = false;
			textScreen = true;
			freeDrawPausePlayButton.play();
			
			codeTextArea.setText(freeDrawProgram.toString());
			return;
		}
		
		freeDrawPausePlayButton.checkForClick();
		
		// Print brush number of times equal to paintsPerFrame
		if (!freeDrawPausePlayButton.isPaused()) {
			for (int i = 0; i < paintsPerFrame; i++) {
				if(MOUSE_INTERACTION_ENABLED){
					if (mouseX >= 0 && mouseX < width && mouseY >= canvasYStart
							&& mouseY <= canvasYStart + canvasHeight) {
						freeDrawBrush.mouse_x = mouseX - (width / 2);
						freeDrawBrush.mouse_y = mouseY - canvasYStart
								- (canvasHeight / 2);
						freeDrawBrush.mouse_pressed = mousePressed;
					} else {
						freeDrawBrush.mouse_x = 0;
						freeDrawBrush.mouse_y = 0;
						freeDrawBrush.mouse_pressed = false;
					}
				}
				
				// Get the next brush from the current individual
				BrushAttributes newBrush = ga.GetNextBrushFromProgram(
						freeDrawBrush, freeDrawProgram);
				
				// Update and paint the next brush
				updateBrush(freeDrawBrush, newBrush);
				paintBrush(freeDrawBrush);
			}
		}
		
		// //////// Create the header //////////
		fill(headerBackgroundColor);
		noStroke();
		rect(0, 0, width, headerHeight);
	
		// Title
		fill(0);
		textFont(fontTitle);
		textAlign(CENTER);
		text("PushBrush", width / 2, 30);
	
		// //////// Create the footer //////////
		// Footer background
		fill(headerBackgroundColor);
		noStroke();
		rect(0, footerYStart, width, footerHeight);
		
		// Display information
		textAlign(LEFT);
		textFont(fontTextBold);
		fill(0);
		text("Time Step: " + ((int)freeDrawBrush.t), 450, footerYStart + 23);
		
		// Buttons
		codeBackToMainButton.render();
		freeBrushCodeButton.render();
		freeDrawPausePlayButton.render();
		
	}
	
	/**
	 * Displays an error if certain buttons are pressed during generation 0
	 */
	private void showGenerationZeroError() {
		genZeroError = true;
		
		pushStyle();
		fill(255, 0, 0);
		textFont(fontTextBold);
		text("Wait Until Gen 1", bestBrushCodeButton.x
				+ bestBrushCodeButton.w + 10, height - 14);
	
		popStyle();
	}
	
	
	/**
	 * Checks to make sure the input string represents a legal Push program.
	 * @param inputProgram
	 * @return
	 */
	private boolean checkForLegalPushProgram(String inProgram) {
		int unmatchedLeftParen = 0;
		inProgram = inProgram.trim();
		
		if(inProgram.length() < 2){
			return false;
		}
		
		for(int i = 0; i < inProgram.length(); i++){
			char c = inProgram.charAt(i);
			if(c == '('){
				unmatchedLeftParen++;
			}
			if(c == ')'){
				unmatchedLeftParen--;
			}
			if(i + 1 < inProgram.length() && unmatchedLeftParen <= 0){
				return false;
			}
		}
		
		if (unmatchedLeftParen == 0){
			return true;
		}
		
		return false;
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
		ins += "frequently to use it while creating the next ";
		ins += "generation.\n\n";
	
		ins += "    To rate the painter that is currently painting ";
		ins += "on the canvas, select the rating you wish to give ";
		ins += "it by clicking on the slider. The painter will be given a ";
		ins += "rating corresponding with the slider's position when clicked.";
		ins += " Then, that rating will be given to that ";
		ins += "painter, and the next painter will start painting.\n\n";
	
		return ins;
	}
	
	// Instructions Screen setup
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
	
	// Main Screen setup
	private void setupFitnessBar() {
		int sliderX = 80;
		int sliderY = 40;
		int sliderW = width - (2 * sliderX);
		int sliderH = 40;
		boolean displayTicks = true;
		fitnessBar = new PopBar(this, sliderX, sliderY, sliderW, sliderH,
				displayTicks);
	}
	
	private void setupbrushCodeButton() {
		String buttonText = "Display This Brush's Code";
	
		int x = 10;
		int y = height - 70;
		int textH = 18;
	
		brushCodeButton = new TextButton(this, fontTextBold, buttonText, x,
				y, buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupenterCodeButton() {
		String textHelp = "Enter Code For Brush";//"Show Best Evolved Code";
	
		int x = brushCodeButton.x + brushCodeButton.w + 10;
		int y = height - 70;
		int textH = 18;
	
		enterCodeButton = new TextButton(this, fontTextBold, textHelp, x, y,
				buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupHelpButton() {
		String textHelp = "Help";
	
		int x = enterCodeButton.x + enterCodeButton.w + 10;
		int y = height - 70;
		int textH = 18;
	
		helpButton = new TextButton(this, fontTextBold, textHelp, x, y,
				buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
		
		helpButton.x = width - 10 - helpButton.w;
	}
	
	private void setupbestBrushPaintButton() {
		String buttonText = "Paint";
	
		textFont(fontTextBold);
		
		int x = 20 + (int)textWidth("Best Brush of Previous Generation: ");
		int y = height - 35;
		int textH = 18;
	
		bestBrushPaintButton = new TextButton(this, fontTextBold, buttonText, x,
				y, buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupbestBrushCodeButton() {
		String buttonText = "Display Code";
	
		int x = bestBrushPaintButton.x + bestBrushPaintButton.w + 10;
		int y = height - 35;
		int textH = 18;
	
		bestBrushCodeButton = new TextButton(this, fontTextBold, buttonText, x,
				y, buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	// CodeTextArea Screen setup
	private void setupcodeViewBrushButton() {
		String buttonText = "Paint Using This Code";
	
		int x = 10;
		int y = height - 35;
		int textH = 18;
	
		codeViewBrushButton = new TextButton(this, fontTextBold, buttonText, x,
				y, buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupcodeBackToMainButton() {
		String buttonText = "Back To Rating Brushes";
	
		int x = codeViewBrushButton.x + codeViewBrushButton.w + 10;
		int y = height - 35;
		int textH = 18;
	
		codeBackToMainButton = new TextButton(this, fontTextBold, buttonText, x,
				y, buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
	}
	
	private void setupfreeBrushCodeButton() {
		String buttonText = "Display Code Of This Brush";
	
		int x = codeViewBrushButton.x + codeViewBrushButton.w + 10;
		int y = height - 70;
		int textH = 18;
	
		freeBrushCodeButton = new TextButton(this, fontTextBold, buttonText, x,
				y, buttonxPadding, buttonyPadding, textH, buttonborderColor,
				buttontextColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
		
		freeBrushCodeButton.x = (width / 2) - (freeBrushCodeButton.w / 2);
	}
	
	private void setuppausePlayButton() {
		int x = width - 55;
		int y = 40;
		
		int playColor = buttontextColor;
		int pauseColor = color(120);
	
		pausePlayButton = new PausePlayButton(this, x, y, buttonborderColor,
				pauseColor, playColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
		
		pausePlayButton.play();
	
	}
	
	private void setupfreeDrawPausePlayButton() {
		int x = width - 55;
		int y = 40;
		
		int playColor = buttontextColor;
		int pauseColor = color(120);
	
		freeDrawPausePlayButton = new PausePlayButton(this, x, y, buttonborderColor,
				pauseColor, playColor, buttonbackgroundColor,
				buttonbackgroundColorHover, buttonbackgroundColorPress);
		
		freeDrawPausePlayButton.play();
	
	}

}
