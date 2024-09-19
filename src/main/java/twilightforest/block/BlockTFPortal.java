package twilightforest.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import twilightforest.TFAchievementPage;
import twilightforest.TFTeleporter;
import twilightforest.TwilightForestMod;

public class BlockTFPortal extends BlockBreakable {

    public BlockTFPortal() {
        super("TFPortal", Material.portal, false);
        this.setHardness(-1F);
        this.setStepSound(Block.soundTypeGlass);
        this.setLightLevel(0.75F);
        // this.setCreativeTab(TFItems.creativeTab);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
        return null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, int i, int j, int k) {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    @Override
    public IIcon getIcon(int side, int meta) {
        return Blocks.portal.getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister) {
        // don't load anything
    }

    /**
     * The function name says it all. Tries to create a portal at the specified location. In this case, the location is
     * the location of a pool with very specific parameters.
     */
    public boolean tryToCreatePortal(World world, int dx, int dy, int dz) {
        if (isGoodPortalPool(world, dx, dy, dz)) {
            world.addWeatherEffect(new EntityLightningBolt(world, dx, dy, dz));
            transmuteWaterToPortal(world, dx, dy, dz);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Changes the pool it's been given to all portal. No checks done, only does 4 squares.
     */
    public void transmuteWaterToPortal(World world, int dx, int dy, int dz) {
        int px = dx;
        int pz = dz;

        // adjust so that the other 3 water squares are in the +x, +z directions.
        if (world.getBlock(px - 1, dy, pz).getMaterial() == Material.water) {
            px--;
        }
        if (world.getBlock(px, dy, pz - 1).getMaterial() == Material.water) {
            pz--;
        }

        world.setBlock(px + 0, dy, pz + 0, TFBlocks.portal, 0, 2);
        world.setBlock(px + 1, dy, pz + 0, TFBlocks.portal, 0, 2);
        world.setBlock(px + 1, dy, pz + 1, TFBlocks.portal, 0, 2);
        world.setBlock(px + 0, dy, pz + 1, TFBlocks.portal, 0, 2);

        // System.out.println("Transmuting water to portal");
    }

    /**
     * If this spot, or a spot in any one of the 8 directions around me is good, we're good.
     */
    public static boolean isGoodPortalPool(World world, int dx, int dy, int dz) {
        boolean flag = false;

        flag |= isGoodPortalPoolStrict(world, dx + 0, dy, dz + 0);

        flag |= isGoodPortalPoolStrict(world, dx - 1, dy, dz - 1);
        flag |= isGoodPortalPoolStrict(world, dx + 0, dy, dz - 1);
        flag |= isGoodPortalPoolStrict(world, dx + 1, dy, dz - 1);

        flag |= isGoodPortalPoolStrict(world, dx - 1, dy, dz + 0);
        flag |= isGoodPortalPoolStrict(world, dx + 1, dy, dz + 0);

        flag |= isGoodPortalPoolStrict(world, dx - 1, dy, dz + 1);
        flag |= isGoodPortalPoolStrict(world, dx + 0, dy, dz + 1);
        flag |= isGoodPortalPoolStrict(world, dx + 1, dy, dz + 1);

        return flag;
    }

    /**
     * Returns true only if there is water here, and at dx + 1, dy + 1, grass surrounding it, and solid beneath.
     * 
     * 
     * GGGG G+wG GwwG GGGG
     * 
     * 
     */
    public static boolean isGoodPortalPoolStrict(World world, int dx, int dy, int dz) {
        boolean flag = true;

        // 4 squares of water
        flag &= world.getBlock(dx + 0, dy, dz + 0).getMaterial() == Material.water;
        flag &= world.getBlock(dx + 1, dy, dz + 0).getMaterial() == Material.water;
        flag &= world.getBlock(dx + 1, dy, dz + 1).getMaterial() == Material.water;
        flag &= world.getBlock(dx + 0, dy, dz + 1).getMaterial() == Material.water;

        // System.out.println("water in 4 squares = " + flag);

        // grass in the 12 squares surrounding
        flag &= isGrassOrDirt(world, dx - 1, dy, dz - 1);
        flag &= isGrassOrDirt(world, dx - 1, dy, dz + 0);
        flag &= isGrassOrDirt(world, dx - 1, dy, dz + 1);
        flag &= isGrassOrDirt(world, dx - 1, dy, dz + 2);

        flag &= isGrassOrDirt(world, dx + 0, dy, dz - 1);
        flag &= isGrassOrDirt(world, dx + 1, dy, dz - 1);

        flag &= isGrassOrDirt(world, dx + 0, dy, dz + 2);
        flag &= isGrassOrDirt(world, dx + 1, dy, dz + 2);

        flag &= isGrassOrDirt(world, dx + 2, dy, dz - 1);
        flag &= isGrassOrDirt(world, dx + 2, dy, dz + 0);
        flag &= isGrassOrDirt(world, dx + 2, dy, dz + 1);
        flag &= isGrassOrDirt(world, dx + 2, dy, dz + 2);

        // System.out.println("grass surrounding = " + flag);

        // solid underneath
        flag &= world.getBlock(dx + 0, dy - 1, dz + 0).getMaterial().isSolid();
        flag &= world.getBlock(dx + 1, dy - 1, dz + 0).getMaterial().isSolid();
        flag &= world.getBlock(dx + 1, dy - 1, dz + 1).getMaterial().isSolid();
        flag &= world.getBlock(dx + 0, dy - 1, dz + 1).getMaterial().isSolid();

        // System.out.println("solid under = " + flag);

        // 12 nature blocks above the grass?
        flag &= isNatureBlock(world, dx - 1, dy + 1, dz - 1);
        flag &= isNatureBlock(world, dx - 1, dy + 1, dz + 0);
        flag &= isNatureBlock(world, dx - 1, dy + 1, dz + 1);
        flag &= isNatureBlock(world, dx - 1, dy + 1, dz + 2);

        flag &= isNatureBlock(world, dx + 0, dy + 1, dz - 1);
        flag &= isNatureBlock(world, dx + 1, dy + 1, dz - 1);

        flag &= isNatureBlock(world, dx + 0, dy + 1, dz + 2);
        flag &= isNatureBlock(world, dx + 1, dy + 1, dz + 2);

        flag &= isNatureBlock(world, dx + 2, dy + 1, dz - 1);
        flag &= isNatureBlock(world, dx + 2, dy + 1, dz + 0);
        flag &= isNatureBlock(world, dx + 2, dy + 1, dz + 1);
        flag &= isNatureBlock(world, dx + 2, dy + 1, dz + 2);

        // System.out.println("nature blocks = " + flag);

        return flag;
    }

    /**
     * Does the block at this location count as a "nature" block for portal purposes?
     */
    public static boolean isNatureBlock(World world, int dx, int dy, int dz) {
        Material mat = world.getBlock(dx, dy, dz).getMaterial();

        if (mat == Material.plants || mat == Material.vine || mat == Material.leaves) {
            return true;
        }

        // plants = tallgrass
        // vine = flower

        return false;
    }

    /**
     * Each twilight portal pool block should have grass or dirt on one side and a portal on the other. If this is not
     * true, delete this block, presumably causing a chain reaction.
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block notUsed) {
        boolean good = true;

        if (world.getBlock(x - 1, y, z) == this) {
            good &= isGrassOrDirt(world, x + 1, y, z);
        } else if (world.getBlock(x + 1, y, z) == this) {
            good &= isGrassOrDirt(world, x - 1, y, z);
        } else {
            good = false;
        }

        if (world.getBlock(x, y, z - 1) == this) {
            good &= isGrassOrDirt(world, x, y, z + 1);
        } else if (world.getBlock(x, y, z + 1) == this) {
            good &= isGrassOrDirt(world, x, y, z - 1);
        } else {
            good = false;
        }

        // if we're not good, remove this block
        if (!good) {
            world.setBlock(x, y, z, Blocks.water, 0, 3);
        }
    }

    protected static boolean isGrassOrDirt(World world, int dx, int dy, int dz) {
        Material mat = world.getBlock(dx, dy, dz).getMaterial();
        return mat == Material.grass || mat == Material.ground;
        // grass = grass
        // ground = dirt
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {
        if (entity.ridingEntity == null && entity.riddenByEntity == null && entity.timeUntilPortal <= 0) {
            if (entity instanceof EntityPlayerMP playerMP) {

                if (playerMP.timeUntilPortal > 0) {
                    // do not switch dimensions if the player has any time on this thinger
                    playerMP.timeUntilPortal = 10;
                } else {

                    // send to twilight
                    if (playerMP.dimension != TwilightForestMod.dimensionID) {
                        playerMP.triggerAchievement(TFAchievementPage.twilightPortal);
                        // playerMP.triggerAchievement(TFAchievementPage.twilightArrival);
                        FMLLog.info(
                                "[TwilightForest] Player touched the portal block.  Sending the player to dimension "
                                        + TwilightForestMod.dimensionID);

                        playerMP.mcServer.getConfigurationManager().transferPlayerToDimension(
                                playerMP,
                                TwilightForestMod.dimensionID,
                                new TFTeleporter(
                                        playerMP.mcServer.worldServerForDimension(TwilightForestMod.dimensionID)));
                        // playerMP.addExperienceLevel(0);
                        // playerMP.triggerAchievement(TFAchievementPage.twilightPortal);
                        playerMP.triggerAchievement(TFAchievementPage.twilightArrival);

                        // set respawn point for TF dimension to near the arrival portal
                        int spawnX = MathHelper.floor_double(playerMP.posX);
                        int spawnY = MathHelper.floor_double(playerMP.posY);
                        int spawnZ = MathHelper.floor_double(playerMP.posZ);

                        playerMP.setSpawnChunk(
                                new ChunkCoordinates(spawnX, spawnY, spawnZ),
                                true,
                                TwilightForestMod.dimensionID);
                    } else {
                        // System.out.println("Player touched the portal block. Sending the player to dimension 11");
                        // playerMP.travelToDimension(11);
                        playerMP.mcServer.getConfigurationManager().transferPlayerToDimension(
                                playerMP,
                                11,
                                new TFTeleporter(playerMP.mcServer.worldServerForDimension(11)));
                        // playerMP.addExperienceLevel(0);
                    }
                }
            } else {
                if (entity.dimension != TwilightForestMod.dimensionID) {
                    // sendEntityToDimension(entity, TwilightForestMod.dimensionID);
                } else {
                    sendEntityToDimension(entity, 11);
                }
            }
        }

    }

    /**
     * This copy of the entity.travelToDimension method exists so that we can use our own teleporter
     */
    public void sendEntityToDimension(Entity entity, int dimensionID) {
        // transfer a random entity?
        if (!entity.worldObj.isRemote && !entity.isDead) {
            entity.worldObj.theProfiler.startSection("changeDimension");
            MinecraftServer minecraftserver = MinecraftServer.getServer();
            int dim = entity.dimension;
            WorldServer worldserver = minecraftserver.worldServerForDimension(dim);
            WorldServer worldserver1 = minecraftserver.worldServerForDimension(dimensionID);
            entity.dimension = dimensionID;
            entity.worldObj.removeEntity(entity);
            entity.isDead = false;
            entity.worldObj.theProfiler.startSection("reposition");
            minecraftserver.getConfigurationManager()
                    .transferEntityToWorld(entity, dim, worldserver, worldserver1, new TFTeleporter(worldserver1));
            entity.worldObj.theProfiler.endStartSection("reloading");
            Entity transferEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), worldserver1);

            if (transferEntity != null) {
                transferEntity.copyDataFrom(entity, true);
                worldserver1.spawnEntityInWorld(transferEntity);
            }

            entity.isDead = true;
            entity.worldObj.theProfiler.endSection();
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            entity.worldObj.theProfiler.endSection();
        }
    }

    @Override
    public void randomDisplayTick(World world, int i, int j, int k, Random random) {
        if (random.nextInt(100) == 0) {
            world.playSoundEffect(
                    i + 0.5D,
                    j + 0.5D,
                    k + 0.5D,
                    "portal.portal",
                    1.0F,
                    random.nextFloat() * 0.4F + 0.8F);
        }
        for (int l = 0; l < 4; l++) {
            double d = i + random.nextFloat();
            double d1 = j + random.nextFloat();
            double d2 = k + random.nextFloat();
            double d3 = 0.0D;
            double d4 = 0.0D;
            double d5 = 0.0D;
            int i1 = random.nextInt(2) * 2 - 1;
            d3 = (random.nextFloat() - 0.5D) * 0.5D;
            d4 = (random.nextFloat() - 0.5D) * 0.5D;
            d5 = (random.nextFloat() - 0.5D) * 0.5D;
            if (world.getBlock(i - 1, j, k) == this || world.getBlock(i + 1, j, k) == this) {
                d2 = k + 0.5D + 0.25D * i1;
                d5 = random.nextFloat() * 2.0F * i1;
            } else {
                d = i + 0.5D + 0.25D * i1;
                d3 = random.nextFloat() * 2.0F * i1;
            }
            world.spawnParticle("portal", d, d1, d2, d3, d4, d5);
        }

    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
        par3List.add(new ItemStack(par1, 1, 0));
    }
}
