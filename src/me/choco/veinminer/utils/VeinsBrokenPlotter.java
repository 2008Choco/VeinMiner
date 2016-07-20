package me.choco.veinminer.utils;

import me.choco.veinminer.utils.Metrics.Plotter;

/** This plotter is used to determine the amount of veins that have been broken using VeinMiner
 * within the period of a half hour (Or whatever Metric's TICK_DELAY is set to)
 * <br>
 * <br>This is a basic statistic that I thought may be interesting to see. How many people actually
 * utilise the VeinMiner tool, and how many people just leave it sitting on their server
 */
public class VeinsBrokenPlotter extends Plotter {
	
	/** The amount of veins broken within this hour of Metrics */
	public static int veinsBroken;
	
	@Override
	public int getValue() {
		int toReturn = veinsBroken;
		veinsBroken = 0;
		return toReturn;
	}
}