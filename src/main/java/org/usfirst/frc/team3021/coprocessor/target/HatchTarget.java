package org.usfirst.frc.team3021.coprocessor.target;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

public class HatchTarget implements Target {
	protected RotatedRect leftRect, rightRect;
	
	public HatchTarget(RotatedRect leftRect, RotatedRect rightRect) {
		this.leftRect = leftRect;
		this.rightRect = rightRect;
	}

	/*
	 * INTERFACE
	 */
	
	@Override
	public Point getCenter() {
		double x = (leftRect.center.x + rightRect.center.x) / 2;
		double y = (leftRect.center.y + rightRect.center.y) / 2;
		return new Point(x, y);
	}

	@Override
	public double getDepth() {
		return 0;
	}

	@Override
	public double getLateralDistance() {
		return 0;
	}

	@Override
	public double getHeight() {
		return 0;
	}
	
	@Override
	public double getAngle() {
		return 0;
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	public List<RotatedRect> getRotatedRects() {
		List<RotatedRect> rects = new ArrayList<>();
		rects.add(leftRect);
		rects.add(rightRect);
		return rects;
	}
	
	/*
	 * DEFAULT METHODS
	 */
	
	public boolean equals(Object obj) {
		if (obj instanceof HatchTarget) {
			return true;
		}
		return false;
	}
	
}
