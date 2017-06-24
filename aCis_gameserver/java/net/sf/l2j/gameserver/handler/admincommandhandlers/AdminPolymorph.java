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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Object.PolyType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;

/**
 * This class handles polymorph commands.
 */
public class AdminPolymorph implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_polymorph",
		"admin_unpolymorph",
		"admin_polymorph_menu",
		"admin_unpolymorph_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar.isMounted())
			return false;
		
		L2Object target = activeChar.getTarget();
		if (target == null)
			target = activeChar;
		
		if (command.startsWith("admin_polymorph"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				
				PolyType info = PolyType.NPC;
				if (st.countTokens() > 1)
					info = Enum.valueOf(PolyType.class, st.nextToken().toUpperCase());
				
				final int npcId = Integer.parseInt(st.nextToken());
				
				if (!target.polymorph(info, npcId))
				{
					activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT);
					return true;
				}
				
				activeChar.sendMessage("You polymorphed " + target.getName() + " into a " + info + " using id: " + npcId + ".");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //polymorph <type> <id>");
			}
		}
		else if (command.startsWith("admin_unpolymorph"))
		{
			if (target.getPolyType() == PolyType.DEFAULT)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return true;
			}
			
			target.unpolymorph();
			
			activeChar.sendMessage("You successfully unpolymorphed " + target.getName() + ".");
		}
		
		if (command.contains("menu"))
			AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}