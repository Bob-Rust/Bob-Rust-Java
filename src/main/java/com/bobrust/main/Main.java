package com.bobrust.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.bobrust.generator.*;
import com.bobrust.generator.BorstGenerator.BorstGeneratorBuilder;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;
import com.bobrust.gui.BobRust;

public class Main {
	public static void main(String[] args) {
		BobRust gui = new BobRust();
		
		//new Main();
	}
	
	public Main() {
		BorstSettings settings = new BorstSettings();
		settings.ImagePath = "src/main/resources/examples/wolf_512.png";
		settings.MaxShapes = 2000;
		settings.CallbackShapes = 100;
		settings.Background = 0xff000000;
		settings.Alpha = 2;
		settings.Width = 512;
		settings.Height = 512;
		
		BorstGenerator generator = new BorstGeneratorBuilder()
			.setSettings(settings)
			.setCallback((data) -> {
				Model model = data.getModel();
				int step = data.getIndex();
				
				BlobList list = new BlobList();
				for(int i = 0, len = model.colors.size(); i < len; i++) {
					Circle circle = model.shapes.get(i);
					BorstColor color = model.colors.get(i);
					list.add(Blob.get(circle.x, circle.y, circle.r, color.rgba));
				}
				
//				int oldScore = score(list);
				BlobList sorted_list = BorstSorter.sort(list);
//				int newScore = score(sorted_list);
//				System.out.printf("Score: old: %d, new: %d\n", oldScore, newScore);
				
				BufferedImage bi = model.current.image;
				
				try {
					ImageIO.write(bi, "png", new File("src/main/resources/output/output_%d.png".formatted(step)));
				} catch(IOException e) {
					e.printStackTrace();
				}
				
				try(FileWriter writer = new FileWriter(new File("src/main/resources/output/output_%d.svg".formatted(step)))) {
					writer.append(generate_svg(settings, sorted_list));
				} catch(IOException e) {
					e.printStackTrace();
				}
			})
			.create();
		
		generator.start();
		
		try {
			System.out.println("Joining the generator.");
			generator.join();
			System.out.println("Generator is done.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String generate_svg(BorstSettings settings, BlobList list) {
		StringBuilder sb = new StringBuilder();
		int width = settings.Width;
		int height = settings.Height;
		String bg = "#%06x".formatted(settings.Background & 0xffffff);
		
		sb.append(String.format(
			("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"%d\" height=\"%d\">\n"
			+ "<rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" fill=\"%s\"/>\n"
			+ "<g transform=\"scale(1) translate(0.5 0.5)\">\n"),
			width, height, width, height, bg
		));
		
		double alpha = BorstUtils.ALPHAS[settings.Alpha] / 255.0;
		for(Blob blob : list.list()) {
			String color = "#%06x".formatted(blob.color & 0xffffff);
			sb.append(String.format(
				Locale.US, "<ellipse fill=\"%s\" fill-opacity=\"%.4f\" cx=\"%d\" cy=\"%d\" rx=\"%d\" ry=\"%d\"/>\n",
				color, alpha, blob.x, blob.y, blob.size, blob.size
			));
		}
		
		sb.append("</g>\n</svg>\n");
		return sb.toString();
	}

	public int score(BlobList data) {
		if(data == null) return 0;
		return score(data.list(), data.size());
	}
	
	public int score(BlobList data, int shapes) {
		if(data == null) return 0;
		return score(data.list(), shapes);
	}
	
	public int score(List<Blob> list, int shapes) {
		Blob last = null;
		
		int index = 0;
		int changes = 2;
		for(Blob blob : list) {
			if(index++ > shapes) break;
			if(last != null) {
				if(last.size != blob.size) changes++;
				if(last.color != blob.color) changes++;
			}
			
			last = blob;
		}
		
		return changes;
	}
}
