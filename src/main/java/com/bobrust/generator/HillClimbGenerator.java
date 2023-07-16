package com.bobrust.generator;

import com.bobrust.util.data.AppConstants;

import java.util.ArrayList;
import java.util.List;

class HillClimbGenerator {
	private static State getBestRandomState(Worker worker, int count) {
		List<State> stateList = new ArrayList<>(count);
		for (int i = 0; i < count; i++) stateList.add(new State(worker));
		stateList.parallelStream().forEach(State::getEnergy);
		
		float bestEnergy = 0;
		State bestState = null;
		
		for (int i = 0, len = stateList.size(); i < len; i++) {
			State state = stateList.get(i);
			float energy = state.getEnergy();
			
			if (bestState == null || energy < bestEnergy) {
				bestEnergy = energy;
				bestState = state;
			}
		}
		
		return bestState;
	}
	
	public static State getHillClimb(State state, int maxAge) {
		State bestState = state;
		float minimumEnergy = state.getEnergy();
		
		// Prevent infinite recursion
		int maxLoops = 4096;
		
		// This function will minimize the energy of the input state
		for (int i = 0; i < maxAge && (maxLoops-- > 0); i++) {
			State undo = state.doMove();
			float energy = state.getEnergy();
			
			if (energy >= minimumEnergy) {
				state.undoMove(undo);
			} else {
				minimumEnergy = energy;
				bestState = undo;
				i = -1;
			}
		}
		
		if (maxLoops <= 0 && AppConstants.DEBUG_GENERATOR) {
			AppConstants.LOGGER.warn("HillClimbGenerator failed to find a better shape after {} tries", 4096);
		}
		
		return bestState;
	}
	
	public static State getBestHillClimbState(Worker worker, int max_random_states, int age, int times) {
		float bestEnergy = 0;
		State bestState = null;
		
		for(int i = 0; i < times; i++) {
			State oldState = getBestRandomState(worker, max_random_states);
			State state = getHillClimb(oldState, age);
			float energy = state.getEnergy();
			
			if (i == 0 || bestEnergy > energy) {
				bestEnergy = energy;
				bestState = state;
			}
		}

		return bestState;
	}
}
