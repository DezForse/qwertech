package com.kbi.qwertech.tileentities;

import com.kbi.qwertech.api.data.QTI;
import com.kbi.qwertech.api.recipe.CountertopRecipe;
import com.kbi.qwertech.api.recipe.managers.CraftingManagerCountertop;
import com.kbi.qwertech.api.tileentities.InventoryScroll;
import com.kbi.qwertech.api.tileentities.SlotScroll;
import com.kbi.qwertech.network.packets.PacketInventorySync;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.block.multitileentity.IMultiTileEntity;
import gregapi.block.multitileentity.MultiTileEntityBlockInternal;
import gregapi.data.CS;
import gregapi.data.LH;
import gregapi.gui.ContainerClient;
import gregapi.gui.ContainerCommon;
import gregapi.gui.Slot_Normal;
import gregapi.old.Textures;
import gregapi.render.BlockTextureDefault;
import gregapi.render.IIconContainer;
import gregapi.render.ITexture;
import gregapi.tileentity.base.TileEntityBase09FacingSingle;
import gregapi.util.ST;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

import static gregapi.data.CS.*;

public class CuttingBoardTileEntity extends TileEntityBase09FacingSingle implements IMultiTileEntity.IMTE_GetSubItems, IMultiTileEntity.IMTE_OnBlockClicked, IMultiTileEntity.IMTE_GetLightOpacity {

    public String mGUITexture = "qwertech:textures/gui/countertop.png";
    public boolean mUpdatedGrid = T;

    @Override
    public void onTick2(long aTimer, boolean aIsServerSide) {
        super.onTick2(aTimer, aIsServerSide);
        if (aIsServerSide) {
            if (mUpdatedGrid) {
                sendDisplays();
                //updateInventory();
                //updateClientData();
                mUpdatedGrid = F;
            } else if (aTimer % (200 + (xCoord % 10) + (zCoord % 10)) == 0)
            {
                sendDisplays();
            }
        }
    }

    public void sendDisplays()
    {
        for (int q = 0; q < this.invsize(); q++)
        {
            QTI.NW_API.sendToAllPlayersInRange(new PacketInventorySync(slot(q), this.xCoord, this.yCoord, this.zCoord, q), this.worldObj, this.xCoord, this.zCoord);
        }
    }

    @Override public void setInventorySlotContents(int aSlot, ItemStack aStack)		{if (aSlot >= 0 && aSlot < 8 && !ST.equal(aStack, slot(aSlot), F)) mUpdatedGrid = T; super.setInventorySlotContents(aSlot, aStack);}
    @Override public void setInventorySlotContentsGUI(int aSlot, ItemStack aStack)	{if (aSlot >= 0 && aSlot < 8 && !ST.equal(aStack, slot(aSlot), F)) mUpdatedGrid = T; super.setInventorySlotContentsGUI(aSlot, aStack);}
    @Override public ItemStack decrStackSize(int aSlot, int aDecrement)				{if (aSlot >= 0 && aSlot < 8 && aDecrement > 0) mUpdatedGrid = T; return super.decrStackSize(aSlot, aDecrement);}
    @Override public ItemStack decrStackSizeGUI(int aSlot, int aDecrement)			{if (aSlot >= 0 && aSlot < 8 && aDecrement > 0) mUpdatedGrid = T; return super.decrStackSizeGUI(aSlot, aDecrement);}


    @Override
    public void addToolTips(List aList, ItemStack aStack, boolean aF3_H) {
        aList.add(LH.Chat.GRAY + "Prepares meals");
        super.addToolTips(aList, aStack, aF3_H);
    }

    @Override
    public void onBlockClicked(EntityPlayer entityPlayer) {

    }

    @Override
    public boolean onBlockActivated3(EntityPlayer aPlayer, byte aSide, float aHitX, float aHitY, float aHitZ) {
        return aSide > 1 && openGUI(aPlayer, 0);
    }

