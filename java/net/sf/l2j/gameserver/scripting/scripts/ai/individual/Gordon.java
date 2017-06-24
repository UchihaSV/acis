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
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

/**
 * Gordon behavior. This boss attacks cursed weapons holders at sight.<br>
 * When he isn't attacking, he follows a pre-established path around Goddard castle.
 */
public class Gordon extends L2AttackableAIScript
{
	private static final int GORDON = 29095;
	
	private static final Location[] LOCS =
	{
		new Location(141569, -45908, -2387),
		new Location(142494, -45456, -2397),
		new Location(142922, -44561, -2395),
		new Location(143672, -44130, -2398),
		new Location(144557, -43378, -2325),
		new Location(145839, -43267, -2301),
		new Location(147044, -43601, -2307),
		new Location(148140, -43206, -2303),
		new Location(148815, -43434, -2328),
		new Location(149862, -44151, -2558),
		new Location(151037, -44197, -2708),
		new Location(152555, -42756, -2836),
		new Location(154808, -39546, -3236),
		new Location(155333, -39962, -3272),
		new Location(156531, -41240, -3470),
		new Location(156863, -43232, -3707),
		new Location(156783, -44198, -3764),
		new Location(158169, -45163, -3541),
		new Location(158952, -45479, -3473),
		new Location(160039, -46514, -3634),
		new Location(160244, -47429, -3656),
		new Location(159155, -48109, -3665),
		new Location(159558, -51027, -3523),
		new Location(159396, -53362, -3244),
		new Location(160872, -56556, -2789),
		new Location(160857, -59072, -2613),
		new Location(160410, -59888, -2647),
		new Location(158770, -60173, -2673),
		new Location(156368, -59557, -2638),
		new Location(155188, -59868, -2642),
		new Location(154118, -60591, -2731),
		new Location(153571, -61567, -2821),
		new Location(153457, -62819, -2886),
		new Location(152939, -63778, -3003),
		new Location(151816, -64209, -3120),
		new Location(147655, -64826, -3433),
		new Location(145422, -64576, -3369),
		new Location(144097, -64320, -3404),
		new Location(140780, -61618, -3096),
		new Location(139688, -61450, -3062),
		new Location(138267, -61743, -3056),
		new Location(138613, -58491, -3465),
		new Location(138139, -57252, -3517),
		new Location(139555, -56044, -3310),
		new Location(139107, -54537, -3240),
		new Location(139279, -53781, -3091),
		new Location(139810, -52687, -2866),
		new Location(139657, -52041, -2793),
		new Location(139215, -51355, -2698),
		new Location(139334, -50514, -2594),
		new Location(139817, -49715, -2449),
		new Location(139824, -48976, -2263),
		new Location(140130, -47578, -2213),
		new Location(140483, -46339, -2382),
		new Location(141569, -45908, -2387)
	};
	
	// The current Location node index.
	private static int _currentNode;
	
	public Gordon()
	{
		super("ai/individual");
		
		final L2Npc npc = findSpawn(GORDON);
		if (npc != null)
			startQuestTimer("ai_loop", 1000, npc, null, true);
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(GORDON, EventType.ON_KILL, EventType.ON_SPAWN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("ai_loop"))
		{
			// Doesn't bother about task AI if the NPC is already fighting.
			if (npc.getAI().getIntention() == CtrlIntention.ATTACK || npc.getAI().getIntention() == CtrlIntention.CAST)
				return null;
			
			// Check if player have Cursed Weapon and is in radius.
			for (L2PcInstance pc : npc.getKnownList().getKnownTypeInRadius(L2PcInstance.class, 5000))
			{
				if (pc.isCursedWeaponEquipped())
				{
					attack(((L2Attackable) npc), pc);
					return null;
				}
			}
			
			// Test the NPC position and move on new position if current position is reached.
			final Location currentNode = LOCS[_currentNode];
			if (npc.isInsideRadius(currentNode.getX(), currentNode.getY(), 100, false))
			{
				// Update current node ; if the whole route is done, come back to point 0.
				_currentNode++;
				if (_currentNode >= LOCS.length)
					_currentNode = 0;
				
				npc.setWalking();
				npc.getAI().setIntention(CtrlIntention.MOVE_TO, LOCS[_currentNode]);
			}
			else if (!npc.isMoving())
			{
				npc.setWalking();
				npc.getAI().setIntention(CtrlIntention.MOVE_TO, LOCS[_currentNode]);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		// Initialize current node.
		_currentNode = 0;
		
		// Launch the AI loop.
		startQuestTimer("ai_loop", 1000, npc, null, true);
		
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		cancelQuestTimer("ai_loop", npc, null);
		
		return super.onKill(npc, killer, isPet);
	}
	
	private static L2Npc findSpawn(int npcId)
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn.getNpcId() == npcId)
				return spawn.getNpc();
		}
		return null;
	}
}