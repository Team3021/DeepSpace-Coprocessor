package org.usfirst.frc.team3021.coprocessor.main;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class IO {
	
	public static Mat loadImage(String path) {
		try {
			Mat mat = Imgcodecs.imread(path);
			return mat;
		}
		catch (Exception e) {
			System.out.println("Error reading image!");
			return null;
		}
	}
	
	public static void writeImage(String filename, Mat image) {
		try {
			System.out.println("Now writing image...");
			Imgcodecs.imwrite(filename, image);
		}
		catch (Exception e) {
			System.out.println("Error writing image!");
		}
		System.out.println("Success!");
	}
	
}