    @Override
    public boolean allowCovers(byte side)
    {
        return false;
    }

    @Override
    public boolean getSubItems(MultiTileEntityBlockInternal aBlock, Item aItem,
                               CreativeTabs aTab, List aList, short aID) {
        return SHOW_HIDDEN_MATERIALS || !mMaterial.mHidden;
    }

    @Override
    public ITexture getTexture2(Block aBlock, int aRenderPass, byte aSide, boolean[] aShouldSideBeRendered) {
        IIconContainer returnable = aRenderPass == 0 ? icons1[aSide < 2 ? 0 : 1] : icons2[aSide < 2 ? 0 : 1];
        return BlockTextureDefault.get(returnable, mRGBa);
    }

    // Icons
    public static String texture_Dir(){
        return "qwertech:cooking/";
    }

    public static IIconContainer icons1[] = new IIconContainer[] {
            new Textures.BlockIcons.CustomIcon(texture_Dir() + "cutboard"),
            new Textures.BlockIcons.CustomIcon(texture_Dir() + "cutside")
    };
    public static IIconContainer icons2[] = new IIconContainer[] {
            new Textures.BlockIcons.CustomIcon(texture_Dir() + "woodbottom"),
            new Textures.BlockIcons.CustomIcon(texture_Dir() + "woodsides")
    };

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses2(Block aBlock, boolean[] aShouldSideBeRendered)
    {
        return 2;
    }


    @Override
    public boolean canDrop(int i) {
        return i < 8;
    }

    @Override
    public String getTileEntityName() {
        return "qt.cooking.simple";
    }

    @SideOnly(Side.CLIENT)
    @Override public Object getGUIClient2(int aGUIID, EntityPlayer aPlayer) {return new GUIClientCuttingBoard(aPlayer.inventory, this);}
    @Override public Object getGUIServer2(int aGUIID, EntityPlayer aPlayer)
    {
        return new GUICommonCuttingBoard(aPlayer.inventory, this);
    }

    @Override
    public boolean isSurfaceOpaque2	(byte aSide)
    {return aSide == CS.SIDE_UP || aSide == CS.SIDE_DOWN;}

    @SideOnly(Side.CLIENT)
    @Override
    public boolean setBlockBounds2(Block block, int aRenderPass, boolean[] aShouldSideBeRendered) {
        short xneg = 1, xpos = 15, zneg = 1, zpos = 15;
        if (this.getOpacityAtSide(CS.SIDE_X_NEG) || (this.getAdjacentTileEntity(CS.SIDE_X_NEG).exists() && this.getAdjacentTileEntity(CS.SIDE_X_NEG).mTileEntity.getClass() == this.getClass()))
        {
            xneg = 0;
        }
        if (this.getOpacityAtSide(CS.SIDE_X_POS) || (this.getAdjacentTileEntity(CS.SIDE_X_POS).exists() && this.getAdjacentTileEntity(CS.SIDE_X_POS).mTileEntity.getClass() == this.getClass()))
        {
            xpos = 16;
        }
        if (this.getOpacityAtSide(CS.SIDE_Z_NEG) || (this.getAdjacentTileEntity(CS.SIDE_Z_NEG).exists() && this.getAdjacentTileEntity(CS.SIDE_Z_NEG).mTileEntity.getClass() == this.getClass()))
        {
            zneg = 0;
        }
        if (this.getOpacityAtSide(CS.SIDE_Z_POS) || (this.getAdjacentTileEntity(CS.SIDE_Z_POS).exists() && this.getAdjacentTileEntity(CS.SIDE_Z_POS).mTileEntity.getClass() == this.getClass()))
        {
            zpos = 16;
        }
        switch (aRenderPass) {
            case 0:
                block.setBlockBounds(PX_P[0], PX_P[10], PX_P[0], PX_P[16], PX_P[16], PX_P[16]);
                break;
            case 1:
                block.setBlockBounds(PX_P[xneg], PX_P[0], PX_P[zneg], PX_P[xpos], PX_P[10], PX_P[zpos]);
                break;
        }
        return true;
    }

