package org.usfirst.frc.team3021.coprocessor.camera;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;

public class VisionSystem {
	
	private final String PREF_VISION_CAMERA_0_ENABLED = "Vision.camera.0.enabled";
	private final String PREF_VISION_CAMERA_1_ENABLED = "Vision.camera.1.enabled";
	
	private static final int USB_CAMERA_UNKNOWN = -1;
	private static final int USB_CAMERA_ZERO = 0;
	private static final int USB_CAMERA_ONE = 1;

	private boolean isInitialized = false;
	
	// Member Attributes
	private int curCamNum;
	
	// main camera server to share registered video inputs and outputs with the dashboard
	private CameraServer server;
	
	// usb cameras are video sources
	private UsbCamera cam0;  
	private UsbCamera cam1;
	
	// mjpeg server is a video sink that takes images and sends them to the dashboard
	private MjpegServer dashboardSink; 
	
	// our robot vision processing
	private VisionProcessor visionProcessor;
	
	public VisionSystem() {
		initialize();
	}
	
	private boolean isCamera0Enabled() {
		return true;
	}
	
	private boolean isCamera1Enabled() {
		return false;
	}
	
	public boolean isVisionEnabled() {
		return isCamera0Enabled() || isCamera1Enabled();
	}
	
	public void initialize() {
		
		if (isInitialized) {
			System.out.println("Vision already initialized");
			return;
		}
		
		isInitialized = true;
		
		if (!isVisionEnabled()) {
			// Don't initialize the camera objects and return right away
			System.out.println("WARNING !!! NO CAMERAS ENABLED");
			return;  
		}
		else {
			System.out.println("One or more cameras are enabled");
		}
		
		server = CameraServer.getInstance();
		
		// setup the mjpeg server to communicate with the smart dashboard
		dashboardSink = new MjpegServer("Vision Server 1", 1181);
		server.addServer(dashboardSink);
		
		// set up a usb camera on port 0
		if (isCamera0Enabled()) {
			cam0 = new UsbCamera("Active USB Camera", 0);

			cam0.setFPS(20);
			cam0.setResolution(VisionProcessor.FRAME_WIDTH, VisionProcessor.FRAME_HEIGHT);
			cam0.setExposureManual(1);
		}

		// set up a usb camera on port 1
		if (isCamera1Enabled()) {
			cam1 = new UsbCamera("Active USB Camera", 1);
			
			cam1.setFPS(20);
			cam1.setResolution(VisionProcessor.FRAME_WIDTH, VisionProcessor.FRAME_HEIGHT);
			cam0.setExposureManual(1);
		}
		
		VideoSource currentCam = null;
		curCamNum = USB_CAMERA_UNKNOWN;

		if (curCamNum == USB_CAMERA_UNKNOWN && isCamera0Enabled()) {
			curCamNum = USB_CAMERA_ZERO;
			currentCam = cam0;
		} else if (curCamNum == USB_CAMERA_UNKNOWN && isCamera1Enabled()) {
			curCamNum = USB_CAMERA_ONE;
			currentCam = cam1;
		}
		
		if (curCamNum != USB_CAMERA_UNKNOWN) {
			System.out.println("Camera starting.");
			
			// setup the vision processor device and connect a camera as the input source
			visionProcessor = new VisionProcessor(currentCam);
			
			// get the output from the vision processor device
			VideoSource visionProcessorOutput = visionProcessor.getOutput();
			
			// wire the output of the vision processor device to the dashboard
			dashboardSink.setSource(visionProcessorOutput);

			visionProcessor.play();
		} else {
			System.out.println("WARNING !!! NO CAMERAS FOUND");
		}
	}
}
