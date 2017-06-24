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
package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.Location;

/**
 * @author Hasha
 */
public class Circle extends AShape
{
	// circle center coordinates
	protected final int _x;
	protected final int _y;
	
	// circle radius
	protected final int _r;
	
	/**
	 * Circle constructor
	 * @param x : Center X coordinate.
	 * @param y : Center Y coordinate.
	 * @param r : Circle radius.
	 */
	public Circle(int x, int y, int r)
	{
		_x = x;
		_y = y;
		
		_r = r;
	}
	
	@Override
	public final int getSize()
	{
		return (int) Math.PI * _r * _r;
	}
	
	@Override
	public double getArea()
	{
		return (int) Math.PI * _r * _r;
	}
	
	@Override
	public double getVolume()
	{
		return 0;
	}
	
	@Override
	public final boolean isInside(int x, int y)
	{
		final int dx = x - _x;
		final int dy = y - _y;
		
		return (dx * dx + dy * dy) <= _r * _r;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		final int dx = x - _x;
		final int dy = y - _y;
		
		return (dx * dx + dy * dy) <= _r * _r;
	}
	
	@Override
	public Location getRandomLocation()
	{
		// get uniform distance and angle
		final double distance = Math.sqrt(Rnd.nextDouble()) * _r;
		final double angle = Rnd.nextDouble() * Math.PI * 2;
		
		// calculate coordinates and return
		return new Location((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)), 0);
	}
}