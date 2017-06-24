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

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;

public class L2TownPetInstance extends L2NpcInstance
{
	private ScheduledFuture<?> _aiTask;
	
	public L2TownPetInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setRunning();
		
		_aiTask = ThreadPool.scheduleAtFixedRate(new RandomWalkTask(), 1000, 10000);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		// Set the target of the L2PcInstance player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (!canInteract(player))
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		super.deleteMe();
	}
	
	public class RandomWalkTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!hasAI() || getSpawn() == null)
				return;
			
			getAI().setIntention(CtrlIntention.MOVE_TO, GeoEngine.getInstance().canMoveToTargetLoc(getX(), getY(), getZ(), getSpawn().getLocX() + Rnd.get(-75, 75), getSpawn().getLocY() + Rnd.get(-75, 75), getZ()));
		}
	}
}