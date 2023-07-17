package com.bobrust.generator;

import com.bobrust.util.data.AppConstants;

import java.util.ArrayList;
import java.util.List;

class HillClimbGenerator {
	private static State getBestRandomState(List<State> random_states) {
		final int len = random_states.size();
		for (int i = 0; i < len; i++) {
			State state = random_states.get(i);
			state.score = -1;
			state.shape.randomize();
		}
		random_states.parallelStream().forEach(State::getEnergy);
		
		float bestEnergy = 0;
		State bestState = null;
		for (int i = 0; i < len; i++) {
			State state = random_states.get(i);
			float energy = state.getEnergy();
			
			if (bestState == null || energy < bestEnergy) {
				bestEnergy = energy;
				bestState = state;
			}
		}
		
		return bestState;
	}
	
	public static State getHillClimb(State state, int maxAge) {
		float minimumEnergy = state.getEnergy();
		
		// Prevent infinite recursion
		int maxLoops = 4096;
		
		State undo = state.getCopy();
		
		// This function will minimize the energy of the input state
		for (int i = 0; i < maxAge && (maxLoops-- > 0); i++) {
			state.doMove(undo);
			float energy = state.getEnergy();
			
			if (energy >= minimumEnergy) {
				state.fromValues(undo);
			} else {
				minimumEnergy = energy;
				i = -1;
			}
		}
		
		if (maxLoops <= 0 && AppConstants.DEBUG_GENERATOR) {
			AppConstants.LOGGER.warn("HillClimbGenerator failed to find a better shape after {} tries", 4096);
		}
		
		return state;
	}
	
	public static State getBestHillClimbState(List<State> random_states, int age, int times) {
		float bestEnergy = 0;
		State bestState = null;
		
		for (int i = 0; i < times; i++) {
			State oldState = getBestRandomState(random_states);
			State state = getHillClimb(oldState, age);
			float energy = state.getEnergy();
			
			if (i == 0 || bestEnergy > energy) {
				bestEnergy = energy;
				bestState = state.getCopy();
			}
		}

		return bestState;
	}
}
