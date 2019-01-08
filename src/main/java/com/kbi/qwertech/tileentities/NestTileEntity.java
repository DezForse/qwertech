package com.kbi.qwertech.tileentities;

import com.kbi.qwertech.api.data.QTI;
import com.kbi.qwertech.network.packets.PacketInventorySync;
import gregapi.block.multitileentity.IMultiTileEntity;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.old.Textures;
import gregapi.render.BlockTextureDefault;
import gregapi.render.IIconContainer;
import gregapi.render.ITexture;
import gregapi.tileentity.base.TileEntityBase05Inventories;
import gregapi.util.OM;
import gregapi.util.ST;
import gregapi.util.UT;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static gregapi.data.CS.F;
import static gregapi.data.CS.PX_P;
import static gregapi.data.CS.T;

public class NestTileEntity extends TileEntityBase05Inventories implements IMultiTileEntity.IMTE_GetBlocksMovement, IMultiTileEntity.IMTE_AddCollisionBoxesToList, IMultiTileEntity.IMTE_SetBlockBoundsBasedOnState, IMultiTileEntity.IMTE_GetSelectedBoundingBoxFromPool, IMultiTileEntity.IMTE_GetCollisionBoundingBoxFromPool {

    protected boolean mUpdatedGrid = true;
    @Override
    public void onTick(long aTimer, boolean aIsServerSide) {
        super.onTick(aTimer, aIsServerSide);
        if (aIsServerSide) {
            if (mUpdatedGrid) {
                mBlockUpdated = true;
                sendDisplays();
                mUpdatedGrid = F;
            } else if (aTimer % (200 + (xCoord % 10) + (zCoord % 10)) == 0)
            {
                mBlockUpdated = true;
                sendDisplays();
            }
        }
    }

    @Override public void setInventorySlotContents(int aSlot, ItemStack aStack)		{if (!ST.equal(aStack, slot(aSlot), F)) mUpdatedGrid = T; super.setInventorySlotContents(aSlot, aStack);}
    @Override public void setInventorySlotContentsGUI(int aSlot, ItemStack aStack)	{if (!ST.equal(aStack, slot(aSlot), F)) mUpdatedGrid = T; super.setInventorySlotContentsGUI(aSlot, aStack);}
    @Override public ItemStack decrStackSize(int aSlot, int aDecrement)				{if (aDecrement > 0) mUpdatedGrid = T; return super.decrStackSize(aSlot, aDecrement);}
    @Override public ItemStack decrStackSizeGUI(int aSlot, int aDecrement)			{if (aDecrement > 0) mUpdatedGrid = T; return super.decrStackSizeGUI(aSlot, aDecrement);}

    @Override public boolean addStackToSlot(int aSlot, ItemStack aStack) {if (!ST.equal(aStack, slot(aSlot), F)) mUpdatedGrid = T; return super.addStackToSlot(aSlot, aStack); }

    public void sendDisplays()
    {
        for (int q = 0; q < this.invsize(); q++)
        {
            QTI.NW_API.sendToAllPlayersInRange(new PacketInventorySync(slot(q), this.xCoord, this.yCoord, this.zCoord, q), this.worldObj, this.xCoord, this.zCoord);
        }
    }

    @Override
    public boolean canDrop(int aSlot) {
        return true;
    }

    @Override
    public String getTileEntityName() {
        return "qt.birdnest";
    }

    public static IIconContainer nest = new Textures.BlockIcons.CustomIcon("qwertech:soil/drystraw");
    public short[] ourcolor = null;
    protected EntityLiving chosenEntity;

