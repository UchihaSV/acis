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
package net.sf.l2j.gameserver.scripting.tasks;

import java.util.Map;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

/**
 * @author Hasha
 */
public final class RaidPointsReset extends ScheduledQuest
{
	public RaidPointsReset()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		// reward clan reputation points
		Map<Integer, Integer> rankList = RaidBossPointsManager.getInstance().getRankList();
		for (L2Clan c : ClanTable.getInstance().getClans())
		{
			for (Map.Entry<Integer, Integer> entry : rankList.entrySet())
			{
				if (entry.getValue() <= 100 && c.isMember(entry.getKey()))
				{
					int reputation = 0;
					switch (entry.getValue())
					{
						case 1:
							reputation = 1250;
							break;
						case 2:
							reputation = 900;
							break;
						case 3:
							reputation = 700;
							break;
						case 4:
							reputation = 600;
							break;
						case 5:
							reputation = 450;
							break;
						case 6:
							reputation = 350;
							break;
						case 7:
							reputation = 300;
							break;
						case 8:
							reputation = 200;
							break;
						case 9:
							reputation = 150;
							break;
						case 10:
							reputation = 100;
							break;
						default:
							if (entry.getValue() <= 50)
								reputation = 25;
							else
								reputation = 12;
							break;
					}
					c.addReputationScore(reputation);
				}
			}
		}
		
		RaidBossPointsManager.getInstance().cleanUp();
		_log.info("RaidPointsReset: Raid boss points were added to clan reputation score.");
	}
	
	@Override
	public final void onEnd()
	{
	}
}