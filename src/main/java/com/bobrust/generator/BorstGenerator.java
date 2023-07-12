package com.bobrust.generator;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.util.RustConstants;

public class BorstGenerator {
	private static final Logger LOGGER = LogManager.getLogger(BorstGenerator.class);
	private final Consumer<BorstData> callback;
	private final BorstSettings settings;
	private volatile Thread thread;
	private volatile int index;
	
	private BorstGenerator(BorstSettings settings, Consumer<BorstData> callback) {
		this.callback = Objects.requireNonNull(callback);
		this.settings = Objects.requireNonNull(settings);
	}
	
	public synchronized boolean isRunning() {
		return thread != null;
	}
	
	/**
	 * Start the generator
	 */
	public synchronized boolean start() {
		if (thread != null) {
			LOGGER.warn("BorstGenerator has already been started! Restarting generator");
			
			try {
				thread.interrupt();
				thread.join();
			} catch (InterruptedException e) {
				LOGGER.warn("Borst generator was interrupted");
				Thread.currentThread().interrupt();
				return false;
			}
		}
		
		if (settings.DirectImage == null) {
			LOGGER.error("Failed to load borst image");
			return false;
		}
		
		BorstImage image = new BorstImage(settings.DirectImage);
		int length = settings.MaxShapes;
		int interval = settings.CallbackInterval;
		int background = settings.Background;
		int alpha = BorstUtils.ALPHAS[settings.Alpha];
		
		Thread thread = new Thread(() -> {
			this.index = 0;
			
			Model model = new Model(image, background, alpha);
			BorstData data = new BorstData(model);
			
			try {
				long begin = System.nanoTime();
				for (int i = 0; i <= length; i++) {
					int n = model.processStep();
					long end = System.nanoTime();
					
					// Return if the current thread is interrupted.
					if (Thread.currentThread().isInterrupted()) {
						Thread.currentThread().interrupt();
						return;
					}
					
					this.index = i;
					if ((i == length) || (i % interval) == 0) {
						data.index = i;
						callback.accept(data);
						
						if (RustConstants.DEBUG_GENERATOR) {
							double time = (end - begin) / 1000000000.0;
							double sps = i / time;
							
							LOGGER.debug("{}: t={} s, score={}, n={}, s/s={}",
								"%5d".formatted(i),
								"%.3f".formatted(time),
								"%.6f".formatted(model.score),
								n,
								"%.2f".formatted(sps)
							);
						}
					}
				}
			} finally {
				data.index = length;
				data.done = true;
				callback.accept(data);
			}
		}, "Borst Generator Thread");
		this.thread = thread;
		thread.setDaemon(true);
		thread.start();
		return true;
	}
	
	/**
	 * Calling this method will close the thread created by this generator.
	 * This method will wait until the thread is fully closed.
	 */
	public synchronized void stop() throws InterruptedException {
		if (thread != null) {
			try {
				// Interrupt the thread and join it to wait for it to close.
				thread.interrupt();
				thread.join();
			} finally {
				// Make sure that the thread keeps it's interrupted state.
				thread = null;
			}
		}
	}
	
	public class BorstData {
		private final Model model;
		private int index;
		private boolean done;
		
		private BorstData(Model model) {
			this.model = model;
		}
		
		public BorstGenerator getGenerator() {
			return BorstGenerator.this;
		}
		
		public BorstSettings getSettings() {
			return BorstGenerator.this.settings;
		}
		
		public Model getModel() {
			return model;
		}
		
		public int getIndex() {
			return index;
		}
		
		public boolean isDone() {
			return done;
		}
	}
	
	public static class BorstGeneratorBuilder {
		private Consumer<BorstData> callback;
		private BorstSettings settings;
		
		public BorstGeneratorBuilder setCallback(Consumer<BorstData> consumer) {
			this.callback = consumer;
			return this;
		}
		
		public BorstGeneratorBuilder setSettings(BorstSettings settings) {
			this.settings = settings;
			return this;
		}
		
		public BorstGenerator create() {
			return new BorstGenerator(settings, callback);
		}
	}
}