    public class GUICommonCuttingBoard extends ContainerCommon {

        public InventoryCrafting craftMatrix;
        public InventoryScroll craftResults;
        public ArrayList<CountertopRecipe> currentRecipes = new ArrayList<CountertopRecipe>();

        public GUICommonCuttingBoard(InventoryPlayer aInventoryPlayer, CuttingBoardTileEntity aTileEntity) {
            super(aInventoryPlayer, aTileEntity);
        }



        @Override
        public void onContainerClosed(EntityPlayer p_75134_1_)
        {
            super.onContainerClosed(p_75134_1_);

            if (!p_75134_1_.worldObj.isRemote)
            {
                //System.out.println("Serverside closed!");
                for (int i = 0; i < 16; ++i)
                {
                    //System.out.println("Checking " + i);
                    ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);

                    if (itemstack != null)
                    {
                        //System.out.println("Found!");
                        //if it's this easy to change...
                        p_75134_1_.dropPlayerItemWithRandomChoice(itemstack, false);
                    }
                }
            }
        }

        @Override
        public int addSlots(InventoryPlayer aInventoryPlayer) {
            if (craftMatrix == null) {
                craftMatrix = new InventoryCrafting(this, 6, 4);
            }
            if (craftResults == null) {
                craftResults = new InventoryScroll(this, 8);
            }

            addSlotToContainer(new Slot(craftMatrix,  0,  8, 14));
            addSlotToContainer(new Slot(craftMatrix,  1, 26, 14));
            addSlotToContainer(new Slot(craftMatrix, 2, 44, 14));
            addSlotToContainer(new Slot(craftMatrix, 3, 62, 14));
            addSlotToContainer(new Slot(craftMatrix, 4,  8, 32));
            addSlotToContainer(new Slot(craftMatrix, 5, 26, 32));
            addSlotToContainer(new Slot(craftMatrix, 6, 44, 32));
            addSlotToContainer(new Slot(craftMatrix, 7, 62, 32));
            addSlotToContainer(new Slot(craftMatrix, 8,  8, 50));
            addSlotToContainer(new Slot(craftMatrix, 9, 26, 50));
            addSlotToContainer(new Slot(craftMatrix, 10, 44, 50));
            addSlotToContainer(new Slot(craftMatrix, 11, 62, 50));
            addSlotToContainer(new Slot(craftMatrix, 12,  8, 68));
            addSlotToContainer(new Slot(craftMatrix, 13, 26, 68));
            addSlotToContainer(new Slot(craftMatrix, 14, 44, 68));
            addSlotToContainer(new Slot(craftMatrix, 15, 62, 68));

            addSlotToContainer(new Slot_Normal(mTileEntity, 0, 16, 92));
            addSlotToContainer(new Slot_Normal(mTileEntity, 1, 34, 92));
            addSlotToContainer(new Slot_Normal(mTileEntity, 2, 52, 92));
            addSlotToContainer(new Slot_Normal(mTileEntity, 3, 70, 92));
            addSlotToContainer(new Slot_Normal(mTileEntity, 4, 88, 92));
            addSlotToContainer(new Slot_Normal(mTileEntity, 5, 106, 92));
            addSlotToContainer(new Slot_Normal(mTileEntity, 6, 124, 92));
            addSlotToContainer(new Slot_Normal(mTileEntity, 7, 142, 92));

            addSlotToContainer(new SlotScroll(craftResults, 0, 134, 14, F, F, 64));
            addSlotToContainer(new SlotScroll(craftResults, 1, 152, 14, F, F, 64));
            addSlotToContainer(new SlotScroll(craftResults, 2, 134, 32, F, F, 64));
            addSlotToContainer(new SlotScroll(craftResults, 3, 152, 32, F, F, 64));
            addSlotToContainer(new SlotScroll(craftResults, 4, 134, 50, F, F, 64));
            addSlotToContainer(new SlotScroll(craftResults, 5, 152, 50, F, F, 64));
            addSlotToContainer(new SlotScroll(craftResults, 6, 134, 68, F, F, 64));
            addSlotToContainer(new SlotScroll(craftResults, 7, 152, 68, F, F, 64));
            return super.addSlots(aInventoryPlayer);
        }

