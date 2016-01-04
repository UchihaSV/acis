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

public class FuncMaxMpAdd extends Func
{
	static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();
	
	public static Func getInstance()
	{
		return _fmma_instance;
	}
	
	private FuncMaxMpAdd()
	{
		super(Stats.MAX_MP, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final L2PcTemplate t = (L2PcTemplate) env.getCharacter().getTemplate();
		final int lvl = Math.max(env.getCharacter().getLevel() - t.classBaseLevel, 0);
		
		final double mpmod = t.lvlMpMod * lvl;
		final double mpmax = (t.lvlMpAdd + mpmod) * lvl;
		final double mpmin = (t.lvlMpAdd * lvl) + mpmod;
		
		env.addValue((mpmax + mpmin) / 2);
	}
}