package com.unicellularcomic.CoPushBrush;

import java.util.ArrayList;
import java.util.HashMap;

import org.spiderland.Psh.*;
import org.spiderland.Psh.Coevolution.*;

import com.unicellularcomic.PushBrush.BrushAttributes;

/**
 * The PushBrush problem class for using co-evolved fitness predictors
 * 
 * @author Tom
 * 
 */
public class CoPushBrushPC extends PushGP {
	private static final long serialVersionUID = 1L;

	int attributeStackIndex;

	boolean newGeneration; // Tells RunUntilHumanEvaluation() whether a new
	// generation has just started.
	PushGPIndividual bestIndividualPreviousGeneration;

	// Variables for evaluation
	double totalFitness;
	GAIndividual currentIndividual;
	public int currentIndividualIndex;
	
	// Trainers
	PushGPIndividual currentlyEvaluatingTrainer;
	PushGPIndividual nextTrainer;
	boolean needNewTrainer;
	
	//From CEFloatSymbolicRegression
	protected PushBrushFitnessPredictionGA _predictorGA;

	@Override
	protected void InitFromParameters() throws Exception {
		newGeneration = true;

		super.InitFromParameters();
		
		// Create and initialize predictors
		_predictorGA = (PushBrushFitnessPredictionGA) PredictionGA
				.PredictionGAWithParameters(this,
						GetPredictorParameters(_parameters));
		
		currentlyEvaluatingTrainer = new PushGPIndividual();
		nextTrainer = new PushGPIndividual();
		InitIndividual(currentlyEvaluatingTrainer);
		InitIndividual(nextTrainer);
		needNewTrainer = false;
		
	}

	@Override
	protected void InitInterpreter(Interpreter inInterpreter) throws Exception {

		attributeStackIndex = inInterpreter.addCustomStack(new floatStack());

		/*
		 * Attribute indices: 0 = x; 1 = y; 2 = radius; 3 = red; 4 = green; 5 =
		 * blue; 6 = alpha;
		 */

		inInterpreter
				.AddInstruction("brush.x.increment", new BrushIncrement(0));
		inInterpreter
				.AddInstruction("brush.x.decrement", new BrushDecrement(0));
		inInterpreter.AddInstruction("brush.x.get", new BrushGet(0));
		inInterpreter.AddInstruction("brush.x.set", new BrushSet(0));
		inInterpreter.AddInstruction("brush.x.randincrement",
				new BrushRandomIncrement(0));

		inInterpreter
				.AddInstruction("brush.y.increment", new BrushIncrement(1));
		inInterpreter
				.AddInstruction("brush.y.decrement", new BrushDecrement(1));
		inInterpreter.AddInstruction("brush.y.get", new BrushGet(1));
		inInterpreter.AddInstruction("brush.y.set", new BrushSet(1));
		inInterpreter.AddInstruction("brush.y.randincrement",
				new BrushRandomIncrement(1));

		inInterpreter.AddInstruction("brush.radius.increment",
				new BrushIncrement(2));
		inInterpreter.AddInstruction("brush.radius.decrement",
				new BrushDecrement(2));
		inInterpreter.AddInstruction("brush.radius.get", new BrushGet(2));
		inInterpreter.AddInstruction("brush.radius.set", new BrushSet(2));
		inInterpreter.AddInstruction("brush.radius.randincrement",
				new BrushRandomIncrement(2));

		inInterpreter.AddInstruction("brush.red.increment", new BrushIncrement(
				3));
		inInterpreter.AddInstruction("brush.red.decrement", new BrushDecrement(
				3));
		inInterpreter.AddInstruction("brush.red.get", new BrushGet(3));
		inInterpreter.AddInstruction("brush.red.set", new BrushSet(3));
		inInterpreter.AddInstruction("brush.red.randincrement",
				new BrushRandomIncrement(3));

		inInterpreter.AddInstruction("brush.green.increment",
				new BrushIncrement(4));
		inInterpreter.AddInstruction("brush.green.decrement",
				new BrushDecrement(4));
		inInterpreter.AddInstruction("brush.green.get", new BrushGet(4));
		inInterpreter.AddInstruction("brush.green.set", new BrushSet(4));
		inInterpreter.AddInstruction("brush.green.randincrement",
				new BrushRandomIncrement(4));

		inInterpreter.AddInstruction("brush.blue.increment",
				new BrushIncrement(5));
		inInterpreter.AddInstruction("brush.blue.decrement",
				new BrushDecrement(5));
		inInterpreter.AddInstruction("brush.blue.get", new BrushGet(5));
		inInterpreter.AddInstruction("brush.blue.set", new BrushSet(5));
		inInterpreter.AddInstruction("brush.blue.randincrement",
				new BrushRandomIncrement(5));

		inInterpreter.AddInstruction("brush.alpha.increment",
				new BrushIncrement(6));
		inInterpreter.AddInstruction("brush.alpha.decrement",
				new BrushDecrement(6));
		inInterpreter.AddInstruction("brush.alpha.get", new BrushGet(6));
		inInterpreter.AddInstruction("brush.alpha.set", new BrushSet(6));
		inInterpreter.AddInstruction("brush.alpha.randincrement",
				new BrushRandomIncrement(6));

	}

