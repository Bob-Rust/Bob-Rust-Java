package com.bobrust.generator;

import java.util.ArrayList;
import java.util.List;

public class Model {
	private Worker worker;
	private BorstImage target;
	private BorstImage context;
	private int alpha;
		
	public List<Circle> shapes;
	public List<BorstColor> colors;
	public BorstImage current;
	public float score;
	public int width;
	public int height;

	public Model(BorstImage target, int backgroundRGB, int alpha) {
		int w = target.width;
		int h = target.height;
		this.shapes = new ArrayList<>();
		this.colors = new ArrayList<>();
		this.target = target;
		this.width = w;
		this.height = h;

		this.current = new BorstImage(w, h);
		for(int i = 0; i < w * h; i++) {
			current.pixels[i] = backgroundRGB;
		}
		
		this.score = BorstCore.differenceFull(target, current);
		this.context = new BorstImage(w, h);
		this.worker = new Worker(target, alpha);
		this.alpha = alpha;
	}

	public void Add(Circle shape) {
		BorstImage before = current.createCopy();

		Scanline[] lines = shape.Rasterize();
		BorstColor color = BorstCore.computeColor(target, current, lines, alpha);
		
		BorstCore.drawLines(current, color, lines, alpha);
		float sc = BorstCore.differencePartial(target, before, current, score, lines);
		
		this.score = sc;
		shapes.add(shape);
		colors.add(color);
		
		BorstCore.drawLines(context, color, lines, alpha);
	}

	public int Step() {
		worker.Init(current, score);
		State state = HillClimbGenerator.BestHillClimbState(worker, 1000, 100, 1);
		Add(state.shape);

		return worker.counter;
	}
};