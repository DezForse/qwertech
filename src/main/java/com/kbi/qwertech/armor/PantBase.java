package com.kbi.qwertech.armor;

import gregapi.render.IIconContainer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import com.kbi.qwertech.api.armor.ArmorBase;
import com.kbi.qwertech.loaders.RegisterArmor;

public class PantBase extends ArmorBase {
	public PantBase()
	{
		super();
	}
	
	@Override
	public int getBaseQuality()
	{
		return 0;
	}
	
	@Override
	public IIconContainer getIcon(ItemStack stack)
	{
		return (IIconContainer)RegisterArmor.iconTitle.get("qwertech:armor/leggings/chainmail");
	}
	
	@Override
	public float getDamageProtection()
	{
		return 0.8F;
	}
	
	@Override
	public boolean isValidInSlot(int armorSlot)
	{
		return armorSlot == 2;
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		if (type != "overlay")
		{
			return "qwertech:textures/armor/chainmail.png";
		} else {
			return "qwertech:textures/armor/blank.png";
		}
	}
	
}
