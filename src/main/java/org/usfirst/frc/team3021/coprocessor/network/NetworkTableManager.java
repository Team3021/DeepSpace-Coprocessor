package org.usfirst.frc.team3021.coprocessor.network;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public final class NetworkTableManager {
	private static NetworkTableManager manager;
	private NetworkTable table;

	private NetworkTableManager() {
		NetworkTableInstance instance = NetworkTableInstance.getDefault();
		instance.startClientTeam(3021);
		table = instance.getTable("vision");
	}
	
	public static NetworkTableManager getInstance() {
		if (manager == null)
			manager = new NetworkTableManager();
		return manager;
	}
	
	public void write(String key, String value) {
		NetworkTableEntry entry = table.getEntry(key);
		entry.setString(value);
	}
	
}
