/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.util;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * General Utility functions related to Gameserver
 */
public final class Util
{
	public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		ThreadPool.schedule(new IllegalPlayerAction(actor, message, punishment), 5000);
	}
	
	/**
	 * @param obj1
	 * @param obj2
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin
	 */
	public static double calculateAngleFrom(L2Object obj1, L2Object obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	/**
	 * @param obj1X
	 * @param obj1Y
	 * @param obj2X
	 * @param obj2Y
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin
	 */
	public static final double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
			angleTarget = 360 + angleTarget;
		
		return angleTarget;
	}
	
	public static final double convertHeadingToDegree(int clientHeading)
	{
		return clientHeading / 182.044444444;
	}
	
	public static final int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
			degree = 360 + degree;
		
		return (int) (degree * 182.044444444);
	}
	
	public static final int calculateHeadingFrom(L2Object obj1, L2Object obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static final int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
			angleTarget = 360 + angleTarget;
		
		return (int) (angleTarget * 182.044444444);
	}
	
	public static final int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0)
			angleTarget = 360 + angleTarget;
		
		return (int) (angleTarget * 182.044444444);
	}
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;
		
		if (includeZAxis)
		{
			double dz = z1 - z2;
			return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return 1000000;
		
		return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
	}
	
	/**
	 * Faster calculation than checkIfInRange if distance is short and collisionRadius isn't needed. Not for long distance checks (potential teleports, far away castles, etc)
	 * @param radius The radius to use as check.
	 * @param obj1 The position 1 to make check on.
	 * @param obj2 The postion 2 to make check on.
	 * @param includeZAxis Include Z check or not.
	 * @return true if both objects are in the given radius.
	 */
	public static boolean checkIfInShortRadius(int radius, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return false;
		
		if (radius == -1)
			return true; // not limited
			
		int dx = obj1.getX() - obj2.getX();
		int dy = obj1.getY() - obj2.getY();
		
		if (includeZAxis)
		{
			int dz = obj1.getZ() - obj2.getZ();
			return dx * dx + dy * dy + dz * dz <= radius * radius;
		}
		
		return dx * dx + dy * dy <= radius * radius;
	}
	
	/**
	 * This check includes collision radius of both characters.<br>
	 * Used for accurate checks (skill casts, knownlist, etc).
	 * @param range The range to use as check.
	 * @param obj1 The position 1 to make check on.
	 * @param obj2 The postion 2 to make check on.
	 * @param includeZAxis Include Z check or not.
	 * @return true if both objects are in the given radius.
	 */
	public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return false;
		
		if (range == -1)
			return true; // not limited
			
		double rad = 0;
		if (obj1 instanceof L2Character)
			rad += ((L2Character) obj1).getCollisionRadius();
		
		if (obj2 instanceof L2Character)
			rad += ((L2Character) obj2).getCollisionRadius();
		
		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();
		
		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			double d = dx * dx + dy * dy + dz * dz;
			
			return d <= range * range + 2 * range * rad + rad * rad;
		}
		
		double d = dx * dx + dy * dy;
		return d <= range * range + 2 * range * rad + rad * rad;
	}
	
	/**
	 * Returns the rounded value of val to specified number of digits after the decimal point.<BR>
	 * (Based on round() in PHP)
	 * @param val
	 * @param numPlaces
	 * @return float roundedVal
	 */
	public static float roundTo(float val, int numPlaces)
	{
		if (numPlaces <= 1)
			return Math.round(val);
		
		float exponent = (float) Math.pow(10, numPlaces);
		
		return (Math.round(val * exponent) / exponent);
	}
}