    @Override
    public void readFromNBT2(NBTTagCompound aNBT) {
        super.readFromNBT2(aNBT);
        String entityID = aNBT.getString("ENT");
        if (entityID != null)
        {
            UUID uuid = UUID.fromString(entityID);
            if (chosenEntity == null || chosenEntity.getUniqueID() != uuid)
            {
                chosenEntity = null;
                List entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, this.box().expand(32, 16, 32));
                for (Object entity : entities) {
                    EntityLiving eL = (EntityLiving)entity;
                    if (eL.getUniqueID() == uuid)
                    {
                        chosenEntity = eL;
                        break;
                    }
                }
            }
        }
        sendDisplays();
    }

    @Override
    public void writeToNBT2(NBTTagCompound aNBT) {
        super.writeToNBT2(aNBT);
        if (chosenEntity != null)
        {
            aNBT.setString("ENT", chosenEntity.getPersistentID().toString());
        }
    }

    /**
     * Attempts to set the passed entity as the owner of the nest.
     * @param entityLiving The entity to nest here.
     * @return true if the nest is unoccupied by a living nearby creature and the passed entity is an adult.
     */
    public boolean setNestingEntity(EntityLiving entityLiving)
    {
        if (entityLiving == chosenEntity)
        {
            return true;
        }
        if (chosenEntity != null && !chosenEntity.isDead && chosenEntity.getDistance(xCoord, yCoord, zCoord) < 32)
        {
            return false;
        }
        if (entityLiving.isChild())
        {
            return false;
        }
        chosenEntity = entityLiving;
        return true;
    }

    public EntityLiving getNestingEntity()
    {
        if (chosenEntity.isDead || chosenEntity.getDistance(xCoord, yCoord, zCoord) > 32)
        {
            //System.out.println("Too far away: " + chosenEntity.getDistance(xCoord, yCoord, zCoord));
            chosenEntity = null;
        }
        return chosenEntity;
    }

    @Override
    public boolean onBlockActivated2(EntityPlayer aPlayer, byte aSide, float aHitX, float aHitY, float aHitZ) {
        ItemStack stack = aPlayer.getHeldItem();
        if (stack != null && OM.is("magnifyingglass", stack))
        {
            List<String> chats = new ArrayList<String>();
            if (getNestingEntity() == null)
            {
                chats.add("It seems this nest has been abandoned by its owner.");
            } else {
                chats.add("This nest belongs to a " + getNestingEntity().getCommandSenderName());
            }
        }
        return super.onBlockActivated2(aPlayer, aSide, aHitX, aHitY, aHitZ);
    }

    @Override
    public ITexture getTexture(Block aBlock, int aRenderPass, byte aSide, boolean[] aShouldSideBeRendered) {
        if (aRenderPass > 4)
        {
            ItemStack stack = slot(aRenderPass - 5);
            short[] colors = new short[4];
            if (stack != null)
            {
                NBTTagCompound nbt = UT.NBT.getOrCreate(stack);
                int color = nbt.getInteger("itemColor");
                Color oColor = Color.getColor(null, color);
                colors[0] = (short)oColor.getRed();
                colors[1] = (short)oColor.getBlue();
                colors[2] = (short)oColor.getGreen();
                colors[3] = (short)oColor.getAlpha();
                return BlockTextureDefault.get(MT.Bone.mTextureSetsBlock.get(OP.blockDust.mIconIndexBlock), colors);
                //return BlockTextureDefault.get(nest, colors);
            }
            return null;
        }
        if (ourcolor == null)
        {
            ourcolor = new short[]{(short)(210 + Math.abs(this.xCoord % 16)), (short)(245 + Math.abs(this.zCoord % 8)), (short)(200 + Math.abs(this.zCoord % 16) - Math.abs(this.xCoord % 12)), 255};
        }
        return BlockTextureDefault.get(nest, ourcolor);
    }

    @Override
    public boolean setBlockBounds(Block aBlock, int aRenderPass, boolean[] aShouldSideBeRendered) {
        switch(aRenderPass) {
            case 9:
                aBlock.setBlockBounds(PX_P[8], PX_P[1], PX_P[10], PX_P[10], PX_P[5], PX_P[12]);
            case 8:
                aBlock.setBlockBounds(PX_P[10], PX_P[1], PX_P[5], PX_P[12], PX_P[5], PX_P[7]);
                break;
            case 7:
                aBlock.setBlockBounds(PX_P[5], PX_P[1], PX_P[8], PX_P[7], PX_P[5], PX_P[11]);
                break;
            case 6:
                aBlock.setBlockBounds(PX_P[5], PX_P[1], PX_P[6], PX_P[7], PX_P[5], PX_P[8]);
                break;
            case 5:
                aBlock.setBlockBounds(PX_P[7], PX_P[1], PX_P[7], PX_P[9], PX_P[5], PX_P[9]);
                break;
            case 4:
                aBlock.setBlockBounds(PX_P[0], PX_P[1], PX_P[14], PX_P[16], PX_P[3], PX_P[16]);
                break;
            case 3:
                aBlock.setBlockBounds(PX_P[0], PX_P[1], PX_P[0], PX_P[16], PX_P[3], PX_P[2]);
                break;
            case 2:
                aBlock.setBlockBounds(PX_P[14], PX_P[1], PX_P[0], PX_P[16], PX_P[3], PX_P[16]);
                break;
            case 1:
                aBlock.setBlockBounds(PX_P[0], PX_P[1], PX_P[0], PX_P[2], PX_P[3], PX_P[16]);
                break;
            default:
                aBlock.setBlockBounds(PX_P[0], PX_P[0], PX_P[0], PX_P[16], PX_P[1], PX_P[16]);
        }
        return true;
    }

    @Override
    public int getRenderPasses(Block aBlock, boolean[] aShouldSideBeRendered) {
        boolean empty = true;
        for (int q = 0; q < this.getSizeInventory(); q++)
        {
            if (slotHas(q))
            {
                empty = false;
                break;
            }
        }
        if (empty) {
            return 5;
        } else {
            return 5 + invsize();
        }
    }

    @Override
    public boolean getBlocksMovement() {
        return true;
    }



    @Override
    public void addCollisionBoxesToList(AxisAlignedBB aAABB, List<AxisAlignedBB> aList, Entity aEntity) {
        this.box(aAABB, aList, PX_P[0], PX_P[0], PX_P[0], PX_P[16], PX_P[1], PX_P[16]);
    }

    @Override
    public void setBlockBoundsBasedOnState(Block aBlock) {
        boolean empty = true;
        for (int q = 0; q < this.getSizeInventory(); q++)
        {
            if (slotHas(q))
            {
                empty = false;
                break;
            }
        }
        if (empty) {
            aBlock.setBlockBounds(PX_P[0], PX_P[0], PX_P[0], PX_P[16], PX_P[3], PX_P[16]);
        } else {
            aBlock.setBlockBounds(PX_P[0], PX_P[0], PX_P[0], PX_P[16], PX_P[5], PX_P[16]);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool() {
        return this.box(0, 0, 0, 1, 0.1, 1);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool() {
        boolean empty = true;
        for (int q = 0; q < this.getSizeInventory(); q++)
        {
            if (slotHas(q))
            {
                empty = false;
                break;
            }
        }
        if (empty) {
            return this.box(PX_P[0], PX_P[0], PX_P[0], PX_P[16], PX_P[3], PX_P[16]);
        } else {
            return this.box(PX_P[0], PX_P[0], PX_P[0], PX_P[16], PX_P[5], PX_P[16]);
        }
    }
}
