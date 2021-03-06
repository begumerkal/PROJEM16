/*
 * Copyright (c) 2015 NOVA, All rights reserved.
 * This library is free software, licensed under GNU Lesser General Public License version 3
 *
 * This file is part of NOVA.
 *
 * NOVA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NOVA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NOVA.  If not, see <http://www.gnu.org/licenses/>.
 */

package nova.core.wrapper.mc.forge.v1_11_2.wrapper.recipes.forward;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import nova.core.item.Item;
import nova.core.recipes.crafting.CraftingGrid;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.item.ItemConverter;

import java.util.Optional;

public class NovaCraftingGrid extends InventoryCrafting {
	private final CraftingGrid craftingGrid;

	public NovaCraftingGrid(CraftingGrid craftingGrid) {
		super(new NovaCraftingGridContainer(craftingGrid), craftingGrid.getWidth(), craftingGrid.getHeight());
		this.craftingGrid = craftingGrid;
	}

	@Override
	public int getSizeInventory() {
		return craftingGrid.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return craftingGrid.getCrafting(slot).map(ItemConverter.instance()::toNative).orElse(ItemStack.EMPTY);
	}

	@Override
	public ItemStack getStackInRowAndColumn(int x, int y) {
		return craftingGrid.getCrafting(x, y).map(ItemConverter.instance()::toNative).orElse(ItemStack.EMPTY);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack item) {
		craftingGrid.setCrafting(slot, Optional.of(item).filter(i -> !i.isEmpty()).map(ItemConverter.instance()::toNova));
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		Optional<Item> optionalItem = craftingGrid.getCrafting(slot);
		if (!optionalItem.isPresent() || count == 0) {
			return null;
		}

		Item item = optionalItem.get();
		int added = -item.addCount(-count);
		if (item.count() == 0) {
			craftingGrid.setCrafting(slot, Optional.empty());
		}
		return ItemConverter.instance().toNative(item.withAmount(added));
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		return craftingGrid.removeCrafting(slot).map(ItemConverter.instance()::toNative).orElse(ItemStack.EMPTY);
	}

	@Override
	public int getWidth() {
		return craftingGrid.getWidth();
	}

	@Override
	public int getHeight() {
		return craftingGrid.getHeight();
	}

	@Override
	public void clear() {
		super.clear();
		for (int i = 0; i < craftingGrid.size(); i++) {
			craftingGrid.setCrafting(i, Optional.empty());
		}
	}
}