	/**
	 * * This method replaces GA's normal Run() method, since this problem
	 * requires human evaluations. Since fitness predictors are being used, it
	 * will evaluate a single individual using prediction each time it is
	 * called.
	 * 
	 * Note: This method should be run once at the beginning of evolution to
	 * prime the individual to be tested.
	 * @return True if GA is finished, false otherwise (likely because a human
	 *         evaluation is required, or just because the GA is not yet
	 *         finished).
	 * @throws Exception
	 */
	public boolean RunUntilFitnessPrediction() throws Exception {
		
		if(needNewTrainer){
			nextTrainer = _predictorGA.ChooseNewTrainer();
			needNewTrainer = false;
		}
		
		if (newGeneration) {
			BeginGeneration();
		}

		newGeneration = PredictorEvaluate();
		
		if (!newGeneration) {
			return false;
		}

		Reproduce();
		EndGeneration();

		Print(Report());

		Checkpoint();
		System.gc();

		_currentPopulation = (_currentPopulation == 0 ? 1 : 0);
		_generationCount++;

		if (Terminate()) {
			// Since this value was changed after termination conditions were
			// set, revert back to previous state.
			_currentPopulation = (_currentPopulation == 0 ? 1 : 0);

			Print(FinalReport());
			return true;
		}

		// We need to re-call this method, since a new individual from a new
		// generation needs to be fetched. We can pass a bogus fitness, since
		// it won't be used (since the individual will be the first of the
		// generation).
		return RunUntilFitnessPrediction();
	}

	/**
	 * This will set a trainer's fitness based on the parameter, and will get
	 * the next trainer for human evaluation. The trainer individual that needs
	 * to be evaluated will be stored as currentIndividualIndex.
	 */
	public void EvaluateTrainerAndGetNext(float inFitness) {
		
		currentlyEvaluatingTrainer.SetFitness(inFitness);
		ArrayList<Float> errors = new ArrayList<Float>();
		errors.add(inFitness);
		currentlyEvaluatingTrainer.SetErrors(errors);
		
		_predictorGA.AddTrainer(currentlyEvaluatingTrainer);
		
		currentlyEvaluatingTrainer = nextTrainer;
		needNewTrainer = true;
		
	}

	protected void BeginGeneration() throws Exception {
		totalFitness = 0;
		_bestMeanFitness = Float.MAX_VALUE;

		currentIndividualIndex = 0;

		// Run the next predictor GA generation
		_predictorGA.RunGeneration();

		super.BeginGeneration();
	}

	protected void EndGeneration() {
		_populationMeanFitness = totalFitness
				/ _populations[_currentPopulation].length;
		bestIndividualPreviousGeneration = (PushGPIndividual) _populations[_currentPopulation][_bestIndividual];
		super.EndGeneration();
	}

	/**
	 * Terminate if we have gone past the maximum generations or success.
	 */
	public boolean Terminate() {
		return (_generationCount >= _maxGenerations || Success());
	}

	/**
	 * As for now, we will never return success.
	 */
	protected boolean Success() {
		return false;
	}

