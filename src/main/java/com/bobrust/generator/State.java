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
		if (score < 0) {
			score = worker.getEnergy(shape);
		}

		return score;
	}
	
	public void doMove(State old) {
		old.fromValues(this);
		shape.mutateShape();
		score = -1;
	}

	public State getCopy() {
		Circle shape_cope = new Circle(worker, shape.x, shape.y, shape.r);
		return new State(worker, shape_cope, score);
	}
	
	public void fromValues(State state) {
		shape.fromValues(state.shape);
		score = state.score;
	}
}
