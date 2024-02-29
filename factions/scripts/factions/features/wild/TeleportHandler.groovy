package scripts.factions.features.wild

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.starcade.starlight.helper.utils.Players

class TeleportHandler {
    private final Random random;
    private final Player player;
    private final World world;
    private int xCoord;
    private int zCoord;
    private int xF;
    private int yF;
    private int zF;

    public TeleportHandler(final Player player, final World world, final int xCoord, final int zCoord) {
        this.xCoord = -1;
        this.zCoord = -1;
        this.player = player;
        this.world = world;
        this.xCoord = xCoord;
        this.zCoord = zCoord;
        random = new Random()
    }

    public void teleport() {
        final Location location = this.getLocation()
        if (location == null) {
            player.sendMessage("§] §> §4§l(!) §4Failed to find a safe teleport location!")
            return
        }
        Players.msg(player, "§] §> §aTeleported to a random location!")
        this.player.teleport(location)
    }

    private void set(final double x, final double y, final double z) {
        this.xF = (int)x;
        this.yF = (int)y;
        this.zF = (int)z;
    }

    protected Location getLocation() {
        int x = random.nextInt(this.xCoord);
        int z = random.nextInt(this.zCoord);
        x = this.randomizeType(x);
        z = this.randomizeType(z);
        final int y = 63
//        if (new Location(this.world, (double)x, (double)y, (double)z))
        Location location = TeleportUtils.safeizeLocation(new Location(this.world, (double)x, (double)y, (double)z));
        if (location == null) {
            return null;
        }
        this.set(location.getX(), location.getY(), location.getZ());
        return location;
    }

    protected int randomizeType(final int i) {
        final int j = random.nextInt(2);
        switch (j) {
            case 0: {
                return -i;
            }
            case 1: {
                return i;
            }
            default: {
                return -1;
            }
        }
    }
}