        public ItemStack consume(CountertopRecipe recipe)
        {
            if (!((CuttingBoardTileEntity)mTileEntity).isServerSide() || !recipe.matchesLists(this.inventoryItemStacks.subList(0, 16), currentRecipes)) {
                return null;
            }
            ItemStack toReturn = recipe.getCraftingResult(craftMatrix);
            System.out.println("Returnable is " + toReturn);
            Integer[] slots = recipe.getRecipeSlotsUsed();
            ArrayList<CountertopRecipe> recipes = new ArrayList<CountertopRecipe>();
            if (slots != null && slots.length > 0) {
                for (int w = 0; w < slots.length; w++) {
                    if (slots[w] > 0) {
                        for (int e = 0; e < slots[w]; e++)
                        {
                            recipes.add(currentRecipes.get(w));
                        }
                    }
                }
                for (int q = 0; q < recipes.size(); q++) {
                    CountertopRecipe intermediate = recipes.get(q);
                    ItemStack product = consume(intermediate);
                    for (int o = 0; o < 16; o++)
                    {
                        Slot slotty = (Slot)this.inventorySlots.get(o);
                        if (slotty.getHasStack())
                        {
                            if (ST.equal(slotty.getStack(), product) && slotty.getStack().stackSize + product.stackSize <= product.getMaxStackSize())
                            {
                                product.stackSize = product.stackSize + slotty.getStack().stackSize;
                                slotty.putStack(product);
                                detectAndSendChanges();
                                break;
                            }
                        } else {
                            slotty.putStack(product);
                            detectAndSendChanges();
                            break;
                        }
                    }
                }
            }
            if (recipe.matchesLists(this.inventoryItemStacks.subList(0, 16), currentRecipes)) {
                slots = recipe.getInputSlotsUsed();
                for (int e = 0; e < slots.length; e++) {
                    if (slots[e] > 0) {
                        ((Slot) inventorySlots.get(e)).decrStackSize(slots[e]);
                    }
                }
                return toReturn;
            }
            return null;
        }

        @Override
        public ItemStack slotClick(int aSlotIndex, int aMouseclick, int aShifthold, EntityPlayer aPlayer) {
            //System.out.println("Click made! It's slot " + aSlotIndex);
            if (aSlotIndex > 31 || aSlotIndex < 0) return super.slotClick(aSlotIndex, aMouseclick, aShifthold, aPlayer);
            //System.out.println("It's one to do stuff with");
            Slot tSlot = ((Slot) inventorySlots.get(aSlotIndex));
            ItemStack tStack = tSlot.getStack();
            if (tStack != null)
            {
                if (aSlotIndex > 23)
                {
                    CountertopRecipe chosenRecipe = currentRecipes.get(craftResults.starting + aSlotIndex - 24);
                    ItemStack toReturn = consume(chosenRecipe);
                    if (toReturn == null) return null;
                    System.out.println("It's a recipe that made " + toReturn.getDisplayName() + " times " + toReturn.stackSize);
                    for (int q = 0; q < 8; q++)
                    {
                        Slot aSlot = ((Slot) inventorySlots.get(16 + q));
                        if (!aSlot.getHasStack())
                        {
                            aSlot.putStack(toReturn);
                            toReturn = null;
                            break;
                        }
                    }
                    if (toReturn != null && ((CuttingBoardTileEntity)this.mTileEntity).isServerSide()) {
                        aPlayer.entityDropItem(toReturn, 0);
                    }

                    detectAndSendChanges();
                    return toReturn;
                } else if (tStack.stackSize <= 0) {
                    tSlot.putStack(null);
                    ItemStack toReturn = super.slotClick(aSlotIndex, aMouseclick, aShifthold, aPlayer);
                    if (aSlotIndex < 16) {
                        craftMatrix.setInventorySlotContents(aSlotIndex, toReturn);
                    }
                    detectAndSendChanges();
                    return toReturn;
                }
            }
            ItemStack returner = super.slotClick(aSlotIndex, aMouseclick, aShifthold, aPlayer);
            onCraftMatrixChanged(craftMatrix);
            detectAndSendChanges();
            return returner;
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
        }

