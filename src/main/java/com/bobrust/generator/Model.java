package com.bobrust.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

		Scanline[] lines = shape.Rasterize();
		BorstColor color = BorstCore.computeColor(target, current, lines, alpha);
		
		BorstCore.drawLines(current, color, lines, alpha);
		float sc = BorstCore.differencePartial(target, beforeImage, current, score, lines);
		
		this.score = sc;
		shapes.add(shape);
		colors.add(color);
		
		BorstCore.drawLines(context, color, lines, alpha);
	}
	
	public synchronized int processStep() {
		worker.Init(current, score);
		State state = HillClimbGenerator.BestHillClimbState(worker, 1000, 100, 1);
		addShape(state.shape);

		return worker.counter;
	}
};