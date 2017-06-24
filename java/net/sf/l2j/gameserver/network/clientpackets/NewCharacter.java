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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.serverpackets.CharTemplates;

public final class NewCharacter extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		CharTemplates ct = new CharTemplates();
		
		ct.addChar(CharTemplateTable.getInstance().getTemplate(0));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.HUMAN_FIGHTER));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.HUMAN_MYSTIC));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ELVEN_FIGHTER));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ELVEN_MYSTIC));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DARK_FIGHTER));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DARK_MYSTIC));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ORC_FIGHTER));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ORC_MYSTIC));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DWARVEN_FIGHTER));
		
		sendPacket(ct);
	}
}