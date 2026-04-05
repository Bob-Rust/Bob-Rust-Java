package com.bobrust.generator;

import com.bobrust.util.data.AppConstants;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

class HillClimbGenerator {
	private static State getBestRandomState(List<State> random_states, ErrorMap errorMap) {
		final int len = random_states.size();
		for (int i = 0; i < len; i++) {
			State state = random_states.get(i);
			state.score = -1;
			state.shape.randomize(errorMap);
		}

		if (AppConstants.USE_BATCH_PARALLEL) {
			// Spatial batching: sort by Y coordinate for cache locality,
			// then process in batches so nearby circles share L2 cache lines
			random_states.sort(Comparator.comparingInt(s -> s.shape.y));
			final int batchSize = 50;
			for (int batch = 0; batch < len; batch += batchSize) {
				final int start = batch;
				final int end = Math.min(batch + batchSize, len);
				// Process each batch in parallel but batches share Y-locality
				IntStream.range(start, end).parallel().forEach(i -> random_states.get(i).getEnergy());
			}
		} else {
			random_states.parallelStream().forEach(State::getEnergy);
		}

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

	/**
	 * Original hill climbing implementation. Kept as fallback when
	 * {@link AppConstants#USE_SIMULATED_ANNEALING} is false.
	 */
	public static State getHillClimbClassic(State state, int maxAge) {
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

	/**
	 * Simulated annealing implementation that can escape local minima by
	 * probabilistically accepting worse moves early in the search.
	 */
	public static State getHillClimbSA(State state, int maxAge) {
		float currentEnergy = state.getEnergy();
		State bestState = state.getCopy();
		float bestEnergy = currentEnergy;

		// Estimate initial temperature from sample mutations
		float temperature = estimateTemperature(state);
		int totalIterations = maxAge * 3; // 3x hill climb iterations balances exploration vs speed
		float coolingRate = computeCoolingRate(temperature, maxAge);

		State undo = state.getCopy();

		for (int i = 0; i < totalIterations; i++) {
			state.doMove(undo);
			float newEnergy = state.getEnergy();
			float delta = newEnergy - currentEnergy;

			if (delta < 0) {
				// Improvement — always accept
				currentEnergy = newEnergy;
				if (currentEnergy < bestEnergy) {
					bestEnergy = currentEnergy;
					bestState = state.getCopy();
				}
			} else if (temperature > 0.001f) {
				// Worse move — accept with probability exp(-delta/T)
				double acceptProb = Math.exp(-delta / temperature);
				if (ThreadLocalRandom.current().nextDouble() < acceptProb) {
					currentEnergy = newEnergy;
				} else {
					state.fromValues(undo);
				}
			} else {
				state.fromValues(undo);
			}

			temperature *= coolingRate;
		}

		// Return the best state found during the entire SA run
		return bestState;
	}

	/**
	 * Dispatches to SA or classic hill climbing based on the feature flag.
	 */
	public static State getHillClimb(State state, int maxAge) {
		if (AppConstants.USE_SIMULATED_ANNEALING) {
			return getHillClimbSA(state, maxAge);
		} else {
			return getHillClimbClassic(state, maxAge);
		}
	}

	/**
	 * Estimate a good starting temperature by sampling random mutations and
	 * measuring average energy deltas. Sets T so that roughly 60% of uphill
	 * moves are accepted at the start.
	 */
	static float estimateTemperature(State state) {
		State probe = state.getCopy();
		State undo = probe.getCopy();
		float totalDelta = 0;
		int samples = 10; // fewer probes for faster temperature estimation

		for (int i = 0; i < samples; i++) {
			float before = probe.getEnergy();
			probe.doMove(undo);
			float after = probe.getEnergy();
			totalDelta += Math.abs(after - before);
			probe.fromValues(undo); // restore
		}

		float avgDelta = totalDelta / samples;
		// Set T so ~60% of uphill moves are accepted initially
		// P = exp(-avgDelta / T) = 0.6 => T = -avgDelta / ln(0.6)
		// -1/ln(0.6) ≈ 1.957, but we use avgDelta / 0.5108 which is equivalent
		return (float) (avgDelta / 0.5108);
	}

	/**
	 * Compute the geometric cooling rate so that temperature decays from
	 * {@code initialTemp} to near-zero (0.001) over {@code maxAge * 10} iterations.
	 */
	static float computeCoolingRate(float initialTemp, int maxAge) {
		int totalIterations = maxAge * 3; // 3x hill climb iterations balances exploration vs speed
		float finalTemp = 0.001f;
		if (initialTemp <= finalTemp) {
			return 0.99f; // fallback if temperature is already tiny
		}
		// initialTemp * rate^totalIterations = finalTemp
		// rate = (finalTemp / initialTemp) ^ (1 / totalIterations)
		return (float) Math.pow(finalTemp / initialTemp, 1.0 / totalIterations);
	}

	public static State getBestHillClimbState(List<State> random_states, int age, int times) {
		return getBestHillClimbState(random_states, age, times, null);
	}

	public static State getBestHillClimbState(List<State> random_states, int age, int times, ErrorMap errorMap) {
		float bestEnergy = 0;
		State bestState = null;

		for (int i = 0; i < times; i++) {
			State oldState = getBestRandomState(random_states, errorMap);
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
