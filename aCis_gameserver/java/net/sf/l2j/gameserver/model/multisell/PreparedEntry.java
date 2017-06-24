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
package net.sf.l2j.gameserver.model.multisell;

import java.util.ArrayList;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class PreparedEntry extends Entry
{
	private int _taxAmount = 0;
	
	public PreparedEntry(Entry template, ItemInstance item, boolean applyTaxes, boolean maintainEnchantment, double taxRate)
	{
		_id = template.getId() * 100000;
		if (maintainEnchantment && item != null)
			_id += item.getEnchantLevel();
		
		int adenaAmount = 0;
		
		_ingredients = new ArrayList<>(template.getIngredients().size());
		for (Ingredient ing : template.getIngredients())
		{
			if (ing.getItemId() == 57)
			{
				// Tax ingredients added only if taxes enabled
				if (ing.isTaxIngredient())
				{
					// if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
					if (applyTaxes)
						_taxAmount += Math.round(ing.getItemCount() * taxRate);
				}
				else
					adenaAmount += ing.getItemCount();
				
				// do not yet add this adena amount to the list as non-taxIngredient adena might be entered later (order not guaranteed)
				continue;
			}
			
			final Ingredient newIngredient = ing.getCopy();
			if (maintainEnchantment && item != null && ing.isArmorOrWeapon())
				newIngredient.setEnchantLevel(item.getEnchantLevel());
			
			_ingredients.add(newIngredient);
		}
		
		// now add the adena, if any.
		adenaAmount += _taxAmount; // do not forget tax
		if (adenaAmount > 0)
			_ingredients.add(new Ingredient(57, adenaAmount, false, false));
		
		// now copy products
		_products = new ArrayList<>(template.getProducts().size());
		for (Ingredient ing : template.getProducts())
		{
			if (!ing.isStackable())
				_stackable = false;
			
			final Ingredient newProduct = ing.getCopy();
			if (maintainEnchantment && item != null && ing.isArmorOrWeapon())
				newProduct.setEnchantLevel(item.getEnchantLevel());
			
			_products.add(newProduct);
		}
	}
	
	@Override
	public final int getTaxAmount()
	{
		return _taxAmount;
	}
}