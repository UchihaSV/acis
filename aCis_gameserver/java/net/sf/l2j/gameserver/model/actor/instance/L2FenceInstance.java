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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.geoengine.geodata.IGeoObject;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.network.serverpackets.ExColosseumFenceInfo;

/**
 * @author Hasha
 */
public class L2FenceInstance extends L2Object implements IGeoObject
{
	private static final int FENCE_HEIGHT = 24;
	
	// fence description from world point of view
	private final int _type;
	private final int _sizeX;
	private final int _sizeY;
	private final int _height;
	
	// 2 dummy object to spawn fences with 2 and 3 layers easily
	// TODO: I know it is shitcoded, but didn't figure out any better solution
	private final L2DummyFence _object2;
	private final L2DummyFence _object3;
	
	// fence description from geodata point of view
	private final int _geoX;
	private final int _geoY;
	private final int _geoZ;
	private final byte[][] _geoData;
	
	public L2FenceInstance(int type, int sizeZ, int sizeY, int height, int geoX, int geoY, int geoZ, byte[][] geoData)
	{
		super(IdFactory.getInstance().getNextId());
		
		_type = type;
		_sizeX = sizeZ;
		_sizeY = sizeY;
		_height = height * FENCE_HEIGHT;
		
		_object2 = height > 1 ? new L2DummyFence(this) : null;
		_object3 = height > 2 ? new L2DummyFence(this) : null;
		
		_geoX = geoX;
		_geoY = geoY;
		_geoZ = geoZ;
		_geoData = geoData;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getSizeX()
	{
		return _sizeX;
	}
	
	public int getSizeY()
	{
		return _sizeY;
	}
	
	@Override
	public void onSpawn()
	{
		// spawn me
		super.onSpawn();
		
		// spawn dummy fences
		if (_object2 != null)
			_object2.spawnMe(getX(), getY(), getZ());
		if (_object3 != null)
			_object3.spawnMe(getX(), getY(), getZ());
	}
	
	@Override
	public void decayMe()
	{
		// despawn dummy fences
		if (_object2 != null)
			_object2.decayMe();
		if (_object3 != null)
			_object3.decayMe();
		
		// despawn me
		super.decayMe();
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExColosseumFenceInfo(getObjectId(), this));
	}
	
	@Override
	public int getGeoX()
	{
		return _geoX;
	}
	
	@Override
	public int getGeoY()
	{
		return _geoY;
	}
	
	@Override
	public int getGeoZ()
	{
		return _geoZ;
	}
	
	@Override
	public int getHeight()
	{
		return _height;
	}
	
	@Override
	public byte[][] getObjectGeoData()
	{
		return _geoData;
	}
	
	/**
	 * Dummy fence class in order to spawn/delete multi-layer fences correctly.
	 * @author Hasha
	 */
	protected class L2DummyFence extends L2Object
	{
		private final L2FenceInstance _fence;
		
		public L2DummyFence(L2FenceInstance fence)
		{
			super(IdFactory.getInstance().getNextId());
			
			_fence = fence;
		}
		
		@Override
		public boolean isAutoAttackable(L2Character attacker)
		{
			return false;
		}
		
		@Override
		public void sendInfo(L2PcInstance activeChar)
		{
			activeChar.sendPacket(new ExColosseumFenceInfo(getObjectId(), _fence));
		}
	}
}