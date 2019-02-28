package org.usfirst.frc.team3021.coprocessor.network;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public final class NetworkTableManager {
	private static boolean isInitialized, clientInitialized;
	
	private static NetworkTable table;

	private NetworkTableManager() {
		
	}
	
	private static void init() {
		isInitialized = true;
		
		NetworkTableInstance instance = NetworkTableInstance.getDefault();
		instance.startClientTeam(3021);
		table = instance.getTable("vision");
	}
	
	public static void checkInit() {
		if (!isInitialized)
			init();
	}
	
	public static void write(String key, String value) {
		checkInit();
		NetworkTableEntry entry = table.getEntry(key);
		entry.setString(value);
	}
	
	public static void write(String key, Number value) {
		checkInit();
		NetworkTableEntry entry = table.getEntry(key);
		entry.setNumber(value);
		
	}
	
	public static void write(String key, boolean value) {
		checkInit();
		NetworkTableEntry entry = table.getEntry(key);
		entry.setBoolean(value);
		
	}
	
	public static NetworkTableEntry getEntry(String key) {
		checkInit();
		return table.getEntry(key);
	}
	
	public static Boolean readBoolean(String key, boolean defaultValue) {
		checkInit();
		NetworkTableEntry entry = table.getEntry(key);
		return entry.getBoolean(defaultValue);
	}
	
	public static boolean[] readBooleanArray(String key, boolean[] defaultValue) {
		checkInit();
		NetworkTableEntry entry = table.getEntry(key);
		return entry.getBooleanArray(defaultValue);
	}

	public static Number readNumber(String key, Number defaultValue) {
		checkInit();
		NetworkTableEntry entry = table.getEntry(key);
		return entry.getNumber(defaultValue);
	}
	
}
