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

import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.knownlist.GuardKnownList;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.Quest;

/**
 * This class manages all Guards in the world.<br>
 * It inherits all methods from L2Attackable and adds some more such as:
 * <ul>
 * <li>tracking PK</li>
 * <li>aggressive L2MonsterInstance.</li>
 * </ul>
 */
public final class L2GuardInstance extends L2Attackable
{
	public L2GuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new GuardKnownList(this));
	}
	
	@Override
	public final GuardKnownList getKnownList()
	{
		return (GuardKnownList) super.getKnownList();
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/guard/" + filename + ".htm";
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		// Set the target of the L2PcInstance player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Calculate the distance between the L2PcInstance and the L2Npc
			if (!canInteract(player))
			{
				// Set the L2PcInstance Intention to INTERACT
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				// Some guards have no HTMs on retail. Bypass the chat window if such guard is met.
				switch (getNpcId())
				{
					case 31671:
					case 31672:
					case 31673:
					case 31674:
						// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
				}
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
				
				if (hasRandomAnimation())
					onRandomAnimation(Rnd.get(8));
				
				List<Quest> qlsa = getTemplate().getEventQuests(EventType.QUEST_START);
				if (qlsa != null && !qlsa.isEmpty())
					player.setLastQuestNpcObject(getObjectId());
				
				List<Quest> qlst = getTemplate().getEventQuests(EventType.ON_FIRST_TALK);
				if (qlst != null && qlst.size() == 1)
					qlst.get(0).notifyFirstTalk(this, player);
				else
					showChatWindow(player);
			}
		}
	}
	
	@Override
	public boolean isGuard()
	{
		return true;
	}
	
	@Override
	public boolean returnHome()
	{
		getAggroList().clear();
		
		if (getMoveSpeed() > 0 && hasAI() && getSpawn() != null && !isInsideRadius(getSpawn().getLocX(), getSpawn().getLocY(), 20, false))
		{
			setIsReturningToSpawnPoint(true);
			setWalking();
			getAI().setIntention(CtrlIntention.MOVE_TO, getSpawn().getLoc());
			return true;
		}
		return false;
	}
}