package org.usfirst.frc.team3021.coprocessor.camera;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team3021.coprocessor.network.NetworkTableManager;
import org.usfirst.frc.team3021.coprocessor.processing.Drawing;
import org.usfirst.frc.team3021.coprocessor.processing.Filtering;
import org.usfirst.frc.team3021.coprocessor.processing.Targeting;
import org.usfirst.frc.team3021.coprocessor.target.HatchTarget;
import org.usfirst.frc.team3021.robot.device.RunnableDevice;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;

public class VisionProcessor extends RunnableDevice {
	
	// The video resolution
	public static final int FRAME_WIDTH = 320;
	public static final int FRAME_HEIGHT = 240;
	
	public static final double TARGET_OFFSET_X = 0.0, TARGET_OFFSET_Y = 0.0;
	
	private final String PREF_TARGET_SCOPE_ENABLED = "VisionProcessor.target.scope.enabled";
	private final String PREF_TARGET_LOCATOR_ENABLED = "VisionProcessor.target.locator.enabled";
	
	private boolean isInitialized;
	
	private UsbCamera[] cams;
	private int curCamNum;
	
	private CameraServer server;
	
	// MjpegServer is a video sink that takes images and sends them to the dashboard
	private MjpegServer dashboardSink;
	
	private CvSink input; // Video sink that will receive images from the camera source
	private CvSource output;
	
	private Mat image; 
	private Mat processed;
	private boolean targetScopeEnabled = false;
	private boolean targetLocatorEnabled = false;

	// TODO What if this code starts before the RoboRio code and these values haven't been written yet? Would this configure itself incorrectly?
	
	public boolean init() {
		if (isInitialized) {
			return false;
		}
		
		isInitialized = true;
		
		server = CameraServer.getInstance();
		
		// setup the mjpeg server to communicate with the smart dashboard
		dashboardSink = new MjpegServer("Vision Server 1", 1181);
		server.addServer(dashboardSink);
		
		boolean[] enabledCams = NetworkTableManager.readBooleanArray("Cameras Enabled", new boolean[0]);
		cams = new UsbCamera[enabledCams.length];
		
		boolean isVisionEnabled = false;
		
		UsbCamera initialCam = null;
		for (int i = 0; i < enabledCams.length; i++) {
			if (enabledCams[i]) {
				UsbCamera cam = new UsbCamera("Active USB Camera", i); // TODO Fix resource leak somehow?
				cam.setFPS(20);
				cam.setResolution(FRAME_WIDTH, FRAME_HEIGHT);
				
				cams[i] = cam;
				if (initialCam == null)
					initialCam = cam;
				
				isVisionEnabled = true;
			}
		}
		
		// Return immediately if no cameras are enabled
		if (!isVisionEnabled || initialCam == null) {
			System.out.println("WARNING: No cameras found or enabled!");
			return false;
		}
		
		
		// Setup a CvSink. This will capture Mats from an external source
		input = new CvSink("Camera Sink");
		input.setSource(initialCam);
		
		// Setup a CvSource. This will send images back to an external sink
		output = CameraServer.getInstance().putVideo("Robot : Vision", FRAME_WIDTH, FRAME_HEIGHT);
		
		// Mats are very memory intensive to construct
		image = new Mat();
		processed = new Mat();
		
		play();
		return true;
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
		// Check which camera to use and switch if necessary
		toggleCamera();
		
		// Grab a frame from the source camera
		// If there is an error notify the output
		if (input.grabFrame(image) == 0) {
			// Send the output the error.
			output.notifyError(input.getError());

			// skip the rest of the current iteration
			System.out.println("Unable to grab frame");
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
			
			NetworkTableManager.write("Target Found", "true");
			NetworkTableManager.write("dx", Double.toString(dx));
			NetworkTableManager.write("dy", Double.toString(dy));
		}
		else {
			output.putFrame(image);
			
			NetworkTableManager.write("Target Found", "false");
		}
		
		// Give the frame to the output
		// Core.rotate(mat, mat, Core.ROTATE_90_COUNTERCLOCKWISE);
		

		delay(100);
	}
	
	private void toggleCamera() {
		int switchingTo = (int) NetworkTableManager.readNumber("Current Camera", curCamNum);
		if (switchingTo >= cams.length) {
			System.out.println("WARNING: Attempted to switch to a camera ID out of bounds!");
			NetworkTableManager.write("Current Camera", curCamNum);
		}
		else if (cams[switchingTo] == null) {
			System.out.println("WARNING: Attempted to switch to a disabled camera!");
			NetworkTableManager.write("Current Camera", curCamNum);
		}
		else if (switchingTo != curCamNum) {
			curCamNum = switchingTo;
			setInput(cams[switchingTo]);
		}
	}
	
	public boolean isCameraEnabled(int id, boolean defaultValue) {
		boolean[] statuses = NetworkTableManager.readBooleanArray("Cameras Enabled", new boolean[0]);
		if (id >= statuses.length)
			return defaultValue;
		else
			return statuses[id];
		
	}
	
}
