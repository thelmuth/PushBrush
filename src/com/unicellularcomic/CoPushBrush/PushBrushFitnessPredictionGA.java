package com.unicellularcomic.CoPushBrush;

import java.util.ArrayList;

import org.spiderland.Psh.*;
import org.spiderland.Psh.Coevolution.*;

public class PushBrushFitnessPredictionGA extends PushFitnessPrediction{
	private static final long serialVersionUID = 1L;

	ArrayList<Integer> trainerIndices;
	
	@Override
	protected void InitFromParameters() throws Exception {
		trainerIndices = new ArrayList<Integer>();

		super.InitFromParameters();
	}
	
	/**
	 * Override to make sure trainers are not updated during the beginning
	 * of the generation.
	 */
	@Override
	protected void BeginGeneration(){
		trainerIndices.clear();
	}
	
	/**
	 * Override to change prediction individual type.
	 */
	@Override
	protected void InitIndividual(GAIndividual inIndividual) {
		
		PushBrushFitnessPredictionIndividual i = (PushBrushFitnessPredictionIndividual) inIndividual;
		
		int randomCodeSize = _RNG.nextInt(_maxRandomCodeSize) + 2;
		Program p = _interpreter.RandomCode(randomCodeSize);
		i.SetProgram(p);
		i.SetInterpreter(_interpreter);
		
	}
	
	/**
	 * Override to create an initially empty trainer population.
	 */
	@Override
	protected void InitTrainerPopulation() {
		_trainerPopulation = new ArrayList<PushGPIndividual>();
	}
	
	/**
	 * Override to make trainers have random fitnesses if not instantiated with
	 * human evaluated fitnesses.
	 */
	@Override
	protected void EvaluateTrainerFitnesses() {
		for(PushGPIndividual trainer : _trainerPopulation){
			if(!trainer.FitnessIsSet()){
				EvaluateSolutionIndividual(trainer);
			}	
		}
	}
	
	/**
	 * Adds a trainer to the trainer population.
	 * @param inTrainer
	 */
	public void AddTrainer(PushGPIndividual inTrainer){
		if(_trainerPopulation.size() >= _trainerPopulationSize){
			_trainerPopulation.remove(0);
		}
		
		_trainerPopulation.add(inTrainer);
		
	}
	
	/**
	 * Override to guarantee that the same individual isn't chosen for a trainer
	 * multiple times in the same generation
	 */
	@Override
	public PushGPIndividual ChooseNewTrainer() {
		ArrayList<Float> individualVariances = new ArrayList<Float>();
		
		for (int i = 0; i < _solutionGA.GetPopulationSize(); i++) {
			PushGPIndividual individual = (PushGPIndividual) _solutionGA
					.GetIndividualFromPopulation(i);

			ArrayList<Float> predictions = new ArrayList<Float>();
			for (int j = 0; j < _populations[_currentPopulation].length; j++) {
				PredictionGAIndividual predictor = (PredictionGAIndividual) _populations[_currentPopulation][j];
				predictions.add(predictor.PredictSolutionFitness(individual));		
			}

			individualVariances.add(Variance(predictions));
		}

		// Find individual with the highest variance
		int highestVarianceIndividual = 0;
		float highestVariance = individualVariances.get(0);
		
		while(trainerIndices.contains(highestVarianceIndividual)){
			highestVarianceIndividual++;
		}

		for (int i = 0; i < _solutionGA.GetPopulationSize(); i++) {
			if (!trainerIndices.contains(i)) {
				if (highestVariance < individualVariances.get(i)) {
					highestVarianceIndividual = i;
					highestVariance = individualVariances.get(i);
				}
			}
		}
		
		trainerIndices.add(highestVarianceIndividual);
		
		System.out.println("TTTTTTTTTT : " + trainerIndices);

		return (PushGPIndividual) _solutionGA
				.GetIndividualFromPopulation(highestVarianceIndividual);
	}

	
}
