package org.usfirst.frc.team3021.coprocessor.main;

import org.usfirst.frc.team3021.coprocessor.camera.VisionSystem;

public class Main {
	private static VisionSystem visionSystem;
	
	
	public static void main(String[] args) {
		
		System.out.println("Starting Vision System");
		
		visionSystem = new VisionSystem();
		
		for (;;) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
}
