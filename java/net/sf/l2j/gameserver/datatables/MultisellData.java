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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.multisell.Entry;
import net.sf.l2j.gameserver.model.multisell.Ingredient;
import net.sf.l2j.gameserver.model.multisell.ListContainer;
import net.sf.l2j.gameserver.model.multisell.PreparedListContainer;
import net.sf.l2j.gameserver.network.serverpackets.MultiSellList;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class MultisellData
{
	private static final Logger _log = Logger.getLogger(MultisellData.class.getName());
	
	public static final int PAGE_SIZE = 40;
	
	private final Map<Integer, ListContainer> _entries = new HashMap<>();
	
	public MultisellData()
	{
		load();
	}
	
	public void reload()
	{
		_entries.clear();
		load();
	}
	
	private void load()
	{
		final File dir = new File("./data/xml/multisell");
		if (!dir.isDirectory())
		{
			_log.config("Dir " + dir.getAbsolutePath() + " doesn't exist.");
			return;
		}
		
		final StatsSet set = new StatsSet();
		
		for (File f : dir.listFiles())
		{
			int entryId = 1;
			
			try
			{
				int id = f.getName().replaceAll(".xml", "").hashCode();
				Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
				
				final ListContainer list = new ListContainer(id);
				
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						Node att = n.getAttributes().getNamedItem("applyTaxes");
						list.setApplyTaxes(att != null && Boolean.parseBoolean(att.getNodeValue()));
						
						att = n.getAttributes().getNamedItem("maintainEnchantment");
						list.setMaintainEnchantment(att != null && Boolean.parseBoolean(att.getNodeValue()));
						
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("item".equalsIgnoreCase(d.getNodeName()))
							{
								final Entry entry = new Entry(entryId++);
								
								for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
								{
									if ("ingredient".equalsIgnoreCase(e.getNodeName()))
									{
										NamedNodeMap attrs = e.getAttributes();
										
										for (int i = 0; i < attrs.getLength(); i++)
										{
											att = attrs.item(i);
											set.set(att.getNodeName(), att.getNodeValue());
										}
										entry.addIngredient(new Ingredient(set));
										set.clear();
									}
									else if ("production".equalsIgnoreCase(e.getNodeName()))
									{
										NamedNodeMap attrs = e.getAttributes();
										
										for (int i = 0; i < attrs.getLength(); i++)
										{
											att = attrs.item(i);
											set.set(att.getNodeName(), att.getNodeValue());
										}
										entry.addProduct(new Ingredient(set));
										set.clear();
									}
								}
								
								list.getEntries().add(entry);
							}
							else if ("npcs".equalsIgnoreCase(d.getNodeName()))
							{
								for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
								{
									if ("npc".equalsIgnoreCase(b.getNodeName()))
									{
										if (StringUtil.isDigit(b.getTextContent()))
											list.allowNpc(Integer.parseInt(b.getTextContent()));
									}
								}
							}
						}
					}
				}
				
				_entries.put(id, list);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error loading file " + f, e);
			}
		}
		_log.log(Level.INFO, "MultisellData: Loaded " + _entries.size() + " files.");
	}
	
	public void separateAndSend(String listName, L2PcInstance player, L2Npc npc, boolean inventoryOnly)
	{
		final ListContainer template = _entries.get(listName.hashCode());
		if (template == null)
			return;
		
		if ((npc != null && !template.isNpcAllowed(npc.getNpcId())) || (npc == null && template.isNpcOnly()))
			return;
		
		final PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
		
		int index = 0;
		do
		{
			// send list at least once even if size = 0
			player.sendPacket(new MultiSellList(list, index));
			index += PAGE_SIZE;
		}
		while (index < list.getEntries().size());
		
		player.setMultiSell(list);
	}
	
	public static MultisellData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MultisellData _instance = new MultisellData();
	}
}