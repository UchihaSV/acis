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

import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

/**
 * Mother class of all interactive objects in the world (PC, NPC, Item...)
 */
public abstract class L2Object
{
	public enum PolyType
	{
		ITEM,
		NPC,
		DEFAULT;
	}
	
	public static final Logger _log = Logger.getLogger(L2Object.class.getName());
	
	private String _name;
	private int _objectId;
	
	private NpcTemplate _polyTemplate;
	private PolyType _polyType = PolyType.DEFAULT;
	private int _polyId;
	
	private SpawnLocation _position = new SpawnLocation(0, 0, 0, 0);
	private L2WorldRegion _region;
	
	private boolean _isVisible;
	
	public L2Object(int objectId)
	{
		_objectId = objectId;
		
		setRegion(L2World.getInstance().getRegion(_position));
	}
	
	public void onAction(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onActionShift(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onForcedAttack(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onSpawn()
	{
	}
	
	/**
	 * Remove a L2Object from the world.
	 */
	public void decayMe()
	{
		assert _region != null;
		
		final L2WorldRegion region = _region;
		
		synchronized (this)
		{
			_isVisible = false;
			setRegion(null);
		}
		
		// Out of synchronized to avoid deadlocks
		L2World.getInstance().removeVisibleObject(this, region);
		L2World.getInstance().removeObject(this);
	}
	
	public void refreshID()
	{
		L2World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}
	
	/**
	 * Init the position of a L2Object spawn and add it in the world as a visible object.
	 */
	public final void spawnMe()
	{
		assert _region == null;
		
		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;
			setRegion(L2World.getInstance().getRegion(_position));
		}
		
		// Add the L2Object spawn in the _allobjects of L2World
		L2World.getInstance().addObject(this);
		
		// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
		_region.addVisibleObject(this);
		
		// Add the L2Object spawn in the world as a visible object -- out of synchronized to avoid deadlocks
		L2World.getInstance().addVisibleObject(this, _region);
		
		onSpawn();
	}
	
	public final void spawnMe(int x, int y, int z)
	{
		assert _region == null;
		
		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;
			
			if (x > L2World.WORLD_X_MAX)
				x = L2World.WORLD_X_MAX - 5000;
			if (x < L2World.WORLD_X_MIN)
				x = L2World.WORLD_X_MIN + 5000;
			if (y > L2World.WORLD_Y_MAX)
				y = L2World.WORLD_Y_MAX - 5000;
			if (y < L2World.WORLD_Y_MIN)
				y = L2World.WORLD_Y_MIN + 5000;
			
			_position.set(x, y, z);
			setRegion(L2World.getInstance().getRegion(_position));
		}
		
		// Add the L2Object spawn in the _allobjects of L2World
		L2World.getInstance().addObject(this);
		
		// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
		_region.addVisibleObject(this);
		
		// Add the L2Object spawn in the world as a visible object
		L2World.getInstance().addVisibleObject(this, _region);
		
		onSpawn();
	}
	
	public void toggleVisible()
	{
		if (isVisible())
			decayMe();
		else
			spawnMe();
	}
	
	public boolean isAttackable()
	{
		return false;
	}
	
	/**
	 * @param attacker The target to make checks on.
	 * @return true or false, depending if the target is attackable or not.
	 */
	public abstract boolean isAutoAttackable(L2Character attacker);
	
	/**
	 * A L2Object is visible if <B>_isVisible</B> = true and <B>_worldregion</B> != null.
	 * @return the visibilty state of the L2Object.
	 */
	public final boolean isVisible()
	{
		return _region != null && _isVisible;
	}
	
	public final void setIsVisible(boolean value)
	{
		_isVisible = value;
		
		if (!_isVisible)
			setRegion(null);
	}
	
	public ObjectKnownList getKnownList()
	{
		return null;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	public final int getObjectId()
	{
		return _objectId;
	}
	
	public final NpcTemplate getPolyTemplate()
	{
		return _polyTemplate;
	}
	
	public final PolyType getPolyType()
	{
		return _polyType;
	}
	
	public final int getPolyId()
	{
		return _polyId;
	}
	
	public boolean polymorph(PolyType type, int id)
	{
		if (!(this instanceof L2Npc) && !(this instanceof L2PcInstance))
			return false;
		
		if (type == PolyType.NPC)
		{
			final NpcTemplate template = NpcTable.getInstance().getTemplate(id);
			if (template == null)
				return false;
			
			_polyTemplate = template;
		}
		else if (type == PolyType.ITEM)
		{
			if (ItemTable.getInstance().getTemplate(id) == null)
				return false;
		}
		else if (type == PolyType.DEFAULT)
			return false;
		
		_polyType = type;
		_polyId = id;
		
		decayMe();
		spawnMe();
		
		return true;
	}
	
	public void unpolymorph()
	{
		_polyTemplate = null;
		_polyType = PolyType.DEFAULT;
		_polyId = 0;
		
		decayMe();
		spawnMe();
	}
	
	public L2PcInstance getActingPlayer()
	{
		return null;
	}
	
	/**
	 * Sends the Server->Client info packet for the object. Is Overridden in: <li>L2BoatInstance</li> <li>L2DoorInstance</li> <li>L2PcInstance</li> <li>L2StaticObjectInstance</li> <li>L2Npc</li> <li>L2Summon</li> <li>ItemInstance</li>
	 * @param activeChar
	 */
	public void sendInfo(L2PcInstance activeChar)
	{
		
	}
	
	/**
	 * Check if current object has charged shot.
	 * @param type of the shot to be checked.
	 * @return true if the object has charged shot.
	 */
	public boolean isChargedShot(ShotType type)
	{
		return false;
	}
	
	/**
	 * Charging shot into the current object.
	 * @param type Type of the shot to be (un)charged.
	 * @param charged True if we charge, false if we uncharge.
	 */
	public void setChargedShot(ShotType type, boolean charged)
	{
	}
	
	/**
	 * Try to recharge a shot.
	 * @param physical skill are using Soul shots.
	 * @param magical skill are using Spirit shots.
	 */
	public void rechargeShots(boolean physical, boolean magical)
	{
	}
	
	@Override
	public String toString()
	{
		return (getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]");
	}
	
	/**
	 * Check if the object is in the given zone Id.
	 * @param zone the zone Id to check
	 * @return {@code true} if the object is in that zone Id
	 */
	public boolean isInsideZone(ZoneId zone)
	{
		return false;
	}
	
	/**
	 * Set the x,y,z position of the L2Object and if necessary modify its _worldRegion.
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void setXYZ(int x, int y, int z)
	{
		assert _region != null;
		
		_position.set(x, y, z);
		
		try
		{
			if (!isVisible())
				return;
			
			final L2WorldRegion region = L2World.getInstance().getRegion(_position);
			if (region != _region)
			{
				_region.removeVisibleObject(this);
				
				setRegion(region);
				
				// Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
				_region.addVisibleObject(this);
			}
		}
		catch (Exception e)
		{
			_log.warning("Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
			badCoords();
		}
	}
	
	/**
	 * Called on setXYZ exception.
	 */
	protected void badCoords()
	{
	}
	
	/**
	 * Set the x,y,z position of the L2Object and make it invisible. A L2Object is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void setXYZInvisible(int x, int y, int z)
	{
		assert _region == null;
		
		if (x > L2World.WORLD_X_MAX)
			x = L2World.WORLD_X_MAX - 5000;
		if (x < L2World.WORLD_X_MIN)
			x = L2World.WORLD_X_MIN + 5000;
		if (y > L2World.WORLD_Y_MAX)
			y = L2World.WORLD_Y_MAX - 5000;
		if (y < L2World.WORLD_Y_MIN)
			y = L2World.WORLD_Y_MIN + 5000;
		
		_position.set(x, y, z);
		setIsVisible(false);
	}
	
	/**
	 * @return the x position of the L2Object.
	 */
	public final int getX()
	{
		assert _region != null || _isVisible;
		
		return _position.getX();
	}
	
	/**
	 * @return the y position of the L2Object.
	 */
	public final int getY()
	{
		assert _region != null || _isVisible;
		
		return _position.getY();
	}
	
	/**
	 * @return the z position of the L2Object.
	 */
	public final int getZ()
	{
		assert _region != null || _isVisible;
		
		return _position.getZ();
	}
	
	public final SpawnLocation getPosition()
	{
		return _position;
	}
	
	public final L2WorldRegion getRegion()
	{
		return _region;
	}
	
	public void setRegion(L2WorldRegion value)
	{
		_region = value;
	}
}