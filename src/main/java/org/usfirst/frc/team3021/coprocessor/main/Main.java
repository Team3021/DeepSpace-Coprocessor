package org.usfirst.frc.team3021.coprocessor.main;

import org.usfirst.frc.team3021.coprocessor.camera.VisionProcessor;

public class Main {
	
	public static void main(String[] args) {
		
		VisionProcessor processor = new VisionProcessor();
		System.out.println("Starting Vision System");
		processor.init();
		
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
}
