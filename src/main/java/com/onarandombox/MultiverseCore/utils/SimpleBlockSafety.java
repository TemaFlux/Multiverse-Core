/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.BlockSafety;
import com.onarandombox.MultiverseCore.api.Core;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The default-implementation of {@link BlockSafety}.
 */
public class SimpleBlockSafety implements BlockSafety {
    private final Core plugin;
    private static final Set<BlockFace> AROUND_BLOCK = EnumSet.noneOf(BlockFace.class);

    static {
        AROUND_BLOCK.add(BlockFace.NORTH);
        AROUND_BLOCK.add(BlockFace.NORTH_EAST);
        AROUND_BLOCK.add(BlockFace.EAST);
        AROUND_BLOCK.add(BlockFace.SOUTH_EAST);
        AROUND_BLOCK.add(BlockFace.SOUTH);
        AROUND_BLOCK.add(BlockFace.SOUTH_WEST);
        AROUND_BLOCK.add(BlockFace.WEST);
        AROUND_BLOCK.add(BlockFace.NORTH_WEST);
    }

    public SimpleBlockSafety(Core plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlockAboveAir(Location l) {
        Location downOne = l.clone();
        downOne.setY(downOne.getY() - 1);
        return (downOne.getBlock().getType() == Material.AIR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean playerCanSpawnHereSafely(World world, double x, double y, double z) {
        Location l = new Location(world, x, y, z);
        return playerCanSpawnHereSafely(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean playerCanSpawnHereSafely(Location l) {
        if (l == null) {
            // Can't safely spawn at a null location!
            return false;
        }

        World world = l.getWorld();
        Location actual = l.clone();
        Location upOne = l.clone();
        Location downOne = l.clone();
        upOne.setY(upOne.getY() + 1);
        downOne.setY(downOne.getY() - 1);

        if (isSolidBlock(world.getBlockAt(actual).getType())
                || isSolidBlock(upOne.getBlock().getType())) {
            Logging.finer("Error Here (Actual)? (%s)[%s]", actual.getBlock().getType(),
                    isSolidBlock(actual.getBlock().getType()));
            Logging.finer("Error Here (upOne)? (%s)[%s]", upOne.getBlock().getType(),
                    isSolidBlock(upOne.getBlock().getType()));
            return false;
        }

        if (downOne.getBlock().getType() == Material.LAVA) {
            Logging.finer("Error Here (downOne)? (%s)[%s]", downOne.getBlock().getType(), isSolidBlock(downOne.getBlock().getType()));
            return false;
        }

        if (downOne.getBlock().getType() == Material.FIRE) {
            Logging.finer("There's fire below! (%s)[%s]", actual.getBlock().getType(), isSolidBlock(actual.getBlock().getType()));
            return false;
        }

        if (isBlockAboveAir(actual)) {
            Logging.finer("Is block above air [%s]", isBlockAboveAir(actual));
            Logging.finer("Has 2 blocks of water below [%s]", this.hasTwoBlocksofWaterBelow(actual));
            return this.hasTwoBlocksofWaterBelow(actual);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getSafeBedSpawn(Location l) {
        // The passed location, may be null (if the bed is invalid)
        if (l == null) {
            return null;
        }
        final Location trySpawn = this.getSafeSpawnAroundABlock(l);
        if (trySpawn != null) {
            return trySpawn;
        }
        Location otherBlock = this.findOtherBedPiece(l);
        if (otherBlock == null) {
            return null;
        }
        // Now we have 2 locations, check around each, if the type is bed, skip it.
        return this.getSafeSpawnAroundABlock(otherBlock);
    }

    /**
     * Find a safe spawn around a location. (N,S,E,W,NE,NW,SE,SW)
     * @param l Location to check around
     * @return A safe location, or none if it wasn't found.
     */
    private Location getSafeSpawnAroundABlock(Location l) {
        Iterator<BlockFace> checkblock = AROUND_BLOCK.iterator();
        while (checkblock.hasNext()) {
            final BlockFace face = checkblock.next();
            if (this.playerCanSpawnHereSafely(l.getBlock().getRelative(face).getLocation())) {
                // Don't forget to center the player.
                return l.getBlock().getRelative(face).getLocation().add(.5, 0, .5);
            }
        }
        return null;
    }

    /**
     * Find the other bed block.
     * @param checkLoc The location to check for the other piece at
     * @return The location of the other bed piece, or null if it was a jacked up bed.
     */
    private Location findOtherBedPiece(Location checkLoc) {
        Block data = checkLoc.getBlock();
        if (!(data instanceof Bed)) {
            return null;
        }
        Bed b = (Bed) data;

//        if (b.getPart() == Bed.Part.HEAD) {
//            return checkLoc.getBlock().getRelative(b.getFacing().getOppositeFace()).getLocation();
//        }
        // We shouldn't ever be looking at the foot, but here's the code for it.
        return checkLoc.getBlock().getRelative(checkLoc.getBlock().getFace(b.getBlock())).getLocation();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Location getTopBlock(Location l) {
        Location check = l.clone();
        check.setY(127); // SUPPRESS CHECKSTYLE: MagicNumberCheck
        while (check.getY() > 0) {
            if (this.playerCanSpawnHereSafely(check)) {
                return check;
            }
            check.setY(check.getY() - 1);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getBottomBlock(Location l) {
        Location check = l.clone();
        check.setY(0);
        while (check.getY() < 127) { // SUPPRESS CHECKSTYLE: MagicNumberCheck
            if (this.playerCanSpawnHereSafely(check)) {
                return check;
            }
            check.setY(check.getY() + 1);
        }
        return null;
    }

    /*
     * If someone has a better way of this... Please either tell us, or submit a pull request!
     */
    public static boolean isSolidBlock(Material type) {
        return type.isSolid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEntitiyOnTrack(Location l) {
        Material currentBlock = l.getBlock().getType();
        return (currentBlock == Material.POWERED_RAIL
                || currentBlock == Material.DETECTOR_RAIL
                || currentBlock.name().equalsIgnoreCase("RAIL")
                || currentBlock.name().equalsIgnoreCase("RAILS")
                || currentBlock == Material.ACTIVATOR_RAIL);
    }

    /**
     * Checks recursively below a {@link Location} for 2 blocks of water.
     *
     * @param l The {@link Location}
     * @return Whether there are 2 blocks of water
     */
    private boolean hasTwoBlocksofWaterBelow(Location l) {
        if (l.getBlockY() < 0) {
            return false;
        }
        Location oneBelow = l.clone();
        oneBelow.subtract(0, 1, 0);
        if (oneBelow.getBlock().getType() == Material.WATER) {
            Location twoBelow = oneBelow.clone();
            twoBelow.subtract(0, 1, 0);
            return (oneBelow.getBlock().getType() == Material.WATER);
        }
        if (oneBelow.getBlock().getType() != Material.AIR) {
            return false;
        }
        return hasTwoBlocksofWaterBelow(oneBelow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSpawnCartSafely(Minecart cart) {
        if (this.isBlockAboveAir(cart.getLocation())) {
            return true;
        }
        if (this.isEntitiyOnTrack(plugin.getLocationManipulation().getNextBlock(cart))) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canSpawnVehicleSafely(Vehicle vehicle) {
        if (this.isBlockAboveAir(vehicle.getLocation())) {
            return true;
        }
        return false;
    }

}
