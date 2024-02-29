package scripts.factions.core.faction.addon.fbanner

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.FactionUtils
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.data.obj.Position
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.utils.DataUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class FBanner {

    static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "fBannerData")
    static Map<Block, Faction> activeBanners = new ConcurrentHashMap<>()

    FBanner() {

        GroovyScript.addUnloadHook {
            Factions.fCommand.subCommands.removeIf { it.aliases.find {
                it.equalsIgnoreCase("banner")
            } != null }
            Factions.fCommand.build()
        }

        events()
        commands()
    }

    static void events() {
        Events.subscribe(BlockPlaceEvent.class).handler({ event ->
            if (event.blockPlaced.getType() != Material.BLACK_BANNER) return
            if (!DataUtils.hasTag(event.itemInHand, DATA_KEY, PersistentDataType.BYTE)) return

            Player player = event.player
            Member p = Factions.getMember(player.getUniqueId())
            if (p.getFactionId() == null || p.getFactionId() == Factions.wildernessId) {
                player.sendMessage("§cYou must be in a faction to place a faction banner.")
                return
            }
            if (event.getBlockReplacedState().getType() != Material.AIR) {
                player.sendMessage("§cYou may not place a banner there.")
                event.setCancelled(true)
                return
            }
            Faction faction = Factions.getFaction(p.factionId, false)
            Location bl = event.blockPlaced.location
            faction.fBanner = Position.of(bl)
            activeBanners.put(event.blockPlaced, faction)
            faction.msg("§r§7\n----------------------------------------------------\n §7§l${player.name} has placed a faction banner at §r§5§l${bl.blockX}, ${bl.blockZ}! \n §r§5§oType /f assist to teleport to the placed banner.\n§r§7----------------------------------------------------\n ")

            Schedulers.sync().runLater({
                if (faction.fBanner == Position.of(bl)) faction.fBanner = null
                if (activeBanners.remove(event.blockPlaced) != null) event.blockPlaced.setType(Material.AIR)
            }, 15, TimeUnit.SECONDS)
        })

        Events.subscribe(BlockBreakEvent.class).handler({ event ->
            Block block = event.block

            if (block == null) return
            if (block.getType() != Material.BLACK_BANNER) return
            if (!activeBanners.containsKey(block)) return

            activeBanners.get(block).fBanner = null
            activeBanners.remove(block)
        })
    }

    static void commands() {
        FCBuilder fCommand = Factions.fCommand

        fCommand.create("banner").description("Gives you a faction banner.").register { cmd ->
            Player player = cmd.sender()
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                player.getInventory().addItem(createBanner())
            }
        }

        fCommand.create("assist").description("Teleport to a placed faction banner.").register { cmd ->
            Player player = cmd.sender()
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (faction.fBanner == null) {
                    player.sendMessage("§cYour faction has no active banner placed.")
                    return
                }

                FactionUtils.teleportPlayer(player, faction.fBanner.getLocation(null))
            }
        }
    }

    static ItemStack createBanner() {
        ItemStack banner = FastItemUtils.createItem(Material.BLACK_BANNER, "§5§lFaction Banner", ["§dPlace this banner to allow your faction members to teleport to", "§dyour banner's location by typing /f assist.", "", "This banner can be destroyed to cancel teleportation."])
        DataUtils.setTagByte(banner, DATA_KEY, 0 as byte)
        return banner
    }
}