	/**
	 * Evaluates the current individual using predictor.
	 * 
	 * @return true if the current generation is finished being evaluated.
	 */
	protected boolean PredictorEvaluate() {
		if (currentIndividualIndex != 0) {
			// If here, the previous individual's fitness needs to be set based
			// on a predicted fitness.
			_averageSize += ((PushGPIndividual) currentIndividual)._program
					.programsize();

			PredictIndividual(currentIndividual);

			// Other system variables that need to be updated.
			totalFitness += currentIndividual.GetFitness();
			if (currentIndividual.GetFitness() < _bestMeanFitness) {
				_bestMeanFitness = currentIndividual.GetFitness();
				_bestIndividual = currentIndividualIndex - 1;
				_bestErrors = currentIndividual.GetErrors();
				_bestSize = ((PushGPIndividual) currentIndividual)._program
						.programsize();
			}
		}

		if (currentIndividualIndex >= _populations[_currentPopulation].length) {
			// If here, we are done with this generation
			return true;
		}

		currentIndividual = _populations[_currentPopulation][currentIndividualIndex];
		currentIndividualIndex++;

		return false;
	}

	/**
	 * Evaluates a solution individual using the best predictor so far.
	 */
	protected void PredictIndividual(GAIndividual inIndividual) {
		PredictionGAIndividual predictor = (PredictionGAIndividual) _predictorGA
				.GetBestPredictor();
		float fitness = predictor
				.PredictSolutionFitness((PushGPIndividual) inIndividual);

		inIndividual.SetFitness(fitness);
		ArrayList<Float> errors = new ArrayList<Float>();
		errors.add(fitness);
		inIndividual.SetErrors(errors);
	}

	/**
	 * Uses the current trainer to get the brush attributes for the next time
	 * step. Note that the returned brush is not constrained, in that some of
	 * its values may be out of the available ranges (e.g. x = -4)
	 * 
	 * @return new brush attributes
	 */
	public BrushAttributes GetNextBrush(BrushAttributes inBrush) {
		PushGPIndividual i = (PushGPIndividual) currentlyEvaluatingTrainer;
		return GetNextBrushFromProgram(inBrush, i._program);
	}

	/**
	 * Uses the parameter Push program to get the brush attributes for the next
	 * time step. Note that the returned brush is not constrained, in that some
	 * of its values may be out of the available ranges (e.g. x = -4)
	 * 
	 * @return new brush attributes
	 */
	public BrushAttributes GetNextBrushFromProgram(BrushAttributes inBrush,
			Program inProgram) {
		BrushAttributes nextBrush = new BrushAttributes();

		// Prepare the interpreter
		_interpreter.ClearStacks();

		floatStack attributeStack = (floatStack) _interpreter
				.getCustomStack(attributeStackIndex);
		floatStack fStack = _interpreter.floatStack();
		ObjectStack inputStack = _interpreter.inputStack();
		booleanStack bStack = _interpreter.boolStack();

		// Push things on stacks
		attributeStack.push(inBrush.x);
		attributeStack.push(inBrush.y);
		attributeStack.push(inBrush.radius);
		attributeStack.push(inBrush.red);
		attributeStack.push(inBrush.green);
		attributeStack.push(inBrush.blue);
		attributeStack.push(inBrush.alpha);

		fStack.push(inBrush.t);
		inputStack.push(inBrush.t);

		if (inBrush.MOUSE_INTERACTION_ENABLED) {
			// Push whether mouse is pressed or not
			bStack.push(inBrush.mouse_pressed);
			inputStack.push(inBrush.mouse_pressed);
			
			// Push mouseY and then mouseX
			fStack.push(inBrush.mouse_y);
			inputStack.push(inBrush.mouse_y);
			fStack.push(inBrush.mouse_x);
			inputStack.push(inBrush.mouse_x);
		}
		
		// Execute the individual
		_interpreter.Execute(inProgram, _executionLimit);

		// Retrieve the attributes
		nextBrush.x = attributeStack.peek(0);
		nextBrush.y = attributeStack.peek(1);
		nextBrush.radius = attributeStack.peek(2);
		nextBrush.red = attributeStack.peek(3);
		nextBrush.green = attributeStack.peek(4);
		nextBrush.blue = attributeStack.peek(5);
		nextBrush.alpha = attributeStack.peek(6);

		return nextBrush;
	}

	public String GetCurrentIndividualCode() {
		return ((PushGPIndividual) currentlyEvaluatingTrainer).toString();
	}

	public PushGPIndividual GetPrevGenBestIndividual() {
		return bestIndividualPreviousGeneration;
	}

