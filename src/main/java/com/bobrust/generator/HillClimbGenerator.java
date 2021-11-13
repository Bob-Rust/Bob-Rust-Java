package com.bobrust.generator;

import java.util.ArrayList;
import java.util.List;

class HillClimbGenerator {
	private static State getBestRandomState(Worker worker, int count) {
		List<State> stateList = new ArrayList<>(count);
		for(int i = 0; i < count; i++) stateList.add(new State(worker));
		stateList.parallelStream().forEach(State::getEnergy);
		
		float bestEnergy = 0;
		State bestState = null;
		
		for(int i = 0, len = stateList.size(); i < len; i++) {
			State state = stateList.get(i);
			float energy = state.getEnergy();
			
			if(bestState == null || energy < bestEnergy) {
				bestEnergy = energy;
				bestState = state;
			}
		}
		
		return bestState;
	}
	
	public static State getHillClimb(State state, int maxAge) {
		State bestState = state;
		float minimumEnergy = state.getEnergy();
		
		// This function will minimize the nergy of the input state
		for(int i = 0; i < maxAge; i++) {
			State undo = state.doMove();
			float energy = state.getEnergy();
			
			if(energy >= minimumEnergy) {
				state.undoMove(undo);
			} else {
				minimumEnergy = energy;
				bestState = state;
				i = -1;
			}
		}
		
		return bestState;
	}
	
	public static State getBestHillClimbState(Worker worker, int max_random_states, int age, int times) {
		float bestEnergy = 0;
		State bestState = null;
		
		for(int i = 0; i < times; i++) {
			State oldstate = getBestRandomState(worker, max_random_states);
//			float before = oldstate.getEnergy();
			
			State state = getHillClimb(oldstate, age);
			float energy = state.getEnergy();
			
			if(i == 0 || bestEnergy > energy) {
				bestEnergy = energy;
				bestState = state;
			}
		}

		return bestState;
	}
}
