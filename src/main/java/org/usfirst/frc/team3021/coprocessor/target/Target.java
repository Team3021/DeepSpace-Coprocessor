package org.usfirst.frc.team3021.coprocessor.target;

import org.opencv.core.Point;

public interface Target {
	
	/** @return the center of the target in screen space. */
	public abstract Point getCenter();
	
	/** @return the distance of the target in front of the camera in real space. */
	public abstract double getDepth();
	
	/** @return the distance of the target to the side of the camera in real space. */
	public abstract double getLateralDistance();
	
	/** @return the height of the target from the camera in real space. */
	public abstract double getHeight();
	
	public abstract double getAngle();
	
}
