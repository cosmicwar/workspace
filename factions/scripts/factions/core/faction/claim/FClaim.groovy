package scripts.factions.core.faction.claim

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.jodah.expiringmap.ExpiringMap
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.command.context.PlayerContext
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.ftop.FTEntryType
import scripts.factions.core.faction.addon.ftop.FTopUtils
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.core.faction.perm.perms.cmd.ClaimPerm
import scripts.factions.data.obj.CL
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.factions.data.uuid.UUIDDataManager
import scripts.factions.features.spawners.CustomSpawners
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

import java.util.concurrent.TimeUnit

/*
  ~ f claim ~
*/
@CompileStatic(TypeCheckingMode.SKIP)
class FClaim {

    static Map<UUID, Long> placingCore = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).build()

    FClaim() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getAllData(Faction).each {
                if (it.coreChunkData != null) {
                    it.coreChunkData.destroyHologram()
                }
            }

            Factions?.fCommand?.subCommands?.removeIf { it.aliases.find {
                it.equalsIgnoreCase("claim") || it.equalsIgnoreCase("unclaim") || it.equalsIgnoreCase("unclaimall") || it.equalsIgnoreCase("map") || it.equalsIgnoreCase("claimsafezone") || it.equalsIgnoreCase("unclaimsafezone") || it.equalsIgnoreCase("claimcore")
            } != null }
            Factions?.fCommand?.build()
        }

        Schedulers.async().runLater({
            UUIDDataManager.getAllData(Faction).each {
                if (it.coreChunkData != null) {
                    it.coreChunkData.spawnHologram()
                }
            }
        }, 5L)

        commands()
        events()
    }

    static def events() {
        Events.subscribe(BlockPlaceEvent.class).handler {event ->
            def player = event.getPlayer()
            def item = event.getItemInHand()

            if (item == null || item.getType().isAir()) return

            def member = Factions.getMember(player.getUniqueId())

            if (member.factionId == null) return

            def faction = Factions.getFaction(member.factionId, false)
            if (faction == null) return

            if (placingCore.containsKey(player.getUniqueId())) {
                if (faction.coreChunkData != null) {
                    def coreChunk = faction.coreChunkData.chunkLocation
                    def factionAt = Factions.getFactionAt(coreChunk)
                    if (factionAt != null && factionAt.getId() == faction.getId()) {
                        Players.msg(player, "§] §> §cYou already have a core chunk claimed.")
                        return
                    }
                }

                def cl = CL.of(event.getBlockPlaced().getLocation())
                def blockPos = new Position(
                        event.getBlockPlaced().getLocation().world.name,
                        event.getBlockPlaced().getLocation().x() as int,
                        event.getBlockPlaced().getLocation().y() as int,
                        event.getBlockPlaced().getLocation().z() as int
                )

                def factionAt = Factions.getFactionAt(cl)
                if (factionAt == null || factionAt.getId() != faction.getId()) {
                    Players.msg(player, "§] §> §cYou can only claim core chunks in your own faction territory.")
                    return
                }

                def claim = faction.claims.find { (it.getLocation() == cl) }
                if (claim == null || claim.getFactionId() != factionAt.getId()) {
                    Players.msg(player, "§] §> §cYou can only claim core chunks in your own faction territory.")
                    return
                }

                event.setCancelled(true)

                claim.coreChunk = true

                def data = new CoreChunkData(faction.id, cl, blockPos)
                faction.coreChunkData = data
                faction.queueSave()

                Players.msg(player, "§3You have claimed this chunk as a core chunk.")
                Players.msg(player, "${blockPos.x} ${blockPos.y} ${blockPos.z} ${blockPos.world}")

                def world = event.getBlockPlaced().world
                def block = world.getBlockAt(blockPos.x, blockPos.y, blockPos.z)
                Schedulers.sync().runLater({
                    if (block.type != Material.ENDER_CHEST) block.setType(Material.ENDER_CHEST)
                    data.spawnHologram()
                }, 2L)

                placingCore.remove(player.getUniqueId())
            }
        }

        Events.subscribe(PlayerInteractEvent.class).handler {event ->
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return
            def block = event.getClickedBlock()

            if (block == null || block.getType() != Material.ENDER_CHEST) return

            def player = event.player

            def factionAt = Factions.getFactionAt(CL.of(block.getLocation()))
            if (factionAt == null || factionAt.coreChunkData == null) return

            def coreChunk = factionAt.coreChunkData
            if (coreChunk.blockPosition.x != block.getX() || coreChunk.blockPosition.y != block.getY() || coreChunk.blockPosition.z != block.getZ()) return

            event.setCancelled(true)

            def member = Factions.getMember(player.getUniqueId())
            if (member.role != Role.ADMIN) {
                if (member.factionId == null || member.factionId != factionAt.id) {
                    Players.msg(player, "§] §> §cThis is not your faction!")
                    return
                }

                if (!member.isRoleAtleast(Role.COLEADER)) {
                    Players.msg(player, "§] §> §cYou must be a co-leader to edit this!")
                    return
                }
            }

            openGui(player, factionAt)
            Players.playSound(player, Sound.BLOCK_CHEST_OPEN)
        }
    }

    static def openGui(Player player, Faction faction) {
        player.sendMessage("inside gui")
        MenuBuilder menu = new MenuBuilder(27, "§e${faction.name} - Core Chunk Options")

        MenuDecorator.decorate(menu, [
                "9e9e9e9e9",
                "e9e9e9e9e",
                "9e9e9e9e9"
        ])

        menu.set(2, 3, new ItemStack(Material.AIR))
        menu.set(2, 4, new ItemStack(Material.AIR))
        menu.set(2, 5, new ItemStack(Material.AIR))
        menu.set(2, 6, new ItemStack(Material.AIR))
        menu.set(2, 7, new ItemStack(Material.AIR))

        menu.setCloseCallback { p ->
            Players.playSound(p, Sound.BLOCK_CHEST_CLOSE)
            faction.queueSave()
        }

        menu.openSync(player)
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        fCommand.create("claim").description("Claim an Area").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (!ClaimPerm.canAccess(faction, member)) {
                    cmd.reply("§] §> §cYou do not have permission to claim areas.")
                    return
                }

                CL location = CL.of(cmd.sender().getLocation())

                if (cmd.args().size() == 0) {
                    // single claim
                    tryClaim(member, location)
                    return
                }

                if (cmd.args().size() == 1) {
                    Integer radius = cmd.arg(0).parseOrFail(Integer)
                    tryClaim(member, location, radius)
                    return
                }

//                if (cmd.args().size() == 2) {
//                    String type = cmd.arg(0).parseOrFail(String)
//                    Integer radius = cmd.arg(1).parseOrFail(Integer)
//                }
            }
        }

        fCommand.create("claimsafezone").description("Claim an Area").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) {member ->
                if (!cmd.sender().isOp()) {
                    cmd.reply("§] §> §cYou do not have permission to this claim area.")
                    return
                }

                SR selection = SelectionUtils.getSelection(cmd.sender())
                if (selection == null) {
                    cmd.reply("§] §> §cYou do not have a selection.")
                    return
                }

                def safezone = Factions.getFaction(Factions.safeZoneId, false)
                if (safezone == null) {
                    cmd.reply("§] §> §cSafe-Zone does not exist.")
                    return
                }

                if (tryClaimRegion(safezone, selection)) {
                    cmd.reply("§3You have claimed a §eRegion §3for the safe-zone.")
                } else {
                    cmd.reply("§3This region is already claimed.")
                }
            }
        }

        fCommand.create("unclaimsafezone").register {cmd ->
            FCommandUtil.memberFromCommand(cmd) {member ->
                if (!cmd.sender().isOp()) {
                    cmd.reply("§] §> §cYou do not have permission to this claim area.")
                    return
                }

                def safezone = Factions.getFaction(Factions.safeZoneId, false)
                if (safezone == null) {
                    cmd.reply("§] §> §cSafe-Zone does not exist.")
                    return
                }

                if (safezone.getClaims().isEmpty()) {
                    cmd.reply("§3Safe-Zone does not have any claims.")
                    return
                }

                int size = safezone.getClaims().size()
                safezone.getClaims().each { claim ->
                    Factions.getBoard(claim.getRegion().world) { board ->
                        board.regionClaims.remove(claim.getRegion())
                    }
                }

                safezone.getClaims().clear()
                safezone.queueSave()

                cmd.reply("§3You have unclaimed §e${size} §3claims.")
            }
        }

        fCommand.create("unclaim").description("Unclaim an Area").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (!member.isRoleAtleast(Role.OFFICER)) {
                    cmd.reply("§] §> §cYou do not have permission to claim areas.")
                    return
                }
                CL location = CL.of(cmd.sender().getLocation())

                if (cmd.args().isEmpty()) {
                    // single claim
                    tryUnClaim(member, location)
                    return
                }

                if (cmd.args().size() == 1) {
                    Integer radius = cmd.arg(0).parseOrFail(Integer)
                    tryUnClaim(member, location, radius)
                    return
                }
            }
        }

        fCommand.create("unclaimall").description("Unclaim all Areas").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (!member.isRoleAtleast(Role.COLEADER)) {
                    cmd.reply("§] §> §cYou do not have permission to claim areas.")
                    return
                }

                if (faction.systemFactionData != null) {
                    cmd.reply("§3You cannot unclaim areas in a system faction.")
                    return
                }

                if (faction.getClaims().isEmpty()) {
                    cmd.reply("§3Your faction does not have any claims.")
                    return
                }

                int size = faction.getClaims().size()
                faction.getClaims().each { claim ->
                    Factions.getBoard(claim.getLocation().worldName) { board ->
                        board.chunkClaims.remove(claim.getLocation())
                    }
                }

                faction.getClaims().clear()
                faction.queueSave()

                cmd.reply("§3You have unclaimed §e${size} §3claims.")
            }
        }

        fCommand.create("map").description("Shows Faction Claim Map").register { cmd ->
            showMap(cmd, CL.of(cmd.sender().getLocation()))
        }

        fCommand.create("claimcore").description("Starts the process to claim core chunk.").register {cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) {faction, member ->
                if (!member.isRoleAtleast(Role.COLEADER)) {
                    cmd.reply("§] §> §cYou do not have permission to claim areas.")
                    return
                }

                if (faction.systemFactionData != null) {
                    cmd.reply("§3You cannot claim core chunks in a system faction.")
                    return
                }

                if (placingCore.containsKey(member.getId())) {
                    placingCore.remove(member.getId())
                    cmd.reply("§3You have stopped the process of claiming a core chunk.")
                    return
                }

                placingCore.put(member.getId(), System.currentTimeMillis())

                def titleBar = "§3§m" + StringUtils.repeat("-", 15) + "§8 [ §eClaim Core §8] §3§m" + StringUtils.repeat("-", 15)

                cmd.reply(titleBar)
                cmd.reply("§3You have started the process of claiming a core chunk.")
                cmd.reply("")
                cmd.reply("§3Right click on a chunk with a block to claim it as a core chunk.")
                cmd.reply("§3You can only claim core chunks in your own faction territory.")

                cmd.reply("§3§m" + titleBar.replaceAll(".", "-"))
            }
        }

        fCommand.build()
    }

    // TODO: add support for different shape claims
    static synchronized boolean tryClaim(Member member, CL cl, int radius = 0) { // presuming perm check is already done
        def player = Bukkit.getPlayer(member.getId())
        if (player == null) return false

        def faction = Factions.getFaction(member.getFactionId(), false)
        if (faction == null) return false
        if (faction.systemFactionData != null) {
            if (faction.name.equalsIgnoreCase("wilderness")) return false
        }
        if (radius == 0) {
            if (claimChunk(faction, cl)) {
                Players.msg(player, "§3You have claimed this chunk for your faction.")
                return true
            } else {
                Players.msg(player, "§3This chunk is already claimed.")
                return false
            }
        } else {
            if (radius >= 10 || radius <= 0) {
                radius == 1
            }

            int startNeg = 0
            int startPos = 0

            for (int i = 0; i < radius; i++) {
                startNeg -= 1
                startPos += 1
            }

            int chunksAttempted = 0
            int chunksClaimed = 0
            for (int x = startNeg; x <= startPos; x++) {
                for (int y = startNeg; y <= startPos; y++) {
                    CL newCl = new CL(cl.worldName, cl.getX() + x, cl.getZ() + y)

                    if (claimChunk(faction, newCl))
                    {
                        int bukkitX = newCl.getX() << 4
                        int bukkitZ = newCl.getZ() << 4

                        chunksClaimed++
                    }
                    chunksAttempted++
                }
            }
            faction.msg("§] §> §3${member.getName()} §3has claimed §e${chunksClaimed} ${chunksClaimed > 1 ? "chunks" : "a chunk"} at §a${player.location.chunk.x}§7,§a${player.location.chunk.z} §3for your faction.")
            Players.msg(player, "§3You have claimed §e${chunksClaimed}§3/${chunksAttempted} chunks.")

            return true

            // Spiral Algo?
        }
    }

    static synchronized boolean tryUnClaim(Member member, CL cl, int radius = 0) {
        def player = Bukkit.getPlayer(member.getId())
        if (player == null) return false

        def faction = Factions.getFaction(member.getFactionId(), false)
        if (faction == null) return false

        if (faction.systemFactionData != null) {
            if (faction.name.equalsIgnoreCase("wilderness")) return false
        }

        if (radius == 0) {
            int bukkitX = cl.getX() << 4
            int bukkitZ = cl.getZ() << 4

            if (unClaimChunk(faction, cl)) {
                faction.msg("§3${member.getName()} §3has unclaimed §e${bukkitX}§7,§e${bukkitZ} §3for your faction.")
                return true
            } else {
                Players.msg(player, "§3This chunk isn't claimed.")
                return false
            }
        } else {
            if (radius >= 10 || radius <= 0) {
                radius == 1
            }

            int startNeg = 0
            int startPos = 0

            for (int i = 0; i < radius; i++) {
                startNeg -= 1
                startPos += 1
            }

            // 3x3 chunk iteration?
            int chunksAttempted = 0
            int chunksUnClaimed = 0
            for (int x = startNeg; x <= startPos; x++) {
                for (int y = startNeg; y <= startPos; y++) {
                    CL newCl = new CL(cl.worldName, cl.getX() + x, cl.getZ() + y)

                    if (unClaimChunk(faction, newCl)) {
                        int bukkitX = newCl.getX() << 4
                        int bukkitZ = newCl.getZ() << 4

                        chunksUnClaimed++
                    }

                    chunksAttempted++
                }
            }
            faction.msg("§] §> §3${member.getName()} §3has claimed §e${chunksUnClaimed} ${chunksUnClaimed > 1 ? "chunks" : "a chunk"} at §a${player.location.chunk.x}§7,§a${player.location.chunk.z} §3for your faction.")
            Players.msg(player, "§3You have unclaimed §e${chunksUnClaimed}§3/${chunksAttempted} chunks.")

            return true

            // Spiral Algo?
        }
    }

    static synchronized boolean claimChunk(Faction faction, CL cl) {
        def board = Factions.getBoardSync(cl.worldName)
        if (board == null) board = Factions.boardCache.computeIfAbsent(cl.worldName, { new Board(cl.worldName) })
        def claim = board.chunkClaims.get(cl)
        if (claim != null && claim.getFactionId() != Factions.wildernessId) return false
        claim = new Claim(faction.getId(), cl)

        board.addChunkClaim(cl, claim)
        faction.claims.add(claim)

        faction.queueSave()

        try {
            def map = CustomSpawners.spawnerChunkCache.get(Bukkit.getWorld(cl.worldName))

            if (map != null || !map.isEmpty()) {
                def cache = map.get(CustomSpawners.chunkToHash(cl.x, cl.z))
                if (cache != null) {
                    FTopUtils.addFTopEntry(faction.getId(), cache.totalSpawnerValue, FTEntryType.SPAWNER_VALUE)
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }

        return true
    }

    static synchronized boolean unClaimChunk(Faction faction, CL cl) {
        def board = Factions.getBoardSync(cl.worldName)
        if (board == null) board = Factions.boardCache.computeIfAbsent(cl.worldName, { new Board(cl.worldName) })

        def claim = board.chunkClaims.get(cl)
        if (claim != null && claim.getFactionId() != Factions.wildernessId) {
            if (claim.isCoreChunk()) {
                faction.coreChunkData = null
            }

            board.removeChunkClaim(cl)
            faction.claims.remove(claim)

            faction.queueSave()

            try {
                def map = CustomSpawners.spawnerChunkCache.get(Bukkit.getWorld(cl.worldName))

                if (map != null) {
                    if (!map.isEmpty()) {
                        def cache = map.get(CustomSpawners.chunkToHash(cl.x, cl.z))
                        if (cache != null) {
                            FTopUtils.addFTopEntry(faction.getId(), -1 * cache.totalSpawnerValue, FTEntryType.SPAWNER_VALUE)
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
            }

            return true
        }

        return false
    }

    static boolean tryClaimRegion(Faction faction, SR region) {
        def board = Factions.getBoardSync(region.world)
        if (board == null) board = Factions.boardCache.computeIfAbsent(region.world, { new Board(region.world) })

        if (board.getRegionClaims().find {it.key.overlaps(region)}) {
            return false
        }

        def claim = new Claim(faction.getId(), region)
        board.addRegionClaim(region, claim)
        faction.claims.add(claim)
        faction.queueSave()

        return true
    }

//    int[][] matrix = [
//            [1,2,3,4,5],
//            [6,7,8,9,10],
//            [11,12,13,14,15],
//            [16,17,18,19,20],
//            [21,22,23,24,25]]

    static def traverseSpiralExpand(int[][] matrix) {
        def length = matrix.length
        if (length != matrix[0].length) {
            return
        }

        // starting point
        int x = ((int) (length - 1) / 2)
        int y = ((int) (length - 1) / 2)

        // 0=>x++, 1=>y++, 2=>x--, 3=>y--
        def direction = 0

        println(matrix[x][y])
        for (def chainSize = 1; chainSize < length; chainSize++) {
            for (def j = 0; j < (chainSize < length - 1 ? 2 : 3); j++) {
                for (def i = 0; i < chainSize; i++) {
                    switch (direction) {
                        case 0:
                            x++
                            break
                        case 1:
                            y++
                            break
                        case 2:
                            x--
                            break
                        case 3:
                            y--
                            break
                    }
                    println(matrix[x][y])
                }
                direction = (direction + 1) % 4
            }
        }
    }

    static List<String> symbols = ['#', '-', 'a', '/', '=', 'h', '@', 'g', 'b', 'e', 'c', 'd']

    static String[][] compass = [
            ["\\", "N", "/"],
            ["W", "+", "E"],
            ["/", "S", "\\"]
    ]

    static int[][] compassMap = [
            /*"N": */[0,1],
            /*"NE": */[0,2],
            /*"E": */[1,2],
            /*"SE": */[2,2],
            /*"S": */[2,1],
            /*"SW": */[2,0],
            /*"W": */[1,0],
            /*"NW": */[0,0],
    ]

    static def showMap(PlayerContext cmd, CL cl) {
        FCommandUtil.memberFromCommand(cmd) { member ->
            def player = cmd.sender()

            def degrees = cmd.sender().getLocation().getYaw()

            degrees = (degrees - 157) % 360;
            if (degrees < 0) degrees += 360;

            int facingIndex = (int) Math.floor(degrees / 45)

            def standingCl = CL.of(cmd.sender().getLocation())

            Map<UUID, String> factionSymbols = [:]
            Set<Faction> factions = []

            def color = "§2"
            def claimString = "Wilderness"

            def board = Factions.getBoardSync(standingCl.worldName)
            if (board == null) board = Factions.boardCache.computeIfAbsent(standingCl.worldName, { new Board(standingCl.worldName) })

            def standingClaim = board.chunkClaims.get(standingCl)
            if (standingClaim != null) {
                def standingFaction = Factions.getFaction(standingClaim.getFactionId(), false)

                if (standingFaction != null) {
                    def relation = Factions.getRelationType(member, standingFaction)
                    if (relation != null) color = relation.color

                    claimString = standingFaction.getName()
                }
            }

            def repeatValue = (int) ((41 - (claimString.length() + 3)) / 2)
            cmd.reply("§3§m${StringUtils.repeat("-", repeatValue)}§8 [ ${color + claimString} §8] §3§m${StringUtils.repeat("-", repeatValue)}")

            List<String> localSymbols = new ArrayList<>(symbols)

            StringBuilder message = new StringBuilder("")

            int lastY = 0
            int compassX = 0
            int compassY = 0

            for (int y = -8; y <= 8; y++) {
                for (int x = -20; x <= 20; x++) {
                    CL newCl = new CL(cl.worldName, cl.getX() + x, cl.getZ() + y)

                    if (lastY != y) {
                        if (message.toString() != "") cmd.reply(message.toString())
                        message = new StringBuilder("")
                    }

                    // compass
                    if (y == -8 || y == -7 || y == -6) { // first three rows
                        if (x == -20 || x == -19 || x == -18) {// first three columns
                            if (compassY >= 0 && compassY <= 2) {
                                if (compassMap[facingIndex][0] == compassX && compassMap[facingIndex][1] == compassY) {
                                    message.append("§e${compass[compassX][compassY]}")
                                } else {
                                    message.append("§3${compass[compassX][compassY]}")
                                }

                                compassY++
                                lastY = y
                                continue
                            }
                        }
                    }

                    boolean isOrigin = x == 0 && y == 0

                    if (board.chunkClaims.containsKey(newCl)) {
                        def claim = board.chunkClaims.get(newCl)
                        def faction = Factions.getFaction(claim.getFactionId(), false)

                        if (faction == null) {
                            if (isOrigin) message.append("§3+")
                            else message.append("§7\\")

                            continue
                        }

                        def relation = Factions.getRelationType(member, faction)
                        if (relation == null) relation = RelationType.NEUTRAL

                        def symbol = factionSymbols.get(faction.getId()) ?: null
                        if (symbol == null) {
                            symbol = RandomUtils.getRandom(localSymbols)
                            localSymbols.remove(symbol)

                            symbol = relation.color + symbol

                            factionSymbols.put(faction.getId(), symbol)
                            factions.add(faction)
                        }

                        if (isOrigin) {
                            message.append("${relation.color}+")
                        } else if (lastY == y) {
                            message.append("§7${symbol}")
                        } else {
                            cmd.reply(message.toString())
                            message = new StringBuilder("§7${symbol}")
                        }
                    } else {
                        if (isOrigin) {
                            message.append("§3+")
                        } else {
                            message.append("§7\\")
                        }
                    }

                    lastY = y
                }

                compassX++
                compassY = 0
            }

            if (factions.isEmpty())
            {
                cmd.reply("§2\\ §7- §2Wilderness")
            }
            else
            {
                def msg = factions.collect {
                    def relation = Factions.getRelationType(member, it)
                    def symbol = factionSymbols.get(it.getId())
                    return "§7${symbol} §7- ${relation.color + it.getName()}"
                }.join("§7, ")

                cmd.reply("§2\\ §7- §2Wilderness§7, ${msg}")
            }
        }
    }

    static ItemStack createFactionCoreItem(Faction faction) {
        def item = FastItemUtils.createItem(Material.BEACON, "§eFaction Core", [
                "§7This is your faction core.",
                "§7You can use this to claim your core chunks.",
                "",
                "§7You can claim your core chunks by",
                "§7right clicking this item on a chunk.",
                "",
                "§7You can only claim your core chunks",
                "§7in your own faction territory.",
                "",
                "§7You can only claim your core chunks",
                "§7in the overworld."
        ])

        return item
    }

}
