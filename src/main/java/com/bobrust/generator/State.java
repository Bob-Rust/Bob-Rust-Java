package com.bobrust.generator;

class State {
	private final Worker worker;
	
	public Circle shape;
	public float score;
		
	public State(Worker worker) {
		this.worker = worker;
		this.score = -1;
		this.shape = new Circle(worker);
	}

	public State(Worker worker, Circle sh, float score) {
		this.worker = worker;
		this.score = score;
		this.shape = sh;
	}
	
	public float getEnergy() {
		if(score < 0) {
			Scanline[] list = shape.Rasterize();
			score = worker.Energy(list);
		}

		return score;
	}

	public State doMove() {
		State oldState = getCopy();
		shape.Mutate();

		score = -1;
		return oldState;
	}

	public void undoMove(State oldState) {
		shape = oldState.shape;
		score = oldState.score;
	}

	public State getCopy() {
		Circle shape_cope = new Circle(worker, shape.x, shape.y, shape.r);
		return new State(worker, shape_cope, score);
	}
};