package org.usfirst.frc.team3021.coprocessor.processing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.RotatedRect;
import org.usfirst.frc.team3021.coprocessor.target.HatchTarget;

public class Targeting {

	public static final double 
	/** The amount by which the angle of any box may be different from the expected value */
	ANGLE_TOLERANCE = 1.0,
	/** The ratio by which the areas of the boxes may be different. 
	 *  Take careful note that this is in fact a ratio! */
	AREA_TOLERANCE = 1.25,
	/** The fraction of the area of a box by which the heights of the boxes may be off */
	VERTICAL_TOLERANCE = 0.001;
	
	// Define constructor to make private
	private Targeting() {
		
	}
	
	
	
	public static HatchTarget getTarget(List<RotatedRect> rectangles) {
		int size = rectangles.size();
		
		// Copy and sort the list
		rectangles = new ArrayList<>(rectangles);
		rectangles.sort(new sortByXCoordinate());
		
		/* Check all unique combinations to find the correct target. 
		 * Exclude the rightmost rectangle from being the left rect,
		 * as by definition there is nothing to the right of it */
		for (int leftIndex = 0; leftIndex < size - 1; leftIndex++) {
			RotatedRect leftRect = rectangles.get(leftIndex);
			if (Math.abs(leftRect.angle + 75) > ANGLE_TOLERANCE)
				continue;
			
			/* Iterate in the opposite direction, excluding rectangles that are at 
			 * a smaller x coordinate than the current left rectangle */
			for (int rightIndex = size - 1; rightIndex > leftIndex; rightIndex--) {
				RotatedRect rightRect = rectangles.get(rightIndex);
				
				if (Math.abs(rightRect.angle + 15) > ANGLE_TOLERANCE)
					continue;
				
				double areaLeft = leftRect.size.area();
				double areaRight = rightRect.size.area();
				
				if (areaLeft > areaRight && areaLeft / areaRight > AREA_TOLERANCE)
					continue;
				if (areaLeft > areaRight && areaRight / areaLeft > AREA_TOLERANCE)
					continue;
				
				// Vertical tolerance is proportional to the size of the boxes
				double verticalTolerance = 0;
				if (areaLeft >= areaRight)
					verticalTolerance = areaLeft * VERTICAL_TOLERANCE;
				else
					verticalTolerance = areaRight * VERTICAL_TOLERANCE;
				
				if (Math.abs(leftRect.center.y - rightRect.center.y) > verticalTolerance)
					continue;
				
				return new HatchTarget(leftRect, rightRect);
			}
		}
		return null;
	}
	
	private static class sortByXCoordinate implements Comparator<RotatedRect> {

		public int compare(RotatedRect rect1, RotatedRect rect2) {
				if (rect1.center.x < rect2.center.x)
					return -1;
				else if (rect1.center.x > rect2.center.x)
					return 1;
				else
					return 0;
		}

	}
	
}
