package org.usfirst.frc.team3021.coprocessor.camera;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team3021.coprocessor.main.IO;
import org.usfirst.frc.team3021.coprocessor.network.NetworkTableManager;
import org.usfirst.frc.team3021.coprocessor.processing.Drawing;
import org.usfirst.frc.team3021.coprocessor.processing.Filtering;
import org.usfirst.frc.team3021.coprocessor.processing.Targeting;
import org.usfirst.frc.team3021.coprocessor.target.HatchTarget;
import org.usfirst.frc.team3021.coprocessor.target.Target;
import org.usfirst.frc.team3021.robot.device.RunnableDevice;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;

public class VisionProcessor extends RunnableDevice {
	
	// The video resolution
	public static final int FRAME_WIDTH = 320;
	public static final int FRAME_HEIGHT = 240;
	
	public static final double TARGET_OFFSET_X = 0.0, TARGET_OFFSET_Y = 0.0;
	
	private final String PREF_TARGET_SCOPE_ENABLED = "VisionProcessor.target.scope.enabled";
	private final String PREF_TARGET_LOCATOR_ENABLED = "VisionProcessor.target.locator.enabled";
	
	private CvSink input; // Video sink that will receive images from the camera source

	private CvSource output;
	
	private Mat image; 
	private Mat processed;
	private boolean targetScopeEnabled = false;
	private boolean targetLocatorEnabled = false;

	public VisionProcessor(VideoSource initialCam) {
		// Setup a CvSink. This will capture Mats from an external source
		input = new CvSink("Camera Sink");
		input.setSource(initialCam);
		
		// Setup a CvSource. This will send images back to an external sink
		output = CameraServer.getInstance().putVideo("Robot : Vision", FRAME_WIDTH, FRAME_HEIGHT);
		
		// Mats are very memory intensive to construct
		image = new Mat();
		processed = new Mat();
	}

	public void setInput(VideoSource source) {
		input.setSource(source);	
	}
	
	public VideoSource getOutput() {
		return output;
	}

	public void setTargetScopeEnabled(boolean newValue) {
		this.targetScopeEnabled = newValue;
	}

	public void setTargetLocatorEnabled(boolean newValue) {
		this.targetLocatorEnabled = newValue;
	}
	
	private boolean isTargetScopeEnabled() {
		return targetScopeEnabled;
	}
	
	private boolean isTargetLocatorEnabled() {
		return targetLocatorEnabled;
	}
	
	public HatchTarget findTarget(Mat image) {
		Filtering.grayscale(image, processed);
	
		Imgproc.Canny(processed, processed, 45, 50);
		
		List<MatOfPoint> contours = Filtering.getContours(processed);
		
		List<MatOfPoint> contoursFiltered = new ArrayList<>();
		List<RotatedRect> rectangles = Filtering.getRotatedRectangles(contours, contoursFiltered);
		
		return Targeting.getTarget(rectangles);
	}
	
	@Override
	protected void runPeriodic() {

		// Grab a frame from the source camera
		// If there is an error notify the output
		if (input.grabFrame(image) == 0) {
			// Send the output the error.
			output.notifyError(input.getError());

			// skip the rest of the current iteration
			System.out.println("Unable to grab frame.");
			delay(50);
			return;
		}

//		// Draw a target location on the image
//		if (isTargetLocatorEnabled()) {
//			targetLocator.draw(mat);
//		}
//
//		// Draw a target scope on the image
//		if (isTargetScopeEnabled()) {
//			targetScope.draw(mat);
//		}

		HatchTarget target = findTarget(image);
		
		if (target != null) {
			double center_x = image.width() * 0.5 + TARGET_OFFSET_X;
			double center_y = image.height() * 0.5 + TARGET_OFFSET_Y;
			
			// Image coordinates start in the top-left corner
			double dx = target.getCenter().x - center_x;
			double dy = center_y - target.getCenter().y;
			
			StringBuilder values = new StringBuilder();
			values.append("(").append(dx).append(", ").append(dy).append(")");
			String data = values.toString();
			Drawing.drawText(image, new Point(30, image.height() - 30), data);

			List<RotatedRect> targets = target.getRotatedRects();
			Drawing.drawRotatedRectangles(image, targets, true);
			
			output.putFrame(image);
			
			NetworkTableManager.getInstance().write("TargetFound", "true");
			NetworkTableManager.getInstance().write("dx", Double.toString(dx));
			NetworkTableManager.getInstance().write("dy", Double.toString(dy));
		}
		else {
			output.putFrame(image);
			
			NetworkTableManager.getInstance().write("TargetFound", "false");
		}
		
		// Give the frame to the output
		// Core.rotate(mat, mat, Core.ROTATE_90_COUNTERCLOCKWISE);
		

		delay(100);
	}
}
