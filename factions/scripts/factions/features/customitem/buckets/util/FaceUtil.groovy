package scripts.factions.features.customitem.buckets.util

import org.bukkit.Location
import org.bukkit.block.BlockFace

class FaceUtil {
    static Location faceSwitch(Location location, BlockFace blockFace)
    {
        switch (blockFace)
        {
            case blockFace.NORTH:
                location.add(0, 0, -1);
                break;

            case blockFace.EAST:
                location.add(1, 0, 0);
                break;

            case blockFace.SOUTH:
                location.add(0, 0, 1);
                break;

            case blockFace.WEST:
                location.add(-1, 0, 0);
                break;
        }
        return location;
    }

    static BlockFace faceSwitchRight(BlockFace blockFace)
    {
        switch (blockFace)
        {
            case blockFace.NORTH:
                return BlockFace.EAST;

            case blockFace.EAST:
                return BlockFace.SOUTH;

            case blockFace.SOUTH:
                return BlockFace.WEST;

            case blockFace.WEST:
                return BlockFace.NORTH;
        }

        return blockFace;
    }

    static BlockFace faceSwitchLeft(BlockFace blockFace)
    {
        switch (blockFace)
        {
            case blockFace.NORTH:
                return BlockFace.WEST;

            case blockFace.EAST:
                return BlockFace.NORTH;

            case blockFace.SOUTH:
                return BlockFace.EAST;

            case blockFace.WEST:
                return BlockFace.SOUTH;
        }

        return blockFace;
    }

}
