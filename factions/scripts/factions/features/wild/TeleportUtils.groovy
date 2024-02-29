package scripts.factions.features.wild

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block

final class TeleportUtils {
    private static final Set<Material> UNSAFE_MATERIALS
    public static final int RADIUS = 3
    public static final Vector3D[] VOLUME

    private TeleportUtils() {
    }

    static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
        return y > world.getMaxHeight() || !world.getBlockAt(x, y - 1, z).getType().isSolid()
    }

    static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
        final Block block = world.getBlockAt(x, y, z)
        final Block below = world.getBlockAt(x, y - 1, z)
        final Block above = world.getBlockAt(x, y + 1, z)
        return UNSAFE_MATERIALS.contains(below.getType()) || block.getType().isSolid() || above.getType().isSolid() || isBlockAboveAir(world, x, y, z)
    }

    static Location safeizeLocation(final Location location) {
        final World world = location.getWorld()
        int x = location.getBlockX()
        int y = (int) location.getY()
        int z = location.getBlockZ()
        final int origX = x
        final int origY = y
        final int origZ = z
        location.setY((double) location.getWorld().getHighestBlockYAt(location))
        while (isBlockAboveAir(world, x, y, z)) {
            if (--y < 0) {
                y = origY
                break
            }
        }
        if (isBlockUnsafe(world, x, y, z)) {
            x = ((Math.round(location.getX()) == origX) ? (x - 1) : (x + 1))
            z = ((Math.round(location.getZ()) == origZ) ? (z - 1) : (z + 1))
        }
        for (int i = 0; isBlockUnsafe(world, x, y, z); x = origX + VOLUME[i].x, y = origY + VOLUME[i].y, z = origZ + VOLUME[i].z) {
            if (++i >= VOLUME.length) {
                x = origX
                y = origY + 3
                z = origZ
                break
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            if (++y >= world.getMaxHeight()) {
                ++x
                break
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            if (--y <= 1) {
                ++x
                y = world.getHighestBlockYAt(x, z)
                if (x - 48 > location.getBlockX()) {
                    return null
                }
            }
        }
        return new Location(world, x + 0.5, (double) y, z + 0.5, location.getYaw(), location.getPitch())
    }

    static {
        (UNSAFE_MATERIALS = new HashSet<Material>()).add(Material.LAVA)
        UNSAFE_MATERIALS.add(Material.LEGACY_STATIONARY_LAVA)
        UNSAFE_MATERIALS.add(Material.FIRE)
        final List<Vector3D> pos = new ArrayList<Vector3D>()
        for (int x = -3; x <= 3; ++x) {
            for (int y = -3; y <= 3; ++y) {
                for (int z = -3; z <= 3; ++z) {
                    pos.add(new Vector3D(x, y, z))
                }
            }
        }
        Collections.sort(pos, new Comparator<Vector3D>() {
            @Override
            int compare(final Vector3D a, final Vector3D b) {
                return a.x * a.x + a.y * a.y + a.z * a.z - (b.x * b.x + b.y * b.y + b.z * b.z)
            }
        })
        VOLUME = pos.toArray(new Vector3D[0])
    }

    static class Vector3D {
        public int x
        public int y
        public int z

        Vector3D(final int x, final int y, final int z) {
            this.x = x
            this.y = y
            this.z = z
        }
    }
}