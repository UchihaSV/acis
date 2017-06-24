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
package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.HelperBuff;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class loads and store newbie buffs into a List of HelperBuff.
 */
public class HelperBuffTable
{
	private static Logger _log = Logger.getLogger(HelperBuffTable.class.getName());
	
	private final List<HelperBuff> _helperBuff = new ArrayList<>();
	
	private int _magicClassLowestLevel = 100;
	private int _physicClassLowestLevel = 100;
	
	private int _magicClassHighestLevel = 1;
	private int _physicClassHighestLevel = 1;
	
	protected HelperBuffTable()
	{
		try
		{
			final StatsSet set = new StatsSet();
			
			File f = new File("./data/xml/helper_buff_list.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("buff"))
				{
					NamedNodeMap attrs = d.getAttributes();
					
					int skillId = Integer.valueOf(attrs.getNamedItem("skillId").getNodeValue());
					int skillLevel = Integer.valueOf(attrs.getNamedItem("skillLevel").getNodeValue());
					int lowerLevel = Integer.valueOf(attrs.getNamedItem("lowerLevel").getNodeValue());
					int upperLevel = Integer.valueOf(attrs.getNamedItem("upperLevel").getNodeValue());
					boolean isMagicClass = Boolean.valueOf(attrs.getNamedItem("isMagicClass").getNodeValue());
					
					set.set("skillId", skillId);
					set.set("skillLevel", skillLevel);
					set.set("lowerLevel", lowerLevel);
					set.set("upperLevel", upperLevel);
					set.set("isMagicClass", isMagicClass);
					
					if (!isMagicClass)
					{
						if (lowerLevel < _physicClassLowestLevel)
							_physicClassLowestLevel = lowerLevel;
						
						if (upperLevel > _physicClassHighestLevel)
							_physicClassHighestLevel = upperLevel;
					}
					else
					{
						if (lowerLevel < _magicClassLowestLevel)
							_magicClassLowestLevel = lowerLevel;
						
						if (upperLevel > _magicClassHighestLevel)
							_magicClassHighestLevel = upperLevel;
					}
					
					_helperBuff.add(new HelperBuff(set));
					set.clear();
				}
			}
		}
		catch (Exception e)
		{
			_log.severe("HelperBuffTable: Error while creating table" + e);
		}
		
		_log.info("HelperBuffTable: Loaded " + _helperBuff.size() + " buffs.");
	}
	
	/**
	 * @return the Helper Buff List
	 */
	public List<HelperBuff> getHelperBuffTable()
	{
		return _helperBuff;
	}
	
	/**
	 * @return Returns the magicClassHighestLevel.
	 */
	public int getMagicClassHighestLevel()
	{
		return _magicClassHighestLevel;
	}
	
	/**
	 * @return Returns the magicClassLowestLevel.
	 */
	public int getMagicClassLowestLevel()
	{
		return _magicClassLowestLevel;
	}
	
	/**
	 * @return Returns the physicClassHighestLevel.
	 */
	public int getPhysicClassHighestLevel()
	{
		return _physicClassHighestLevel;
	}
	
	/**
	 * @return Returns the physicClassLowestLevel.
	 */
	public int getPhysicClassLowestLevel()
	{
		return _physicClassLowestLevel;
	}
	
	public static HelperBuffTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HelperBuffTable _instance = new HelperBuffTable();
	}
}