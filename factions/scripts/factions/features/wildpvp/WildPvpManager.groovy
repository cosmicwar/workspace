package scripts.factions.features.wildpvp

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import scripts.shared.core.cfg.utils.SelectionUtils
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.claim.Board
import scripts.factions.core.faction.claim.Claim
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.relation.RelationType
import scripts.shared.data.obj.BlockPosition
import scripts.factions.features.wildpvp.utils.MatchState
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import org.starcade.starlight.helper.scheduler.Task
import scripts.shared.utils.DataUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class WildPvpManager {

    static Map<UUID, WildPvp> ActivePvps = new ConcurrentHashMap<>()
    static Map<UUID, WildPvp> JoiningPlayers = new ConcurrentHashMap<>()
    static Map<UUID, MenuBuilder> PvpInventories = new ConcurrentHashMap<>()

    static Map<UUID, Location> lastWall = new ConcurrentHashMap<>()
    static CopyOnWriteArrayList<UUID> invinciblePlayers = new CopyOnWriteArrayList<>()

    WildPvpManager() {
        GroovyScript.addUnloadHook {
            ActivePvps.clear()
            JoiningPlayers.clear()
            PvpInventories.clear()
        }
        registerCommands()
        registerListeners()
    }


    static void registerCommands() {
        SubCommandBuilder command = new SubCommandBuilder("pvp", "wildpvp").defaultAction {player ->
            pvpMenu(player)
        }.create("create").register { cmd ->
            createWildPvp(cmd.sender())
        }.create("cancel").register {cmd ->
            leaveWildPvp(cmd.sender())
        }.build()
    }

    private static NamespacedKey wildPvpKey = new NamespacedKey(Starlight.plugin, "wildpvp")\

    static def pvpMenu(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§7Wild PvP", ActivePvps.keySet().toList(), { UUID playerId, int index ->
            Player wildPlayer = Bukkit.getPlayer(playerId)
            Member MWildPlayer = Factions.getMember(playerId)
            Faction faction = Factions.getFaction(MWildPlayer.factionId)

            String facName
            if (faction == null)
            {
                facName = "Wilderness"
            }
            else
            {
                def relation = Factions.getRelationType(Factions.getMember(player.getUniqueId()), MWildPlayer)
                facName = relation.getColor() + faction.getName()
            }

            def item = FastItemUtils.createSkull(wildPlayer, facName + " §r" + wildPlayer.getName(), ["§c ▎ §7Left click to join pvp.§7", "§c ▎ §7Right click to view inventory.§7"])

            DataUtils.setTag(item, wildPvpKey, PersistentDataType.STRING, playerId.toString())

            return item

        }, page, false, [
                { Player p, ClickType t, int slot ->
                    def item = menu.get().getItem(slot)
                    if (item == null || item.type.isAir() || !DataUtils.hasTag(item, wildPvpKey, PersistentDataType.STRING)) return

                    def playerId = UUID.fromString(DataUtils.getTag(item, wildPvpKey, PersistentDataType.STRING))

                    if (t == ClickType.LEFT) {
                        joinWildPvp(p, ActivePvps.get(playerId))
                    } else if (t == ClickType.RIGHT) {
                        PvpInventories.get(p.getUniqueId()).openSync(p)
                    }

                },
                { Player p, ClickType t, int slot ->
                    pvpMenu(p, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    pvpMenu(p, page - 1)
                },
        ])

        ItemStack pvpItem = ActivePvps.containsKey(player.getUniqueId()) ? FastItemUtils.createItem(Material.BARRIER, "§cClick to cancel your pvp.", []) : FastItemUtils.createItem(Material.SUNFLOWER, "§eClick to create a wild pvp.", [])
        menu.set(menu.get().size - 1, pvpItem, { p, t, s ->
            p = (Player) p
            if (ActivePvps.containsKey(p.getUniqueId())) leaveWildPvp(p)
            else createWildPvp(p)
        })

        menu.openSync(player)
    }

