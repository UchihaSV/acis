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

/**
 *
 * @author FBIagent
 *
 */
package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.logging.Level;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.L2Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2XmassTreeInstance;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.SummonItem;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(L2Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		
		if (activeChar.inObserverMode())
			return;
		
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
			return;
		
		final SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		
		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			return;
		}
		
		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		
		final int npcId = sitem.getNpcId();
		if (npcId == 0)
			return;
		
		final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
			return;
		
		activeChar.stopMove(null);
		
		switch (sitem.getType())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					for (L2XmassTreeInstance ch : activeChar.getKnownList().getKnownTypeInRadius(L2XmassTreeInstance.class, 1200))
					{
						if (npcTemplate.getNpcId() == L2XmassTreeInstance.SPECIAL_TREE_ID)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN).addCharName(ch));
							return;
						}
					}
					
					if (activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false))
					{
						final L2Spawn spawn = new L2Spawn(npcTemplate);
						spawn.setLoc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading());
						spawn.setRespawnState(false);
						
						final L2Npc npc = spawn.doSpawn(true);
						npc.setTitle(activeChar.getName());
						npc.setIsRunning(false); // broadcast info
					}
				}
				catch (Exception e)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
				}
				break;
			case 1: // pet summons
				final L2Object oldTarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0));
				activeChar.setTarget(oldTarget);
				activeChar.sendPacket(new SetupGauge(0, 5000));
				activeChar.sendPacket(SystemMessageId.SUMMON_A_PET);
				activeChar.setIsCastingNow(true);
				
				ThreadPool.schedule(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
				break;
		}
	}
	
	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final ItemInstance _item;
		private final NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(L2PcInstance activeChar, NpcTemplate npcTemplate, ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_activeChar.setIsCastingNow(false);
				
				// check for summon item validity
				if (_item == null || _item.getOwnerId() != _activeChar.getObjectId() || _item.getLocation() != ItemInstance.ItemLocation.INVENTORY)
					return;
				
				final L2PetInstance pet = L2PetInstance.spawnPet(_npcTemplate, _activeChar, _item);
				if (pet == null)
					return;
				
				pet.setShowSummonAnimation(true);
				
				if (!pet.isRespawned())
				{
					pet.setCurrentHp(pet.getMaxHp());
					pet.setCurrentMp(pet.getMaxMp());
					pet.getStat().setExp(pet.getExpForThisLevel());
					pet.setCurrentFed(pet.getPetData().getMaxMeal());
				}
				
				pet.setRunning();
				
				if (!pet.isRespawned())
					pet.store();
				
				_activeChar.setPet(pet);
				
				pet.spawnMe(_activeChar.getX() + 50, _activeChar.getY() + 100, _activeChar.getZ());
				pet.startFeed();
				_item.setEnchantLevel(pet.getLevel());
				
				pet.setFollowStatus(true);
				
				pet.getOwner().sendPacket(new PetItemList(pet));
				pet.broadcastStatusUpdate();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
}
