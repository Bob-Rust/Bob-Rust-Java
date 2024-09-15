package com.bobrust.generator;

import com.bobrust.robot.BobRustPainter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class CircleCache {
	private static final String CIRCLE_CONFIG_PATH = "circle_config.json";
	private static Map<String, Integer> circleSizes = new TreeMap<>();
	private static final Logger LOGGER = LogManager.getLogger(BobRustPainter.class);


	private static  Scanline[] CIRCLE_0 ;
	private static  Scanline[] CIRCLE_1 ;
	private static  Scanline[] CIRCLE_2 ;
	private static  Scanline[] CIRCLE_3 ;
	private static  Scanline[] CIRCLE_4 ;
	private static  Scanline[] CIRCLE_5;
	//private static  Scanline[] CIRCLE_6;
	public static  Scanline[][] CIRCLE_CACHE ;
	public static final int[] CIRCLE_CACHE_LENGTH;

    public static  int CIRCLE_0_VALUE = 3;
    public static  int CIRCLE_1_VALUE = 6;
    public static  int CIRCLE_2_VALUE = 12;
    public static  int CIRCLE_3_VALUE = 25;
    public static  int CIRCLE_4_VALUE = 50;
    public static  int CIRCLE_5_VALUE =100;

    static {		loadCircleSizes();
		CIRCLE_0 = generateCircle(circleSizes.getOrDefault("CIRCLE_0", CIRCLE_0_VALUE));
		CIRCLE_1 = generateCircle(circleSizes.getOrDefault("CIRCLE_1", CIRCLE_1_VALUE));
		CIRCLE_2 = generateCircle(circleSizes.getOrDefault("CIRCLE_2", CIRCLE_2_VALUE));
		CIRCLE_3 = generateCircle(circleSizes.getOrDefault("CIRCLE_3", CIRCLE_3_VALUE));
		CIRCLE_4 = generateCircle(circleSizes.getOrDefault("CIRCLE_4", CIRCLE_4_VALUE));
		CIRCLE_5 = generateCircle(circleSizes.getOrDefault("CIRCLE_5", CIRCLE_5_VALUE));
		//CIRCLE_6 = generateCircle(circleSizes.getOrDefault("CIRCLE_6", 70));
		CIRCLE_CACHE = new Scanline[][]{CIRCLE_0, CIRCLE_1, CIRCLE_2, CIRCLE_3, CIRCLE_4, CIRCLE_5
              // ,CIRCLE_6
        };
		CIRCLE_CACHE_LENGTH = new int[CIRCLE_CACHE.length];
		for (int i = 0; i < CIRCLE_CACHE.length; i++) {
			CIRCLE_CACHE_LENGTH[i] = CIRCLE_CACHE[i].length;
		}

	}

	private static Scanline[] generateCircle(int size) {
		LOGGER.info("circle size " +size);
		boolean[] grid = new boolean[size * size];
		for (int i = 0; i < size * size; i++) {
			double px = (int) (i % size) + 0.5;
			double py = (int) (i / size) + 0.5;
			double x = (px / (double) size) * 2.0 - 1;
			double y = (py / (double) size) * 2.0 - 1;

			double magnitudeSqr = x * x + y * y;
			grid[i] = magnitudeSqr <= 1;
		}

		Scanline[] scanlines = new Scanline[size];
		for (int i = 0; i < size; i++) {
			int start = size;
			int end = 0;
			for (int j = 0; j < size; j++) {
				if (grid[i * size + j]) {
					start = Math.min(start, j);
					end = Math.max(end, j);
				}
			}

			if (start <= end) {
				int off = size / 2;
				scanlines[i] = new Scanline(i - off, start - off, end - off);
			}
		}
		return scanlines;
	}

	private static void loadCircleSizes() {
		if (Files.exists(Paths.get(CIRCLE_CONFIG_PATH))) {
			try (FileReader reader = new FileReader(CIRCLE_CONFIG_PATH)) {
				Gson gson = new Gson();
				Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
				circleSizes = gson.fromJson(reader, mapType);
				for (Map.Entry<String, Integer> entry : circleSizes.entrySet()) {
					LOGGER.info("circle: " + entry.getKey() + ". Value loaded: " + entry.getValue());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			circleSizes.put("CIRCLE_0", CIRCLE_0_VALUE);
			circleSizes.put("CIRCLE_1", CIRCLE_1_VALUE);
			circleSizes.put("CIRCLE_2", CIRCLE_2_VALUE);
			circleSizes.put("CIRCLE_3", CIRCLE_3_VALUE);
			circleSizes.put("CIRCLE_4", CIRCLE_4_VALUE);
			circleSizes.put("CIRCLE_5", CIRCLE_5_VALUE);
			//circleSizes.put("CIRCLE_6", 70);
			saveDefaultCircleSizes();
		}
	}

	private static void saveDefaultCircleSizes() {
		try (FileWriter writer = new FileWriter(CIRCLE_CONFIG_PATH)) {
			Gson gson = new Gson();
			gson.toJson(circleSizes, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