        @Override
        public void onCraftMatrixChanged(IInventory par1IInventory)
        {
            currentRecipes = CraftingManagerCountertop.getInstance().findMatchingRecipes(craftMatrix, worldObj);
            ArrayList<ItemStack> results = new ArrayList<ItemStack>();
            for (CountertopRecipe recipe: currentRecipes) {
                results.add(recipe.getCraftingResult(craftMatrix));
            }
            craftResults.setList(results);
            super.onCraftMatrixChanged(par1IInventory);
        }

        @Override
        public int getSlotCount() {
            return 32;
        }

        @Override
        public int getShiftClickSlotCount() {
            return 8;
        }

        @Override
        public int getShiftClickStartIndex() {
            return 16;
        }

        @Override
        public int getStartIndex() {
            return 0;
        }

        @Override
        protected void bindPlayerInventory(InventoryPlayer aInventoryPlayer, int aOffset) {
            for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(aInventoryPlayer, j + i * 9 + 9, 8 + j * 18, (aOffset + i * 18) + 32));
            }
            for (int i = 0; i < 9; i++) {
                addSlotToContainer(new Slot(aInventoryPlayer, i, 8 + i * 18, aOffset + 90));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public class GUIClientCuttingBoard extends ContainerClient {

        private int upButtonCounter = 0;
        private int downButtonCounter = 0;
        private boolean[] isAvailable = new boolean[8];
        private ArrayList<ItemStack> cached = new ArrayList<ItemStack>(16);
        public GUIClientCuttingBoard(InventoryPlayer aInventoryPlayer, CuttingBoardTileEntity aTileEntity) {
            super(new GUICommonCuttingBoard(aInventoryPlayer, aTileEntity), aTileEntity.mGUITexture);
            this.xSize = 176;
            this.ySize = 198;
        }

        @Override
        protected void mouseClicked(int x, int y, int mB) {
            super.mouseClicked(x, y, mB);
            //System.out.println("Mouse clicked at " + x + ", " + y);
            if (x > getLeft() + 118 && x < getLeft() + 130)
            {
                if (y > getTop() + 14 && y < getTop() + 21)
                {
                    upButtonCounter = 5;
                    ((GUICommonCuttingBoard)mContainer).craftResults.scrollDown(1);
                } else if (y > getTop() + 77 && y < getTop() + 84)
                {
                    downButtonCounter = 5;
                    ((GUICommonCuttingBoard)mContainer).craftResults.scrollUp(1);
                }
            }
            updateNotifications();
        }

        public void updateNotifications()
        {
            //the can-we-do-it icons
            for (int w = 0; w < 8; w++)
            {
                try {
                    GUICommonCuttingBoard GUI = ((GUICommonCuttingBoard) this.inventorySlots);
                    if (GUI.currentRecipes.size() > w) {
                        CountertopRecipe recipe = GUI.currentRecipes.get(w + GUI.craftResults.starting);
                        if (recipe != null) {
                            if (recipe.matchesLists(GUI.inventoryItemStacks.subList(0, 16), GUI.currentRecipes)) {
                                isAvailable[w] = true;
                            } else {
                                isAvailable[w] = false;
                            }
                        } else {
                            isAvailable[w] = false;
                        }
                    }
                } catch (Exception e) {

                }
            }
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
            super.drawGuiContainerBackgroundLayer(par1, par2, par3);
            if (!this.inventorySlots.inventoryItemStacks.equals(cached))
            {
                updateNotifications();
                cached = new ArrayList<ItemStack>(this.inventorySlots.inventoryItemStacks);
            }
        }



        @Override
        public void drawScreen(int aX, int aY, float par3) {
            super.drawScreen(aX, aY, par3);
            this.zLevel = 0.0F;
        }

        @Override
        protected void renderToolTip(ItemStack stack, int x, int y) {
            System.out.println(x + "; " + y);
            GUICommonCuttingBoard CB = (GUICommonCuttingBoard)this.mContainer;
            if (this.isMouseOverSlot(CB.getSlot(24), x, y))
            {
                //List list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

                List list = new ArrayList();
                list.add(stack.getRarity().rarityColor + stack.getDisplayName());

                CountertopRecipe recipe = CB.currentRecipes.get(CB.craftResults.starting);
                List<Object> input = recipe.getInputList();
                HashMap<String, Integer> amounts = new HashMap<String, Integer>();
                for (int q = 0; q < input.size(); q++)
                {
                    Object ob = input.get(q);
                    if (ob instanceof ItemStack)
                    {
                        amounts.put(((ItemStack)ob).getDisplayName(), amounts.get(((ItemStack)ob).getDisplayName()) + ((ItemStack)ob).stackSize);
                    } else if (ob instanceof ArrayList && ((ArrayList)ob).size() > 0)
                    {
                        ItemStack IS = ((ArrayList<ItemStack>)ob).get(0);
                        amounts.put(IS.getDisplayName(), amounts.get(IS.getDisplayName()) + IS.stackSize);
                    } else if (ob instanceof String)
                    {
                        amounts.put((String)ob, amounts.get((String)ob + 1));
                    }
                }
                Iterator iterable = amounts.entrySet().iterator();
                while (iterable.hasNext())
                {
                    Map.Entry entry = (Map.Entry)iterable.next();
                    list.add((String)entry.getKey() + ": " + (Integer)entry.getValue());
                }

                FontRenderer font = stack.getItem().getFontRenderer(stack);
                drawHoveringText(list, x, y, (font == null ? fontRendererObj : font));
            } else {
                super.renderToolTip(stack, x, y);
            }
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int par1, int par2) {
            super.drawGuiContainerForegroundLayer(par1, par2);
            this.mc.getTextureManager().bindTexture(this.mBackground);

            //the scrollbar
            drawTexturedModalRect(118, 21 + Math.round(41 * ((GUICommonCuttingBoard)mContainer).craftResults.getScroll()), 176, 0,12, 15);

            //the topbutton
            if (upButtonCounter > 0)
            {
                upButtonCounter--;
                drawTexturedModalRect(118, 14, 176, 29, 12, 7);
            } else {
                drawTexturedModalRect(118, 14, 176, 15, 12, 7);
            }

            //the bottombutton
            if (downButtonCounter > 0)
            {
                downButtonCounter--;
                drawTexturedModalRect(118, 77, 176, 36, 12, 7);
            } else {
                drawTexturedModalRect(118, 77, 176, 22, 12, 7);
            }

            this.mc.getTextureManager().bindTexture(this.mBackground);
            this.zLevel = 201.0F;
            for (int x = 0; x < 8; x++)
            {
                Slot slot = (Slot)this.inventorySlots.inventorySlots.get(24 + x);
                if (this.inventorySlots.inventoryItemStacks.get(24 + x) != null) {
                    if (isAvailable[x]) {
                        drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 188, 0, 16, 16);
                    } else {
                        drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 188, 16, 16, 16);
                    }
                }
            }
            this.zLevel = 0.0F;
        }
    }
}
