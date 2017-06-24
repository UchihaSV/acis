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
package net.sf.l2j.gameserver.model;

public class SpawnLocation extends Location
{
	public static final SpawnLocation DUMMY_SPAWNLOC = new SpawnLocation(0, 0, 0, 0);
	
	protected volatile int _heading;
	
	public SpawnLocation(int x, int y, int z, int heading)
	{
		super(x, y, z);
		
		_heading = heading;
	}
	
	public SpawnLocation(SpawnLocation loc)
	{
		super(loc.getX(), loc.getY(), loc.getZ());
		
		_heading = loc.getHeading();
	}
	
	@Override
	public String toString()
	{
		return _x + ", " + _y + ", " + _z + ", " + _heading;
	}
	
	@Override
	public int hashCode()
	{
		return _x ^ _y ^ _z ^ _heading;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof SpawnLocation)
		{
			SpawnLocation loc = (SpawnLocation) o;
			return (loc.getX() == _x && loc.getY() == _y && loc.getZ() == _z && loc.getHeading() == _heading);
		}
		
		return false;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public void set(int x, int y, int z, int heading)
	{
		super.set(x, y, z);
		
		_heading = heading;
	}
	
	public void set(SpawnLocation loc)
	{
		super.set(loc.getX(), loc.getY(), loc.getZ());
		
		_heading = loc.getHeading();
	}
	
	@Override
	public void clean()
	{
		super.set(0, 0, 0);
		
		_heading = 0;
	}
}