	/*
	protected String Report() {
		String report = "\n";
		report += ";;--------------------------------------------------------;;\n";
		report += ";;---------------";
		report += " Report for Generation " + _generationCount + " ";

		if (_generationCount < 10)
			report += "-";
		if (_generationCount < 100)
			report += "-";
		if (_generationCount < 1000)
			report += "-";

		report += "-------------;;\n";
		report += ";;--------------------------------------------------------;;\n";

		if (Double.isInfinite(_populationMeanFitness))
			_populationMeanFitness = Double.MAX_VALUE;

		report += ";; Best Program:\n  "
				+ _populations[_currentPopulation][_bestIndividual] + "\n\n";

		report += ";; Best Program Fitness (higher is better, out of 500): "
				+ (500 - _bestMeanFitness) + "\n";
		report += ";; Best Program Size: " + _bestSize + "\n\n";

		report += ";; Mean Fitness: " + _populationMeanFitness + "\n";
		report += ";; Mean Program Size: " + _averageSize + "\n";

		report += ";; Total Timesteps Thus Far: "
				+ _interpreter.GetEvaluationExecutions() + "\n";
		String mem = String
				.valueOf(Runtime.getRuntime().totalMemory() / 10000000.0f);
		report += ";; Memory usage: " + mem + "\n\n";

		return report;
	}*/
	
	protected String Report() {
		String report = "\n";
		report += ";;--------------------------------------------------------;;\n";
		report += ";;---------------";
		report += " Report for Generation " + _generationCount + " ";

		if (_generationCount < 10)
			report += "-";
		if (_generationCount < 100)
			report += "-";
		if (_generationCount < 1000)
			report += "-";

		report += "-------------;;\n";
		report += ";;--------------------------------------------------------;;\n";

		if (Double.isInfinite(_populationMeanFitness))
			_populationMeanFitness = Double.MAX_VALUE;

		report += "Best program: "
				+ _populations[_currentPopulation][_bestIndividual] + "\n\n";

		report += "Predicted Fitness (higher is better, out of 500): "
			+ _populations[_currentPopulation][_bestIndividual]
			           						.GetFitness() + "\n";

		report += "Size: " + _bestSize + "\n\n";

		report += "--- Population Statistics ---\n";
		report += "Average mean error in population: " + _populationMeanFitness
				+ "\n";
		report += "Average program size in population (points): "
				+ _averageSize + "\n";

		report += "Number of evaluations (may be inaccurate): "
				+ _interpreter.GetEvaluationExecutions() + "\n";
		String mem = String
				.valueOf(Runtime.getRuntime().totalMemory() / 10000000.0f);
		report += "Memory usage: " + mem + "\n";

		report += ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n\n";

		return report;
	}

	@Override
	protected GAIndividual ReproduceByCrossover(int inIndex) {
		// This implementation uses a random number of crossovers between
		// 1 and 10 of the same two individuals.
		
		PushGPIndividual a = (PushGPIndividual) ReproduceByClone(inIndex);
		PushGPIndividual b = (PushGPIndividual) TournamentSelect(
				_tournamentSize, inIndex);

		if (a._program.programsize() <= 0) {
			return b;
		}
		if (b._program.programsize() <= 0) {
			return a;
		}

		int numberCrossovers = _RNG.nextInt(10) + 1;
		for (int i = 0; i < numberCrossovers; i++) {
			int aindex = _RNG.nextInt(a._program.programsize());
			int bindex = _RNG.nextInt(b._program.programsize());

			if (a._program.programsize() + b._program.SubtreeSize(bindex)
					- a._program.SubtreeSize(aindex) <= _maxPointsInProgram){
				a._program.ReplaceSubtree(aindex, b._program.Subtree(bindex));
			}
		}
		
		return a;
	}

	@Override
	protected GAIndividual ReproduceByMutation(int inIndex) {
		PushGPIndividual i = (PushGPIndividual) ReproduceByClone(inIndex);

		int numberMutations = _RNG.nextInt(10) + 1;
		for (int jj = 0; jj < numberMutations; jj++) {
			int totalsize = i._program.programsize();
			int which = totalsize > 1 ? _RNG.nextInt(totalsize) : 0;
			int oldsize = i._program.SubtreeSize(which);
			int newsize = 0;

			if (_useFairMutation) {
				int range = (int) Math.max(1, _fairMutationRange * oldsize);
				newsize = Math
						.max(1, oldsize + _RNG.nextInt(2 * range) - range);
			} else {
				newsize = _RNG.nextInt(_maxRandomCodeSize);
			}

			if (newsize + totalsize - oldsize <= _maxPointsInProgram) {
				Object newtree;

				if (newsize == 1) {
					newtree = _interpreter.RandomAtom();
				} else {
					newtree = _interpreter.RandomCode(newsize);
				}

				i._program.ReplaceSubtree(which, newtree);
			}
		}
		
		return i;
	}
	
