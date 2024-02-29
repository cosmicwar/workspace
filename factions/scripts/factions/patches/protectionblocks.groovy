package scripts.factions.patches

import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.bukkit.Material
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent

Set<Material> PROTECTION_BLOCKS = [Material.EMERALD_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK]

Events.subscribe(BlockExplodeEvent.class).handler {event ->

}

Events.subscribe(EntityExplodeEvent.class).filter(EventFilters.<EntityExplodeEvent> ignoreCancelled()).handler { event ->
    event.blockList().forEach { block ->
        block.getLocation()
        if (PROTECTION_BLOCKS.contains(block.getType())) {
            event.setCancelled(true)
        }
    }

    def loc = event.getLocation()

    event.blockList().removeIf { block ->
        PROTECTION_BLOCKS.contains(block.getType())
    }
}

//public class ProtectionBlocks extends JavaPlugin implements Listener
//{
//    private static final Set<Material> PROTECTION_BLOCKS;
//
//    public void onEnable() {
//        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onPreExplode(final TNTPreExplodeEvent event) {
//        final TNTPrimed tntPrimed = event.getTntPrimed();
//        final Location location = tntPrimed.getLocation();
//        final Chunk chunk = event.getTntPrimed().getLocation().getChunk();
//        for (int y = location.getBlockY(); y > 0; --y) {
//            final Block block = chunk.getBlock(location.getBlockX() & 0xF, y, location.getBlockZ() & 0xF);
//            if (ProtectionBlocks.PROTECTION_BLOCKS.contains(block.getType())) {
//                event.setDamageBlocks(false);
//                break;
//            }
//        }
//    }
//
//    static {
//        PROTECTION_BLOCKS = Sets.newHashSet((Object[])new Material[] { Material.EMERALD_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK });
//    }
//}
