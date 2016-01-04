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
package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.basefuncs.Func;
import net.sf.l2j.gameserver.templates.chars.L2PcTemplate;

public class FuncMaxHpAdd extends Func
{
	static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();
	
	public static Func getInstance()
	{
		return _fmha_instance;
	}
	
	private FuncMaxHpAdd()
	{
		super(Stats.MAX_HP, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final L2PcTemplate t = (L2PcTemplate) env.getCharacter().getTemplate();
		final int lvl = Math.max(env.getCharacter().getLevel() - t.classBaseLevel, 0);
		
		final double hpmod = t.lvlHpMod * lvl;
		final double hpmax = (t.lvlHpAdd + hpmod) * lvl;
		final double hpmin = (t.lvlHpAdd * lvl) + hpmod;
		
		env.addValue((hpmax + hpmin) / 2);
	}
}