	@Override
	public float EvaluateTestCase(GAIndividual inIndividual, Object inInput,
			Object inOutput) {
		// This isn't being used at this time
		return 0;
	}
	
	private HashMap<String, String> GetPredictorParameters(
			HashMap<String, String> parameters) throws Exception {

		HashMap<String, String> predictorParameters = new HashMap<String, String>();

		predictorParameters.put("max-generations", Integer
				.toString(Integer.MAX_VALUE));

		predictorParameters.put("problem-class",
				GetParam("PREDICTOR-problem-class"));
		predictorParameters.put("individual-class",
				GetParam("PREDICTOR-individual-class"));
		predictorParameters.put("population-size",
				GetParam("PREDICTOR-population-size"));
		predictorParameters.put("mutation-percent",
				GetParam("PREDICTOR-mutation-percent"));
		predictorParameters.put("crossover-percent",
				GetParam("PREDICTOR-crossover-percent"));
		predictorParameters.put("tournament-size",
				GetParam("PREDICTOR-tournament-size"));
		predictorParameters.put("trivial-geography-radius",
				GetParam("PREDICTOR-trivial-geography-radius"));
		predictorParameters.put("generations-between-trainers",
				GetParam("PREDICTOR-generations-between-trainers"));
		predictorParameters.put("trainer-population-size",
				GetParam("PREDICTOR-trainer-population-size"));

		return predictorParameters;
	}
	

	class BrushIncrement extends Instruction {
		private static final long serialVersionUID = 1L;

		int _attribute;

		BrushIncrement(int inAttribute) {
			_attribute = inAttribute;
		}

		public void Execute(Interpreter inInterpreter) {
			floatStack attributeStack = (floatStack) inInterpreter
					.getCustomStack(attributeStackIndex);
			attributeStack.set(_attribute, attributeStack.peek(_attribute) + 1);
		}
	}

	class BrushDecrement extends Instruction {
		private static final long serialVersionUID = 1L;

		int _attribute;

		BrushDecrement(int inAttribute) {
			_attribute = inAttribute;
		}

		public void Execute(Interpreter inInterpreter) {
			floatStack attributeStack = (floatStack) inInterpreter
					.getCustomStack(attributeStackIndex);
			attributeStack.set(_attribute, attributeStack.peek(_attribute) - 1);
		}
	}

	class BrushGet extends Instruction {
		private static final long serialVersionUID = 1L;

		int _attribute;

		BrushGet(int inAttribute) {
			_attribute = inAttribute;
		}

		public void Execute(Interpreter inInterpreter) {
			floatStack attributeStack = (floatStack) inInterpreter
					.getCustomStack(attributeStackIndex);
			floatStack fStack = inInterpreter.floatStack();
			float val = attributeStack.peek(_attribute);
			fStack.push(val);
		}
	}

	class BrushSet extends Instruction {
		private static final long serialVersionUID = 1L;

		int _attribute;

		BrushSet(int inAttribute) {
			_attribute = inAttribute;
		}

		public void Execute(Interpreter inInterpreter) {
			floatStack attributeStack = (floatStack) inInterpreter
					.getCustomStack(attributeStackIndex);
			floatStack fStack = inInterpreter.floatStack();

			if (fStack.size() > 0) {
				float val = fStack.pop();
				attributeStack.set(_attribute, val);
			}
		}
	}

	class BrushRandomIncrement extends Instruction {
		private static final long serialVersionUID = 1L;

		int _attribute;

		BrushRandomIncrement(int inAttribute) {
			_attribute = inAttribute;
		}

		public void Execute(Interpreter inInterpreter) {
			floatStack attributeStack = (floatStack) inInterpreter
					.getCustomStack(attributeStackIndex);
			float change = _RNG.nextInt(3) - 1;

			attributeStack.set(_attribute, attributeStack.peek(_attribute)
					+ change);
		}
	}

}
