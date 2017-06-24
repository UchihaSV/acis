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
package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

/**
 * Those monsters don't attack at sight players owning itemId 8064, 8065 or 8067.
 */
public class GatekeeperZombies extends L2AttackableAIScript
{
	public GatekeeperZombies()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAggroRangeEnterId(22136);
	}
	
	@Override
	public String onAggro(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (player.getInventory().hasAtLeastOneItem(8064, 8065, 8067))
			return null;
		
		return super.onAggro(npc, player, isPet);
	}
}