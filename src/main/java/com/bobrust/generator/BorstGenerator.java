package com.bobrust.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.bobrust.generator.sorter.Blob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.util.data.AppConstants;

public class BorstGenerator {
	private static final Logger LOGGER = LogManager.getLogger(BorstGenerator.class);
	private final Consumer<BorstData> callback;
	private final BorstSettings settings;
	private volatile Thread thread;
	
	// Sent as a callback
	private final BorstData data = new BorstData();
	
	public BorstGenerator(BorstSettings settings, Consumer<BorstData> callback) {
		this.callback = Objects.requireNonNull(callback);
		this.settings = Objects.requireNonNull(settings);
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
		data.clear();
		
		Thread thread = new Thread(() -> {
			if (AppConstants.DEBUG_DRAWN_COLORS) {
				synchronized (data) {
					data.blobs.clear();
					data.blobs.addAll(generateDebugDrawList());
					data.index = data.blobs.size();
					data.alpha = 0;
					callback.accept(data);
				}
				
				return;
			}
			
			Model model = new Model(image, background, alpha);
			
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
					
					if ((i == length) || (i % interval) == 0) {
						synchronized (data) {
							data.update(model, i);
							callback.accept(data);
						}
						
						if (AppConstants.DEBUG_GENERATOR) {
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
				// TODO: Handle if we were stopped early
				synchronized (data) {
					data.index = length;
					data.done = true;
					data.update(model, length);
					callback.accept(data);
				}
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
	
	/**
	 * Generate a debug set of drawable blobs
	 * 
	 * This is used for calibrating the shape sizes
	 */
	private List<Blob> generateDebugDrawList() {
		List<Blob> list = new ArrayList<>();
		
		int xo = 64;
		int yo = 64;
		int ml = 32;
		
		// Draw shape sizes
		for (int shape = 0; shape < 2; shape++) {
			for (int size = 0; size < 6; size++) {
				int x = size * ml + xo;
				int y = shape * ml + yo;
				list.add(Blob.of(
					x,
					y,
					BorstUtils.SIZES[size],
					0,
					BorstUtils.ALPHAS[5],
					shape == 0
						? AppConstants.CIRCLE_SHAPE
						: AppConstants.SQUARE_SHAPE
				));
			}
		}
		
		// Draw alpha
		for (int alpha = 0; alpha < 6; alpha++) {
			int x = (alpha) * ml + xo;
			int y = 3 * ml + yo;
			
			list.add(Blob.of(
				x,
				y,
				BorstUtils.SIZES[5],
				0,
				BorstUtils.ALPHAS[alpha],
				AppConstants.SQUARE_SHAPE
			));
		}
		
		// Draw colors
		int maxWidth = 16;
		for (int color = 0; color < BorstUtils.COLORS.length; color++) {
			int x = (color % maxWidth) * ml + (7) * ml + xo;
			int y = (color / maxWidth) * ml + yo;
			list.add(Blob.of(
				x,
				y,
				BorstUtils.SIZES[5],
				BorstUtils.COLORS[color].rgb,
				BorstUtils.ALPHAS[5],
				AppConstants.SQUARE_SHAPE
			));
		}
		
		return list;
	}
	
	/**
	 * When accessing this object you must first synchronize with it
	 * 
	 * <pre>
	 * synchronized (data) {
	 *     
	 * }
	 * </pre>
	 */
	public static class BorstData {
		private final List<Blob> blobs;
		private int alpha;
		private int index;
		private boolean done;
		
		private BorstData() {
			this.blobs = new ArrayList<>();
		}
		
		private synchronized void update(Model model, int index) {
			this.index = index;
			this.alpha = model.alpha;
			
			// For all new elements
			var shapes = model.shapes;
			var colors = model.colors;
			for (int i = blobs.size(); i < shapes.size(); i++) {
				var shape = shapes.get(i);
				var color = colors.get(i);
				blobs.add(Blob.of(
					shape.x,
					shape.y,
					shape.r,
					color.rgb,
					model.alpha,
					AppConstants.CIRCLE_SHAPE
				));
			}
		}
		
		private synchronized void clear() {
			this.done = false;
			this.alpha = 0;
			this.index = 0;
			this.blobs.clear();
		}
		
		public synchronized List<Blob> getBlobs() {
			return blobs;
		}
		
		public synchronized int getAlpha() {
			return alpha;
		}
		
		public synchronized int getIndex() {
			return index;
		}
	}
}
