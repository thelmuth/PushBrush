package com.unicellularcomic.CoPushBrush;

import org.spiderland.Psh.GAIndividual;
import org.spiderland.Psh.Interpreter;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.PushGPIndividual;
import org.spiderland.Psh.Coevolution.PushFitnessPredictionIndividual;

public class PushBrushFitnessPredictionIndividual extends
		PushFitnessPredictionIndividual {
	private static final long serialVersionUID = 1L;

	public PushBrushFitnessPredictionIndividual() {
	}
	
	public PushBrushFitnessPredictionIndividual(Program program,
			Interpreter interpreter) {
		super(program, interpreter);
	}

	/**
	 * Override to guarantee returned value is within the bounds of legal
	 * fitnesses, which for this problem is [0, 500].
	 */
	@Override
	public float PredictSolutionFitness(PushGPIndividual pgpIndividual) {
		float result = super.PredictSolutionFitness(pgpIndividual);
		result = Math.max(result, 0);
		result = Math.min(result, 500);
		
		return result;
	}
	
	@Override
	public GAIndividual clone() {
		return new PushBrushFitnessPredictionIndividual(_program, _interpreter);
	}
	
}
