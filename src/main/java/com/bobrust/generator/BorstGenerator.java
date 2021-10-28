package com.bobrust.generator;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import com.bobrust.logging.LogUtils;

public class BorstGenerator {
	private final Consumer<BorstData> callback;
	private final BorstSettings settings;
	private volatile Thread thread;
	
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
		if(thread != null) {
			LogUtils.warn("BorstGenerator has already been started! Restarting generator");
			try {
				stop();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		int length = settings.MaxShapes;
		int interval = settings.CallbackShapes;
		int background = settings.Background;
		int alpha = BorstUtils.ALPHAS[settings.Alpha];
		BorstImage image;
		if(settings.DirectImage != null) {
			image = new BorstImage(settings.DirectImage);
		} else {
			try {
				image = BorstImage.loadBorstImage(settings.ImagePath, settings.Width, settings.Height);
			} catch(IOException e) {
				LogUtils.error("Failed to load borst image '%s'", settings.ImagePath);
				e.printStackTrace();
				return false;
			}
		}
		
		Thread thread = new Thread(() -> {
			Model model = new Model(image, background, alpha);
			BorstData data = new BorstData(model);
			
			try {
				for(int i = 0; i <= length; i++) {
					@SuppressWarnings("unused")
					int n = model.Step();
					
					// Return if the current thread is interrupted.
					if(Thread.currentThread().isInterrupted()) {
						return;
					}
					
					if((i == length) || (i % interval) == 0) {
						data.index = i;
						this.callback.accept(data);
					}
				}
			} finally {
				data.index = length;
				this.callback.accept(data);
			}
		}, "Borst Generator Thread");
		thread.setDaemon(true);
		this.thread = thread;
		thread.start();
		return true;
	}
	
	/**
	 * Calling this method will close the thread created by this generator.
	 * This method will wait until the thread is fully closed.
	 */
	public synchronized void stop() throws InterruptedException {
		if(thread != null) {
			try {
				// Interrupt the thread and join it to wait for it to close.
				thread.interrupt();
				thread.join();
			} finally {
				// Make sure that the thread keeps it's interrupted state.
				thread.interrupt();
				thread = null;
			}
		}
	}
	
	/**
	 * Join the current thread.
	 * @throws InterruptedException
	 */
	public synchronized void join() throws InterruptedException {
		if(thread != null) {
			thread.join();
		}
	}
	
	public class BorstData {
		private final Model model;
		private int index;
		
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
	}
	
	public static class BorstGeneratorBuilder {
		private Consumer<BorstData> callback;
		private BorstSettings settings;
		
		public BorstGeneratorBuilder() {
			
		}
		
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
