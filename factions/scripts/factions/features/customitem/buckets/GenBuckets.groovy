package scripts.factions.features.customitem.buckets

//import com.massivecraft.factions.entity.BoardColl
//import com.massivecraft.massivecore.ps.PS
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.claim.Board
import scripts.shared.data.obj.BlockPosition
import scripts.factions.features.customitem.buckets.util.GenDirection
import scripts.factions.features.customitem.buckets.util.GenUtils
import scripts.factions.features.customitem.buckets.data.GenBucketData
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.Temple
import org.starcade.starlight.helper.scheduler.Task

class GenBuckets {

    public static Map<String, ?> config

    GenBuckets() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/GenBucketListener.groovy")
            Starlight.unload("~/util/GenUtils.groovy")
        }
        Starlight.watch("~/GenBucketListener.groovy")
        Starlight.watch("~/util/GenUtils.groovy")

        reloadConfig()
        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            if (!GroovyScript.getCurrentScript().getWatchedScripts().contains(it)) return
            reloadConfig()
        })

    }

    static void reloadConfig() {
        Starlight.watch("scripts/exec/$Temple.templeId/genbucketconfig.groovy")
        config = Exports.ptr("genbucketconfig") as Map<String, ?>
    }


    static Map<String, ?> getConfig() {
        return config == null ? config = Exports.ptr("genbucketconfig") as Map<String, ?> : config
    }


    static def verticalGenTask(GenBucketData data, BlockPosition blockPS, boolean reverse = false) {
        def location = blockPS.getLocation()
        BlockFace direction = reverse ? BlockFace.DOWN : BlockFace.UP
        World world = Bukkit.getWorld(blockPS.world)

        Task task

        task = Schedulers.sync().runRepeating({
            if (location.world.getBlockAt(location).type != Material.AIR) {
                task.stop()
                return
            }
            if (direction == BlockFace.UP) {
                if (location.getBlockY() > world.getMaxHeight()) {
                    task.stop()
                    return
                }
                location.world.setBlockData(location, data.getMaterial().createBlockData())
                location.add(0, 1, 0)
            } else {
                location.world.setBlockData(location, data.getMaterial().createBlockData())
                location.subtract(0, 1, 0)
            }
        }, 0, 7L)
    }

    static def horizontalGenTask(GenBucketData data, BlockPosition blockPS, BlockFace direction) {
        int blockCount = 0
        int limit = getConfig()["horizontallimit"] as int
        def location = blockPS.getLocation()
        Map<String, Vector> blockFaceToVector = getConfig()["blockfacetovector"] as Map<String, Vector>
        BlockPosition originPS = blockPS
        Chunk prevChunk = null

        Task task

        task = Schedulers.sync().runRepeating({
            if (prevChunk == null || !prevChunk.equals(location.getChunk())) {
                prevChunk = location.getChunk()
                blockPS.valueOf(location)

                Board board = Factions.getBoard(Bukkit.getWorld(blockPS.world))

                if (board == null) return
                if (board.getClaimAtPos(blockPS.getLocation()) == null) {
                    task.stop()
                    return
                }
                if (board.getClaimAtPos(blockPS.getLocation()).getFactionId() != board.getClaimAtPos(originPS.getLocation()).getFactionId()) {
                    task.stop()
                    return
                }
            }
            if (location.world.getBlockAt(location).type != Material.AIR || blockCount > limit) {
                task.stop()
                return
            }
            location.world.setBlockData(location, data.getMaterial().createBlockData())
            location.add(blockFaceToVector.get(direction.toString().toLowerCase()))
            blockCount++

        }, 0, 7L)
    }

    static def openGenShopMenu(Player player) {
        MenuBuilder builder

        builder = new MenuBuilder(3 * 9, "§3Gen Shop")
        MenuDecorator.decorate(builder, [
                "383838383",
                "838383838",
                "383838383"
        ])

        builder.set(1, 5, FastItemUtils.createItem(Material.LAVA_BUCKET, "§lVertical Lava Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["vertical"]["lava"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.VERTICAL, Material.LAVA))
            p.updateInventory()
        })

        builder.set(2, 2, FastItemUtils.createItem(Material.COBBLESTONE, "§lVertical Coblestone Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["vertical"]["cobblestone"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.VERTICAL, Material.COBBLESTONE))
            p.updateInventory()
        })

        builder.set(2, 3, FastItemUtils.createItem(Material.COBBLESTONE, "§lHorizontal Coblestone Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["horizontal"]["cobblestone"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.HORIZONTAL, Material.COBBLESTONE))
            p.updateInventory()
        })

        builder.set(2, 4, FastItemUtils.createItem(Material.OBSIDIAN, "§lVertical Obsidian Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["vertical"]["obsidian"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.VERTICAL, Material.OBSIDIAN))
            p.updateInventory()
        })

        builder.set(2, 5, FastItemUtils.createItem(Material.OBSIDIAN, "§lHorizontal Obsidian Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["horizontal"]["obsidian"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.HORIZONTAL, Material.OBSIDIAN))
            p.updateInventory()
        })

        builder.set(2, 6, FastItemUtils.createItem(Material.NETHERRACK, "§lVertical Netherrack Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["vertical"]["netherrack"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.VERTICAL, Material.NETHERRACK))
            p.updateInventory()
        })

        builder.set(2, 7, FastItemUtils.createItem(Material.NETHERRACK, "§lHorizontal Netherrack Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["horizontal"]["netherrack"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.HORIZONTAL, Material.NETHERRACK))
            p.updateInventory()
        })

        builder.set(2, 8, FastItemUtils.createItem(Material.SAND, "§lVertical Sand Gen", ["§c ▎ §7Price§7: §c${getConfig()["direction"]["vertical"]["sand"]["cost"] as String}"]), { p, t, s ->
            p = (Player) p
            p.getInventory().addItem(GenUtils.createGenBucket(GenDirection.VERTICAL, Material.SAND))
            p.updateInventory()
        })



        builder.set(3, 5, FastItemUtils.createItem(Material.BARRIER, "§cClose Menu", []), { p, t, s ->
            p.closeInventory()
        })

        builder.openSync(player)
    }
}

