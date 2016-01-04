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
import net.sf.l2j.gameserver.templates.item.L2Henna;

public class HennaEquipList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final L2Henna[] _hennaEquipList;
	
	public HennaEquipList(L2PcInstance player, L2Henna[] hennaEquipList)
	{
		_player = player;
		_hennaEquipList = hennaEquipList;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe2);
		writeD(_player.getAdena());
		writeD(3);
		writeD(_hennaEquipList.length);
		
		for (L2Henna temp : _hennaEquipList)
		{
			// Player must have at least one dye in inventory to be able to see the henna that can be applied with it.
			if ((_player.getInventory().getItemByItemId(temp.getDyeId())) != null)
			{
				writeD(temp.getSymbolId()); // symbolid
				writeD(temp.getDyeId()); // itemid of dye
				writeD(L2Henna.getAmountDyeRequire()); // amount of dyes required
				writeD(temp.getPrice()); // amount of adenas required
				writeD(1); // meet the requirement or not
			}
		}
	}
}