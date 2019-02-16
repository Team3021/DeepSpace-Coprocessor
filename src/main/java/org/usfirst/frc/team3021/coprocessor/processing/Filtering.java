package org.usfirst.frc.team3021.coprocessor.processing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/** 
 * For code to find bounding boxes, please see Targeting. 
 */
public class Filtering {
	public static final Scalar 
	POINT_COLOR = new Scalar(0, 0, 255),
	LINE_COLOR = new Scalar(0, 0, 255), 
	ROTATED_COLOR = new Scalar(0, 100, 255);
	
	public static final double 
	/** The amount by which the angle of any box may be different from the expected value */
	ANGLE_TOLERANCE = 1.0;
	
	public static final int HIST_SIZE = 256;
	
	// Define constructor to make private
	private Filtering() {
		
	}
	
	public static Mat canny(Mat image, int thresh1, int thresh2) {
		Mat edges = new Mat();
		Imgproc.Canny(image, edges, thresh1, thresh2);
		return edges;
	}
	
	public static Mat autoCanny(Mat image, double sigma) { // Suggested sigma: 0.33
		double median = median(image);
		
		int lower = (int) Math.max(0, (1.0 - sigma) * median);
		int upper = (int) Math.min(255, (1.0 + sigma) * median);
		Mat edged = canny(image, lower, upper);
		
		System.out.println("Median: " + median + "; Canny thresholds: [" + lower + ", " + upper + "]");
		
		return edged;
	}
	
	public static double median(Mat channel) {
		ArrayList<Mat> listOfMat = new ArrayList<>();
		listOfMat.add(channel);
		MatOfInt channels = new MatOfInt(0);
		Mat mask = new Mat();
		Mat hist = new Mat(256, 1, CvType.CV_8UC1);
		MatOfInt histSize = new MatOfInt(256);
		MatOfFloat ranges = new MatOfFloat(0, 256);

		Imgproc.calcHist(listOfMat, channels, mask, hist, histSize, ranges);

		double t = channel.rows() * channel.cols() / 2;
		double total = 0;
		int med = -1;
		for (int row = 0; row < hist.rows(); row++) {
			double val = hist.get(row, 0)[0];
			if ((total <= t) && (total + val >= t)) {
				med = row;
				break;
			}
			total += val;
		}

		return med;
	}
	
	public static Mat gaussianBlur(Mat image, Size size, int factor) {
		Mat blur = new Mat();
		Imgproc.GaussianBlur(image, blur, size, factor);
		return blur;
	}
	
	public static Mat grayscale(Mat image) {
		Mat grayscale = new Mat();
		Imgproc.cvtColor(image, grayscale, Imgproc.COLOR_BGR2GRAY);
		return grayscale;
	}
	
	public static void grayscale(Mat image, Mat output) {
		Imgproc.cvtColor(image, output, Imgproc.COLOR_BGR2GRAY);
	}
	
	public static List<MatOfPoint> getContours(Mat image) {
		List<MatOfPoint> contours = new ArrayList<>();
		Mat contoured = new Mat();
		Imgproc.findContours(image, contours, contoured, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		return contours;
	}
	
	public static MatOfPoint2f approximate(MatOfPoint contour) {
		double epsilon = 0.015 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
		MatOfPoint2f approx = new MatOfPoint2f();
		Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, epsilon, true);
		return approx;
	}
	
	/**
	 * Get rect, noob
	 */
	public static List<RotatedRect> getRotatedRectangles(List<MatOfPoint> contours, List<MatOfPoint> toKeep) {
		List<RotatedRect> rectangles = new ArrayList<>();
		System.out.println("Number of contours: " + contours.size());
		for (MatOfPoint contour : contours) {
			
			MatOfPoint2f approx = approximate(contour);

			int points = approx.toList().size();

			if (points == 4) {
				RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour. toArray()));
				
				if (Math.abs(rotatedRect.angle + 15) < ANGLE_TOLERANCE || Math.abs(rotatedRect.angle + 75) < ANGLE_TOLERANCE) {
					System.out.println("Area: " + rotatedRect.size.width * rotatedRect.size.height 
								   + "; Angle: " + rotatedRect.angle 
								   + "; Points: " + points
								   + "; Center: (" + rotatedRect.center.x + ", " + rotatedRect.center.y + ")");
					rectangles.add(rotatedRect);
					toKeep.add(contour);
				}
				
			}
		}
		return rectangles; 
	}
	
}
