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
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private final int _objectId;
	private final int _boatId;
	private final int _targetX;
	private final int _targetY;
	private final int _targetZ;
	private final int _originX;
	private final int _originY;
	private final int _originZ;
	
	public MoveToLocationInVehicle(L2PcInstance player, int targetX, int targetY, int targetZ, int originX, int originY, int originZ)
	{
		_objectId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_targetX = targetX;
		_targetY = targetY;
		_targetZ = targetZ;
		_originX = originX;
		_originY = originY;
		_originZ = originZ;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x71);
		writeD(_objectId);
		writeD(_boatId);
		writeD(_targetX);
		writeD(_targetY);
		writeD(_targetZ);
		writeD(_originX);
		writeD(_originY);
		writeD(_originZ);
	}
}