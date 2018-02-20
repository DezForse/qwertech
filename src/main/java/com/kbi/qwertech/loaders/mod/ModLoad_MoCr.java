package com.kbi.qwertech.loaders.mod;

import gregapi.code.ModData;
import gregapi.data.MD;
import gregapi.util.ST;
import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModLoad_MoCr extends ModLoadBase {

	@Override
	public ModData getMod() {
		return MD.MoCr;
	}
	
	@Override
	public void addOreDict()
	{
		String mId = this.getMod().mID;
		String[] oreNames = new String[]{"Wyvern", "Ogre"};
		String[] blockNames = new String[]{"MoCWoodPlank", "MoCWoodPlank"};
		int[] meta = new int[]{0, 1};
		for (int q = 0; q < oreNames.length; q++)
		{
			Block result = GameRegistry.findBlock(mId, blockNames[q]);
			if (result != null)
			{
				OreDictionary.registerOre("plankWood" + oreNames[q], ST.make(result, 1, meta[q]));
			}
		}
	}

}