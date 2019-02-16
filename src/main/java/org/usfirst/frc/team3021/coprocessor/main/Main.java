package org.usfirst.frc.team3021.coprocessor.main;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.usfirst.frc.team3021.coprocessor.camera.VisionSystem;
import org.usfirst.frc.team3021.coprocessor.processing.Drawing;
import org.usfirst.frc.team3021.coprocessor.processing.Filtering;
import org.usfirst.frc.team3021.coprocessor.processing.Targeting;
import org.usfirst.frc.team3021.coprocessor.target.HatchTarget;

public class Main {
	private static VisionSystem visionSystem;
	
	public static void init() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) {
		init();
		
		visionSystem = new VisionSystem();
	}
	
}
