package com.bobrust.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Internal representation of the approximation model
 */
public class Model {
	private final Worker worker;
	private final BorstImage target;
	private final BorstImage context;
	private final BorstImage beforeImage;
	public final BorstImage current;
		
	public final List<Circle> shapes;
	public final List<BorstColor> colors;
	public final int alpha;
	public final int width;
	public final int height;
	protected float score;

	public Model(BorstImage target, int backgroundRGB, int alpha) {
		int w = target.width;
		int h = target.height;
		this.shapes = new ArrayList<>();
		this.colors = new ArrayList<>();
		this.target = target;
		this.width = w;
		this.height = h;

		this.current = new BorstImage(w, h);
		Arrays.fill(this.current.pixels, backgroundRGB);
		this.beforeImage = new BorstImage(w, h);
		
		this.score = BorstCore.differenceFull(target, current);
		this.context = new BorstImage(w, h);
		this.worker = new Worker(target, alpha);
		this.alpha = alpha;
	}

	private void addShape(Circle shape) {
		beforeImage.draw(current);

		int cache_index = BorstUtils.getClosestSizeIndex(shape.r);
		BorstColor color = BorstCore.computeColor(target, current, alpha, cache_index, shape.x, shape.y);
		
		BorstCore.drawLines(current, color, alpha, cache_index, shape.x, shape.y);
		this.score = BorstCore.differencePartial(target, beforeImage, current, score, cache_index, shape.x, shape.y);
		shapes.add(shape);
		colors.add(color);
		
		BorstCore.drawLines(context, color, alpha, cache_index, shape.x, shape.y);
	}
	
	private static final int max_random_states = 1000;
	private static final int age = 100;
	private static final int times = 1;
	
	private List<State> randomStates;
	
	public int processStep() {
		worker.init(current, score);
		if (randomStates == null) {
			randomStates = new ArrayList<>();
			for (int i = 0; i < max_random_states; i++) {
				randomStates.add(new State(worker));
			}
		}
		
		State state = HillClimbGenerator.getBestHillClimbState(randomStates, age, times);
		addShape(state.shape);

		return worker.counter;
	}
};