//    static def openPvpMenu(Player player) {
//        int numPvps = ActivePvps.size()
//        MenuBuilder builder
//        int rows = (numPvps + 1).intdiv(9)
//        builder = new MenuBuilder(rows == 0 ? 9 : rows * 9, "§lWild Pvp")
//        UUID[] playerUUIDs = ActivePvps.keySet().toArray() as UUID[]
//        Member playerOpen = Factions.getMember(player.getUniqueId())
//
//        for (int i = 0; i < numPvps; i++) {
//            int row = i.intdiv(9)
//            Player wildPlayer = Bukkit.getPlayer(playerUUIDs[i])
//            Member MWildPlayer = Factions.getMember(wildPlayer.getUniqueId())
//            def faction = Factions.getFaction(MWildPlayer.getFactionId())
//            if (faction == null)
//            {
//                faction = "Wilderness"
//            }
//            else
//            {
//                def relation = Factions.getRelationType(playerOpen, MWildPlayer)
//                faction = relation.getColor() + faction.getName()
//            }
//
//            builder.set(row + 1, i % 9 + 1, FastItemUtils.createSkull(wildPlayer, faction + " §r" + wildPlayer.getName(), ["§c ▎ §7Left click to join pvp.§7", "§c ▎ §7Right click to view inventory.§7"]), { p, t, s ->
//                p = (Player) p
//                t = (ClickType) t
//                s = (Integer) s
//                if (t == ClickType.LEFT) {
//                    joinWildPvp(p, ActivePvps.get(playerUUIDs[s]))
//                } else if (t == ClickType.RIGHT) {
//                    PvpInventories.get(p.getUniqueId()).openSync(p)
//                }
//            })
//        }
//        ItemStack pvpItem = ActivePvps.containsKey(player.getUniqueId()) ? FastItemUtils.createItem(Material.BARRIER, "§cClick to cancel your pvp.", []) : FastItemUtils.createItem(Material.SUNFLOWER, "§eClick to create a wild pvp.", [])
//        builder.set(rows + 1, 9, pvpItem, {p, t, s ->
//            p = (Player) p
//            if (ActivePvps.containsKey(p.getUniqueId())) leaveWildPvp(p)
//            else createWildPvp(p)
//        })
//        builder.openSync(player)
//    }

    static def createWildPvp(Player player) {
        if (ActivePvps.containsKey(player.getUniqueId())) {
            player.sendMessage("§cYou already have a wild pvp!")
            return
        }


//        PS blockPS = PS.valueOf(player.getLocation())
        BlockPosition blockPS = new BlockPosition(player.getLocation())

        Board board = Factions.getBoard(player.world)
        if (board == null) return

        Claim claim = board.getClaimAtPos(player.getLocation())
        Faction faction
        if (claim == null) {
            faction = Factions.getFaction(Factions.getWildernessId())
        } else {
            faction = Factions.getFaction(claim.factionId)
        }

//        Faction faction = BoardColl.get().getFactionAt(blockPS)
        if (faction.id != Factions.wildernessId) {
            player.sendMessage("§cYou must be in wilderness to create a wild pvp!")
            return
        }

        WildPvp pvp = new WildPvp(player)
        ActivePvps.put(player.getUniqueId(), pvp)
        PvpInventories.put(player.getUniqueId(), createInventory(player))
        BroadcastUtils.broadcast("§7${player.getName()} has created a wild pvp!")
        waitInWild(player, pvp)
    }

    static def leaveWildPvp(Player player) {
        WildPvp pvp = ActivePvps.remove(player.getUniqueId())
        WildPvp joiningPvp = JoiningPlayers.remove(player.getUniqueId())
        if (pvp != null) {
            pvp.matchState = MatchState.CANCELLED
            PvpInventories.remove(player.getUniqueId())
        } else if (joiningPvp != null) {
            joiningPvp.matchState = MatchState.CANCELLED
        }
        else player.sendMessage("§cYou must be a part of an active wild pvp to cancel it!")
    }

    static def waitInWild(Player player, WildPvp pvp) {
        Location originLocation = player.getLocation()
        Task task
        Location joiningPlayerOrigin = null
        UUID pUUID = player.getUniqueId()
        task = Schedulers.async().runRepeating({
            switch (pvp.matchState) {
                case MatchState.WAITING:
                    boolean send = keepPlayerInChunk(player, originLocation)
                    unloadPrevWall(player)
                    if (send) Schedulers.sync().run({sendWallToPlayer(player)})
                    break
                case MatchState.STARTING:
                    if (pvp.joiningPlayer != null && joiningPlayerOrigin == null) joiningPlayerOrigin = pvp.joiningPlayer.getLocation()
                    else return
                    player.sendMessage("§c§l${pvp.joiningPlayer.getName()} has joined your pvp!")
                    unloadAllWall(player)
                    doWildCountdown(player, pvp.joiningPlayer)
                    ActivePvps.remove(player.getUniqueId())
                    PvpInventories.remove(player.getUniqueId())
                    task.stop()
                    break
                case MatchState.CANCELLED:
                    unloadAllWall(player)
                    if (joiningPlayerOrigin != null) {
                        Schedulers.sync().run({})
                    }
                    task.stop()
                    break
            }
        }, 0, 5)
    }

    static def joinWildPvp(Player player, WildPvp pvp) {
        if (ActivePvps.containsKey(player.getUniqueId())) {
            player.sendMessage("§cYou cannot join a wild pvp if you have a currently active one as well!")
            return
        }
        if (pvp == null) {
            player.sendMessage("§cSorry this pvp is no longer available!")
            return
        }
        if (pvp.joiningPlayer != null || pvp.matchState != MatchState.WAITING) {
            player.sendMessage("§cSorry this pvp is no longer available!")
            return
        }

        if (isRelationAtLeast(player, pvp.owner, RelationType.TRUCE)) {
            player.sendMessage("§cYou may not accept friendly pvp requests.")
            return
        }
        pvp.joiningPlayer = player
        pvp.matchState = MatchState.STARTING
    }

    static MenuBuilder createInventory(Player player) {
        MenuBuilder builder

        builder = new MenuBuilder(45, "§e§l${player.getName()}'s Inventory")

        PlayerInventory playerInventory = player.getInventory()
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack item = playerInventory.getItem(i)
            if (item == null || item.getType() == Material.AIR) continue
            ItemStack itemClone = item.clone()
            ItemMeta itemMeta = itemClone.getItemMeta()
            itemClone.setItemMeta(itemMeta)
            builder.get().setItem(i, itemClone)
        }

        builder.set(5, 3, player.getInventory().getHelmet() ?: new ItemStack(Material.AIR))
        builder.set(5, 4, player.getInventory().getChestplate() ?: new ItemStack(Material.AIR))
        builder.set(5, 6, player.getInventory().getLeggings() ?: new ItemStack(Material.AIR))
        builder.set(5, 7, player.getInventory().getBoots() ?: new ItemStack(Material.AIR))
        builder.set(5, 1, new ItemStack(Material.RED_STAINED_GLASS_PANE))
        builder.set(5, 2, new ItemStack(Material.RED_STAINED_GLASS_PANE))
        builder.set(5, 5, new ItemStack(Material.RED_STAINED_GLASS_PANE))
        builder.set(5, 8, new ItemStack(Material.RED_STAINED_GLASS_PANE))
        builder.set(5, 9, new ItemStack(Material.RED_STAINED_GLASS_PANE))

        return builder
    }


    static void unloadPrevWall(Player player, Material unloadInto = Material.AIR) {
        BlockData wallBlockData = unloadInto.createBlockData()
        Location startLocation = lastWall.get(player.getUniqueId())
        if (startLocation == null) return
        Location bLocation = new Location(player.getWorld(), startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ())
        startLocation.add(0, -8, 0)
        for (int y = 0; y < 4; y++) {
            bLocation.set(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ())
            startLocation.add(0, 1, 0)
            for (int x = 0; x < 16; x++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(0, 0, 15)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(0, 0, 15)
                bLocation.add(1, 0, 0)
            }
            bLocation.subtract(1, 0, 0)
            for (int z = 0; z < 16; z++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(-15, 0, 0)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(-15, 0, 0)
                bLocation.add(0, 0, 1)
            }
        }
        startLocation.add(0,4,0)
        for (int y = 0; y < 14; y++) {
            bLocation.set(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ())
            startLocation.add(0, 1, 0)
            for (int x = 0; x < 16; x++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(0, 0, 15)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(0, 0, 15)
                bLocation.add(1, 0, 0)
            }
            bLocation.subtract(1, 0, 0)
            for (int z = 0; z < 16; z++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(-15, 0, 0)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(-15, 0, 0)
                bLocation.add(0, 0, 1)
            }
        }
    }


    static void sendWallToPlayer(Player player, Material wallMaterial = Material.RED_STAINED_GLASS) {
        Location pLocation = player.getLocation()
        BlockData wallBlockData = wallMaterial.createBlockData()
        Location startLocation = pLocation.subtract(pLocation.getBlockX() % 16, 0, pLocation.getBlockZ() % 16)
        if (startLocation.getBlockX() < 0) startLocation.subtract(16, 0, 0)
        if (startLocation.getBlockZ() < 0) startLocation.subtract(0, 0, 16)
        Location bLocation = new Location(player.getWorld(), startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ())
        lastWall.put(player.getUniqueId(), startLocation)

        for (int y = 0; y < 4; y++) {
            bLocation.set(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ())
            startLocation.add(0, 1, 0)
            for (int x = 0; x < 16; x++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(0, 0, 15)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(0, 0, 15)
                bLocation.add(1, 0, 0)
            }
            bLocation.subtract(1, 0, 0)
            for (int z = 0; z < 16; z++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(-15, 0, 0)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(-15, 0, 0)
                bLocation.add(0, 0, 1)
            }

        }
    }

    static void unloadAllWall(Player player, Material unloadInto = Material.AIR) {
        BlockData wallBlockData = unloadInto.createBlockData()
        Location startLocation = lastWall.get(player.getUniqueId())
        if (startLocation == null) return
        Location bLocation = new Location(player.getWorld(), startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ())
        startLocation.add(0, -8, 0)
        for (int y = 0; y < 8; y++) {
            bLocation.set(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ())
            startLocation.add(0, 1, 0)
            for (int x = 0; x < 16; x++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(0, 0, 15)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(0, 0, 15)
                bLocation.add(1, 0, 0)
            }
            bLocation.subtract(1, 0, 0)
            for (int z = 0; z < 16; z++) {
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.add(-15, 0, 0)
                if (bLocation.getBlock().getType() == Material.AIR) player.sendBlockChange(bLocation, wallBlockData)
                bLocation.subtract(-15, 0, 0)
                bLocation.add(0, 0, 1)
            }
        }
    }


    static boolean keepPlayerInChunk(Player player, Location originLocation) {
        if (player.getLocation().getChunk() != originLocation.getChunk()) {
            Schedulers.sync().call({player.teleport(originLocation)})
            player.sendMessage("§cYou may not leave the chunk you created the wild pvp in. Type /pvp cancel if you wish to leave the wild pvp queue.")
            return false
        }
        return true
    }


    static def doWildCountdown(Player wildPlayer, Player joiningPlayer) {
        invinciblePlayers.add(wildPlayer.getUniqueId())
        invinciblePlayers.add(joiningPlayer.getUniqueId())
        Schedulers.sync().run({joiningPlayer.teleport(wildPlayer)})
        int counter = 10
        Task task
        wildPlayer.sendMessage("§cType /pvp cancel to cancel the pvp.")
        joiningPlayer.sendMessage("§cType /pvp cancel to cancel the pvp, you will be teleported back to your original location.")
        task = Schedulers.async().runRepeating({
            wildPlayer.sendMessage("§cPVP will be enabled in ${counter}...")
            joiningPlayer.sendMessage("§cPVP will be enabled in ${counter}...")
            counter--
            if (counter == 0) {
                wildPlayer.sendMessage("§cPVP is now enabled.")
                joiningPlayer.sendMessage("§cPVP is now enabled.")
                invinciblePlayers.remove(wildPlayer.getUniqueId())
                invinciblePlayers.remove(joiningPlayer.getUniqueId())
                task.stop()
            }
        }, 40, 20)

    }


    static void registerListeners() {
        Events.subscribe(EntityDamageByEntityEvent.class).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler({ event ->
            if (event.getEntity() !instanceof Player) return
            if (event.getDamager() !instanceof Player) return

            Player damaged = (Player) event.getEntity()
            Player attacker = (Player) event.getDamager()

            if (invinciblePlayers.contains(damaged.getUniqueId()) || invinciblePlayers.contains(attacker.getUniqueId())) {
                event.setCancelled(true)
                return
            }
        })

        Events.subscribe(PlayerQuitEvent.class).handler({ event ->
            invinciblePlayers.remove(event.getPlayer().getUniqueId())
        })
    }

    static boolean isRelationAtLeast(Player player, Player otherPlayer, RelationType rel) {
        def member = Factions.getMember(player.getUniqueId())
        def otherMember = Factions.getMember(otherPlayer.getUniqueId())
        return Factions.getRelationType(member, otherMember).isAtLeast(rel)
    }
}
