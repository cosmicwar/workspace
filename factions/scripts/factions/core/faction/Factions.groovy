package scripts.factions.core.faction

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.kyori.adventure.text.Component
import net.minecraft.ChatFormatting
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import scripts.shared.content.SCBuilder
import scripts.shared.content.scoreboard.tab.TabHandler
import scripts.shared.core.cfg.Config
import scripts.shared.core.cfg.ConfigCategory
import scripts.factions.core.faction.addon.upgrade.UpgradeUtil
import scripts.factions.core.faction.claim.Board
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.factions.core.faction.claim.Claim
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.data.SystemFaction
import scripts.factions.core.faction.data.random.WarpData
import scripts.shared.data.obj.CL
import scripts.factions.core.faction.data.relation.Relation
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.core.faction.event.FactionCreateEvent
import scripts.factions.core.faction.perm.Permission
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.utils.*
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.MojangAPI
import scripts.shared3.utils.Callback

import java.nio.file.Files
import java.util.function.BiFunction

@CompileStatic(TypeCheckingMode.SKIP)
class Factions {

    static Map<String, Board> boardCache = Maps.newConcurrentMap()
    static Map<UUID, Faction> factionCache = Maps.newConcurrentMap()
    static Map<UUID, Member> memberCache = Maps.newConcurrentMap()

    static CurrencyStorage moneyStorage = Exports.ptr("money") as CurrencyStorage

    static UUID wildernessId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    static UUID warZoneId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    static UUID safeZoneId = UUID.fromString("33333333-3333-3333-3333-333333333333")

    static Config config

    static ConfigCategory combatSettings
    static ConfigCategory claimSettings
    static ConfigCategory permSettings
    static ConfigCategory factionSettings
    static ConfigCategory economySettings
    static ConfigCategory messageSettings

    static String configId = "factions_config"
    
    static SCBuilder fCommand

    Factions() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getAllData(Member.class).each { member ->
                if (member.previousRole != null) {
                    member.setRole(member.previousRole)
                }
                member.setLastOnline(System.currentTimeMillis())
                member.queueSave()
            }

            UUIDDataManager.getByClass(Member.class).saveAll(false)
            UUIDDataManager.getByClass(Faction.class).saveAll(false)

            defaultPermissions.clear()
            Starlight.unload(permissionPaths as String[])

            Starlight.unload("~/FactionEvents.groovy")
            Starlight.unload("~/FactionUtils.groovy")

            Starlight.unload("~/chat/FChat.groovy")
            Starlight.unload("~/cmd/FLogs.groovy")
            Starlight.unload("~/cmd/FAdmin.groovy")
            Starlight.unload("~/cmd/FRelations.groovy")
            Starlight.unload("~/cmd/FShow.groovy")
            Starlight.unload("~/cmd/FWarp.groovy")

            Starlight.unload("~/addon/sandbot/SandBots.groovy")
            Starlight.unload("~/addon/shield/Shields.groovy")

            Starlight.unload("~/addon/upgrade/Upgrades.groovy")
            Starlight.unload("~/claim/FClaim.groovy")
            Starlight.unload("~/addon/fbanner/FBanner.groovy")
            Starlight.unload("~/addon/ftop/FTop.groovy")
            Starlight.unload("~/addon/tnt/FTnt.groovy")

            Starlight.unload("~/perm/FPerms.groovy")

            if (fCommand) {
                if (fCommand.tabbableFC != null) {
                    fCommand.tabbableFC.close()
                }
            }
        }

        fCommand = new SCBuilder("f", "factions", "faction", "fac", "clan", "island")

        config = DBConfigUtil.createConfig(configId, "§bFactions", ["§bFactions Configuration"], Material.TNT)
        setupConfig()

        UUIDDataManager.register("members", Member.class)
        UUIDDataManager.register("factions", Faction.class) // TODO: create a way to stored disbanded factions for logs?
//        FactionDataManager.register("disbanded_factions", Faction.class)
//        cant save two of the same objects, need to make a way to

        Starlight.watch("~/perm/FPerms.groovy")
        Starlight.watch("~/addon/upgrade/Upgrades.groovy")

        Starlight.watch("~/addon/ftop/FTop.groovy")
        Starlight.watch("~/addon/fbanner/FBanner.groovy")
        Starlight.watch("~/claim/FClaim.groovy")

        Starlight.watch("~/addon/sandbot/SandBots.groovy")
        Starlight.watch("~/addon/shield/Shields.groovy")
        Starlight.watch("~/addon/tnt/FTnt.groovy")

        Starlight.watch("~/cmd/FRelations.groovy")

        Starlight.watch("~/cmd/FAdmin.groovy")
        Starlight.watch("~/cmd/FShow.groovy")
        Starlight.watch("~/cmd/FWarp.groovy")
        Starlight.watch("~/cmd/FLogs.groovy")
        Starlight.watch("~/chat/FChat.groovy")

        Starlight.watch("~/FactionEvents.groovy")
        Starlight.watch("~/FactionUtils.groovy")

        registerPermissions()

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE) {
            if (!GroovyScript.getCurrentScript().getWatchedScripts().contains(it)) return
        }

        UUIDDataManager.getAllData(Member.class).each { member ->
            Schedulers.async().execute {
                memberCache.put(member.getId(), member)
            }
        }

        UUIDDataManager.getAllData(Faction).each { faction ->
            Schedulers.async().execute {
                // check for expired factions here
                faction.claims.each { claim ->
                    if (claim.claimed()) { // TODO: Add support for SR
                        if (claim.location.worldName) {
                            def board = boardCache.computeIfAbsent(claim.location.worldName, { new Board(claim.location.worldName) })

                            board.addChunkClaim(claim.location, claim)
                            if (claim.isCoreChunk()) board.coreChunkClaims.add(claim)
                        } else if (claim.region.world) {
                            def board = boardCache.computeIfAbsent(claim.region.world, { new Board(claim.region.world) })

                            board.addRegionClaim(claim.region, claim)
                        }
                    }
                }

                factionCache.put(faction.getId(), faction)
            }
        }

        createWilderness()
        createWarzone()
        createSafezone()

        BukkitUtils.getOnlineNonSpoofPlayers().each { player -> loadMember(player) }

        Commands.parserRegistry().register(Faction.class) { s ->
            def faction = getFactionByName(s)
            if (faction == null) {
                Player player = Bukkit.getPlayer(s)
                UUID uuid
                if (player == null) {
                    uuid = MojangAPI.getUUID(s)
                    def member = getMember(uuid)
                    if (member != null && member.factionId != null) {
                        faction = getFaction(member.factionId, false)
                    }
                } else {
                    uuid = player.getUniqueId()
                    def member = getMember(uuid)
                    if (member != null && member.factionId != null) {
                        faction = getFaction(member.factionId, false)
                    }
                }
            }
            return Optional.ofNullable(faction)
        }

        Commands.parserRegistry().register(Member.class) { s ->
            Player player = Bukkit.getPlayer(s)
            UUID uuid
            if (player == null) {
                uuid = MojangAPI.getUUID(s)
                return Optional.ofNullable(getMember(uuid))
            } else {
                uuid = player.getUniqueId()
                return Optional.ofNullable(getMember(uuid))
            }
        }

        events()
        commands()

        /*
            arktags/getNameTagData
            arktags/setBuildPrefix
            arktags/setBuildNameColor
            arktags/setBuildSuffix
        */
        TabHandler.buildSuffix = { Player player, Player target ->
            def member = getMember(player.getUniqueId())
            def targetMember = getMember(target.getUniqueId())

            if (targetMember.getFactionId() == null) {
                return ""
            }

            def targetFac = getFaction(targetMember.getFactionId())
            if (targetFac == null || targetFac.id == wildernessId) return ""

            if (member == targetMember) {
                return "§a${targetFac.getName()}"
            } else {
                def relation = getRelationType(member, targetFac)

                return "${relation.color}${targetFac.name}"
            }
        }
        TabHandler.buildNameColor = { Player player, Player target ->
            def member = getMember(player.getUniqueId())
            def targetMember = getMember(target.getUniqueId())

            if (targetMember.getFactionId() == null) {
                return "dd"
            }

            def targetFac = getFaction(targetMember.getFactionId())
            if (targetFac == null || targetFac.id == wildernessId) return "dd"

            if (member == targetMember) {
                return "§add"
            } else {
                def relation = getRelationType(member, targetFac)

                return relation.color + "dd"
            }
        }

        Schedulers.sync().runLater({
            Bukkit.getWorlds().each { world ->
                getBoardCache().computeIfAbsent(world.getName(), { new Board(world.getName()) })
            }
        }, 5L)
    }

    static def createWilderness() {
        createSystemFaction(wildernessId, "Wilderness", "The wild, untamed lands.", "§<#22D851>", [])
    }

    static def createWarzone() {
        createSystemFaction(warZoneId, "War-Zone", "Be careful, pvp is enabled here.", "§<#ff0000>", [])
    }

    static def createSafezone() {
        createSystemFaction(safeZoneId, "Safe-Zone", "You are safe here.", "§<#22D851>", [])
    }

    /*
       ~ events until moved ~
    */

    static def events() {
        Events.subscribe(PlayerJoinEvent.class).handler { event ->
            loadMember(event.getPlayer())
        }

        Events.subscribe(PlayerQuitEvent.class).handler { event ->
            def member = getMember(event.getPlayer().getUniqueId(), true)
            if (member == null) return

            member.setLastOnline(System.currentTimeMillis())
            member.queueSave()
        }

        // chunk update event
        // todo: auto-claim here || auto map || update faction fly
        Events.subscribe(PlayerMoveEvent.class, EventPriority.MONITOR).filter(EventFilters.<PlayerMoveEvent> ignoreCancelled()).handler { e ->
//            if (e.from.blockX != e.to.blockX || e.from.blockY != e.to.blockY || e.from.blockZ != e.to.blockZ) return

            def player = e.getPlayer()
            def member = getMember(player.getUniqueId(), false)
            if (member == null) return

            def locFrom = e.getFrom()
            def locTo = e.getTo()

            def factionFrom = getFactionAt(locFrom)

            def factionTo = getFactionAt(locTo)
            if (factionTo != null) {
                if (factionFrom != null) {
                    if (factionFrom != factionTo) {
                        if (factionTo.systemFactionData != null) {
                            TitleUtils.show(player, ColorUtil.color("${factionTo.systemFactionData.color}${factionTo.getName()}"), ColorUtil.color("${factionTo.systemFactionData.color}${factionTo.description}"), 0, 2, 1)
                        } else {
                            def relation = getRelationType(member, factionTo)
                            TitleUtils.show(player, ColorUtil.color("${relation.color}${factionTo.getName()}"), ColorUtil.color("§f${factionTo.description}"), 0, 2, 1)
                        }
                    }
                } else {
                    if (factionTo.systemFactionData != null) {
                        TitleUtils.show(player, ColorUtil.color("${factionTo.systemFactionData.color}${factionTo.getName()}"), ColorUtil.color("${factionTo.systemFactionData.color}${factionTo.description}"), 0, 2, 1)
                    } else {
                        def relation = getRelationType(member, factionTo)
                        TitleUtils.show(player, ColorUtil.color("${relation.color}${factionTo.getName()}"), ColorUtil.color("§f${factionTo.description}"), 0, 2, 1)
                    }
                }
            } else if (factionFrom != null) {
                TitleUtils.show(player, "§2Wilderness", "§2The wild, untamed lands.", 0, 2, 1)
            }
        }
    }

    static def loadMember(Player player) {
        getMemberAsync(player.getUniqueId()) { member ->
            if (member == null) {
                player.kick(Component.text("§cAn error occurred while loading your faction data. Please rejoin."))
                return
            }

            if (member.getFactionId() == null) {
                setWilderness(member)
            } else {
                def faction = getFaction(member.getFactionId())
                if (faction == null || faction.isDisbanded()) {
                    setWilderness(member)
                }/* else {
                    faction.members.add(member)
                }*/
            }
        }
    }

    static def getMemberAsync(UUID uuid, Callback<Member> callback, boolean create = true) {
        Schedulers.async().execute {
            def member = getMember(uuid, create)
            callback.exec(member)
        }
    }

    static Member getMember(UUID uuid, boolean create = true) {
        def cached = memberCache.get(uuid)
        if (cached != null) return cached

        def member = UUIDDataManager.getData(uuid, Member.class)
        if (member != null) {
            memberCache.put(uuid, member)
            return member
        }

        if (create) {
            member = UUIDDataManager.getData(uuid, Member.class, true)
            memberCache.put(uuid, member)
            return member
        }

        return null
    }

    static Faction createFaction(String name) {
        def faction = getFaction(UUID.randomUUID(), true)
        faction.setName(name)
        faction.setCreateDate(System.currentTimeMillis())

        faction.queueSave()

        Schedulers.sync().execute {
            def event = new FactionCreateEvent(faction, System.currentTimeMillis())
            Bukkit.getPluginManager().callEvent(event)
        }

        Starlight.log.info("[Factions] Created faction ${faction.getName()}")

        return faction
    }

    static def setFaction(Member member, Faction faction, Role role = Role.RECRUIT) {
        removeMember(member, false)

        Schedulers.async().execute {
            member.setFactionId(faction.id)
            member.setRole(role)
            faction.members.add(member.getId())

            if (role == Role.LEADER) faction.leaderId = member.getId()

            member.queueSave()
            faction.queueSave()
        }
    }

    static def setWilderness(Member member) {
        def wilderness = getSystemFaction(wildernessId)
        if (wilderness == null) return

        member.setFactionId(wilderness.id)
        member.setRole(Role.SYSTEM)
        wilderness.members.add(member.getId())

        member.queueSave()
        wilderness.queueSave()
    }

    static def removeMember(Member member, boolean setSystemFaction = true, Callback<Boolean> success = {}) {
        Schedulers.async().execute {
            if (member.getFactionId() != null) {
                def memberFaction = getFaction(member.getFactionId())

                if (memberFaction != null) {
                    memberFaction.members.remove(member.getId())
                    memberFaction.queueSave()
                }
            }

            if (setSystemFaction) setWilderness(member)
            success.exec(true)
        }
    }

    static List<UUID> getFactionIds() {
        UUIDDataManager.getAllData(Faction.class).collect { it.id }
    }

    static List<UUID> systemFactionPossibleIds = [
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            UUID.fromString("44444444-4444-4444-4444-444444444444"),
            UUID.fromString("55555555-5555-5555-5555-555555555555"),
            UUID.fromString("66666666-6666-6666-6666-666666666666"),
            UUID.fromString("77777777-7777-7777-7777-777777777777"),
            UUID.fromString("88888888-8888-8888-8888-888888888888"),
            UUID.fromString("99999999-9999-9999-9999-999999999999"),
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
            UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
    ]

    static def createSystemFaction(UUID uuid, String name, String description, String color, List<Permission> permissions = [], Callback<Faction> callback = {}) {
        if (getFactionIds().contains(uuid)) {
            return
        }

        Schedulers.async().execute {
            def systemFaction = getFaction(uuid, true)
            systemFaction.setName(name)
            systemFaction.setDescription(description)
            systemFaction.setOpen(false)
            systemFaction.setCreateDate(System.currentTimeMillis())

            def systemFactionData = new SystemFaction(color, permissions)
            systemFaction.systemFactionData = systemFactionData

            systemFaction.queueSave()

            callback.exec(systemFaction)
        }

        Starlight.log.info("[Factions] Created system faction ${name}")
    }

    static def createSystemFaction(String name, String description, String color, List<Permission> permissions = [], Callback<Faction> callback) {
        if (UUIDDataManager.getAllData(Faction.class).find { it.getName().equalsIgnoreCase(name) } != null) return null

        def randomId = RandomUtils.getRandom(systemFactionPossibleIds)
        if (randomId == null) return null

        if (getFactionIds().contains(randomId)) {
            createSystemFaction(name, description, color, permissions, callback)
            return
        }

        Schedulers.async().execute {
            def systemFaction = getFaction(randomId, true)
            systemFaction.setName(name)
            systemFaction.setDescription(description)
            systemFaction.setOpen(false)
            systemFaction.setCreateDate(System.currentTimeMillis())

            def systemFactionData = new SystemFaction(color, permissions)
            systemFaction.systemFactionData = systemFactionData

            systemFaction.queueSave()

            callback.exec(systemFaction)
        }
    }

    static def getWilderness(Callback<Faction> callback) {
        Schedulers.async().execute {
            def faction = getSystemFaction(wildernessId)
            callback.exec(faction)
        }
    }

    static def getSystemFactionAsync(String name, Callback<Faction> callback) {
        Schedulers.async().execute {
            def faction = getSystemFaction(name)
            callback.exec(faction)
        }
    }

    static Faction getSystemFaction(String name) {
        return UUIDDataManager.getAllData(Faction.class).find { it.getName().equalsIgnoreCase(name) && it.systemFactionData != null }
    }

    static Faction getSystemFaction(UUID uuid) {
        return UUIDDataManager.getAllData(Faction.class).find { it.systemFactionData != null && it.id == uuid }
    }

    static Faction getFactionByName(String name) {
        return UUIDDataManager.getAllData(Faction.class)?.find { it?.getName()?.equalsIgnoreCase(name) ?: false } ?: null
    }

    static def getFactionAsync(UUID factionId, Callback<Faction> callback, boolean create = true) {
        Schedulers.async().execute {
            def faction = getFaction(factionId, create)
            callback.exec(faction)
        }
    }

    static Faction getFaction(UUID factionId, boolean create = true) {
        return UUIDDataManager.getData(factionId, Faction.class, create)
    }

    static Faction getFactionAt(CL location) {
        def board = getBoardCache().get(location.worldName)
        if (board == null) return null

        def claim = board.chunkClaims.get(location)
        if (claim == null) return null

        return getFaction(claim.getFactionId(), false)
    }

    static Claim getClaimAt(CL location) {
        def board = getBoardCache().get(location.worldName)
        if (board == null) return null

        return board.chunkClaims.get(location)
    }

    static Collection<Claim> getConnectingClaims(Claim startingClaim) {
        Set<Claim> connectingClaims = Sets.newConcurrentHashSet()

        if (startingClaim.factionId == null) return connectingClaims

        Queue<CL> queue = new LinkedList<CL>()
        queue.add(startingClaim.location)
        while (queue.size() > 0) {
            def currentPos = queue.poll()

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++)
                {
                    def chunkLocation = new CL(startingClaim.location.worldName, currentPos.getX() + x, currentPos.getZ() + z)

                    def claim = getClaimAt(chunkLocation)
                    if (claim != null && claim.factionId == startingClaim.factionId) {
                        if (!connectingClaims.contains(claim)) {
                            if (chunkLocation != currentPos) queue.add(chunkLocation)

                            connectingClaims.add(claim)
                        }
                    }
                }
            }
        }

        return connectingClaims
    }

    static Faction getFactionAt(Location location) {
        def board = getBoardCache().get(location.world.name)
        if (board == null) return null

        def claim = board.getClaimAtPos(location)
        if (claim == null) return null

        return getFaction(claim.getFactionId(), false)
    }

    static def getFactionAtAsync(CL location, Callback<Faction> callback) {
        Schedulers.async().execute {
            callback.exec(getFactionAt(location))
        }
    }

    static def getFactionAtAsync(Location location, Callback<Faction> callback) {
        Schedulers.async().execute {
            callback.exec(getFactionAt(location))
        }
    }

    static Collection<Member> getFactionMembers(Faction faction) {
        return faction.members.collect { getMember(it, false) }
    }

    static Collection<Member> getOnlineMembers(Faction faction) {
        return getFactionMembers(faction).findAll { Bukkit.getPlayer(it.getId()) != null }
    }

    static Collection<Member> getOfflineMembers(Faction faction) {
        return getFactionMembers(faction).findAll { Bukkit.getPlayer(it.getId()) != null }
    }

    static def disbandFaction(Faction faction) {
        Schedulers.async().execute {
            if (faction == null) {
                return
            }

            // TODO: money system, give to leader?
            faction.members.each {
                def member = getMember(it)
                removeMember(member, true)
            }

            boardCache.values().each {
                it.chunkClaims.values().removeIf { it.getFactionId() == faction.id }
                it.regionClaims.values().removeIf { it.getFactionId() == faction.id }
            }

            UUIDDataManager.removeOne(faction.id, Faction.class)
        }
    }

    /*
       ~ Relations ~
    */

    static RelationType getRelationType(UUID factionId, UUID targetFactionId) {
        def faction = getFaction(factionId)
        if (faction == null) return RelationType.NEUTRAL

        if (faction.id == targetFactionId) return RelationType.MEMBER

        def relation = faction.factionRelations.find { it.targetFactionId == targetFactionId || it.initiatorId == targetFactionId }
        if (relation == null) {
            def targetFaction = getFaction(targetFactionId)
            if (targetFaction == null) return RelationType.NEUTRAL

            relation = targetFaction.factionRelations.find { it.targetFactionId == faction.id || it.initiatorId == faction.id }
        }

        if (relation == null) return RelationType.NEUTRAL

        return relation.type
    }

    static RelationType getRelationType(Faction faction, Faction targetFaction) {
        if (faction == targetFaction) return RelationType.MEMBER

        def relation = faction.factionRelations.find { it.targetFactionId == targetFaction.id || it.initiatorId == targetFaction.id }
        if (relation == null) {
            relation = targetFaction.factionRelations.find {it.targetFactionId == faction.id || it.initiatorId == faction.id }
        }

        if (relation == null) return RelationType.NEUTRAL

        return relation.type
    }

    static RelationType getRelationType(Member memberId, Member targetMember) {
        def faction = getFaction(memberId.getFactionId(), false)
        if (faction == null) return RelationType.NEUTRAL

        def targetFaction = getFaction(targetMember.getFactionId(), false)
        if (targetFaction == null) return RelationType.NEUTRAL

        if (faction.id == targetFaction.id) return RelationType.MEMBER

        def relation = faction.factionRelations.find { it.targetFactionId == targetFaction.id || it.initiatorId == targetFaction.id }
        if (relation == null) {
            relation = targetFaction.factionRelations.find {it.targetFactionId == faction.id || it.initiatorId == faction.id }
        }

        if (relation == null) return RelationType.NEUTRAL

        return relation.type
    }

    static RelationType getRelationType(Member member, Faction targetFaction) {
        def faction = getFaction(member.getFactionId(), false)
        if (faction == null) return RelationType.NEUTRAL

        if (faction.id == targetFaction.id) return RelationType.MEMBER

        def relation = faction.factionRelations.find { it.targetFactionId == targetFaction.id || it.initiatorId == targetFaction.id }
        if (relation == null) {
            relation = targetFaction.factionRelations.find {it.targetFactionId == faction.id || it.initiatorId == faction.id }
        }

        if (relation == null) return RelationType.NEUTRAL

        return relation.type
    }

    static def setRelationType(UUID factionId, UUID targetFactionId, RelationType type, Callback<Boolean> success = {}) {
        Schedulers.async().execute {
            def faction = getFaction(factionId)
            def targetFaction = getFaction(targetFactionId)
            if (faction == null || targetFaction == null) {
                success.exec(false)
                return
            }

            def relation = faction.factionRelations.find { it.targetFactionId == targetFactionId } // same id
            if (relation == null) {
                relation = new Relation(targetFactionId, factionId, type)
                faction.factionRelations.add(relation)
            } else {
                relation.type = type
            }

            def targetRelation = targetFaction.factionRelations.find { it.targetFactionId == factionId }
            if (targetRelation == null) {
                targetRelation = new Relation(factionId, targetFactionId, type)
                targetFaction.factionRelations.add(targetRelation)
            } else {
                targetRelation.type = type
            }

            targetFaction.queueSave()
            faction.queueSave()
            success.exec(true)
        }
    }

    static def setRelationType(Faction faction, Faction targetFaction, UUID initiatorId, RelationType type, Callback<Boolean> success = {}) {
        Schedulers.async().execute {
            def relation = faction.factionRelations.find { it.targetFactionId == targetFaction.id || it.initiatorId == targetFaction.id } // same id
            if (relation == null) {
                relation = new Relation(targetFaction.id, initiatorId, type)
                faction.factionRelations.add(relation)
            } else {
                relation.type = type
            }

            def targetRelation = targetFaction.factionRelations.find { it.targetFactionId == faction.id || it.initiatorId == faction.id }
            if (targetRelation == null) {
                targetRelation = new Relation(faction.id, initiatorId, type)
                targetFaction.factionRelations.add(targetRelation)
            } else {
                targetRelation.type = type
            }

            targetFaction.queueSave()
            faction.queueSave()
            success.exec(true)
        }
    }

    /*
       ~ perm utils ~
    */

    static Map<String, Permission> defaultPermissions = Maps.newConcurrentMap()
    static Set<String> permissionPaths = Sets.newConcurrentHashSet()

    static def registerPermissions() {
        File permScriptFolder = new File("${GroovyScript.getCurrentScript().getScript().getParent()}${File.separator}/perm/perms")
        Files.walk(permScriptFolder.toPath(), 3).forEach({
            String path = it.toString()
            if (path.endsWith(".groovy")) {
                registerPermission(path)
            }
        })
    }

    static def registerPermission(String permissionPath) {
        if (!permissionPaths.add(permissionPath)) return

        Starlight.watch(permissionPath)
    }

    static def registerPermission(String internalId, Permission permission) {
        defaultPermissions.put(internalId, permission)
        Starlight.log.info("[Factions] Registered permission ${internalId}")
    }

    /*
       ~ Commands ~
    */
    static def commands() {
        // F Disband logic
        fCommand.create("disband").description("Disband a Faction.").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (faction.systemFactionData != null) {
                    if (faction.name.equalsIgnoreCase("wilderness")) {
                        cmd.reply("§3You are not in a faction.")
                        return
                    }
                }

                if (member.role == Role.LEADER || member.role == Role.ADMIN) {
                    createDisbandMenu(member)
                    return
                }

                cmd.reply("§3You must be the leader to disband the faction.")
            }
        }

        /**
         *   ~ admin (spooooky) ~
         */
        fCommand.create("bypass").requirePermission("factions.bypass").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                if (member.previousRole != null && member.role == Role.ADMIN) {
                    member.setRole(member.previousRole)
                    member.setPreviousRole(null)
                    member.queueSave()
                    cmd.reply("§] §> §3You are §cno longer §3in bypass mode.")
                } else {
                    member.setPreviousRole(member.role)
                    member.setRole(Role.ADMIN)
                    member.queueSave()
                    cmd.reply("§] §> §3You are §anow §3in bypass mode.")
                }
            }
        }

        fCommand.create("wipe").requirePermission("faction.*").register { cmd ->
            Schedulers.async().execute {
                UUIDDataManager.wipe(Faction.class)
                UUIDDataManager.wipe(Member.class)
                factionCache.clear()
                memberCache.clear()

                cmd.reply("§] §> §3All factions have been wiped.")
                cmd.reply("§] §> §3re-generating server data...")

                createWilderness()

                BukkitUtils.getOnlineNonSpoofPlayers().each {
                    loadMember(it)
                }

                cmd.reply("§] §> §3regenerated ${BukkitUtils.getOnlineNonSpoofPlayers().size()} members.")
            }
        }

        fCommand.build()
    }

    /*
      ~ f disband ~
    */

    static def createDisbandMenu(Member member) {
        def player = Bukkit.getPlayer(member.getId())
        if (player == null) return

        def faction = getFaction(member.getFactionId())
        if (faction == null) return

        def role = member.getRole()

        if (role == Role.SYSTEM) {
            Players.msg(player, "§3You cannot disband a system faction.")
            return
        }

        if (role == Role.ADMIN || role == Role.LEADER) {
            Schedulers.sync().execute {
                MenuUtils.createConfirmMenu(player, "Disband Faction", FastItemUtils.createItem(Material.NETHER_STAR, "§cDisband Faction", ["§7Are you sure you want to disband your faction?"]), {
                    Players.msg(player, "§3You have disbanded your faction.")
                    player.closeInventory()
                    disbandFaction(faction)
                }, {
                    player.closeInventory()
                    Players.msg(player, "§3You have cancelled disbanding your faction.")
                })
            }
        } else {
            Players.msg(player, "§3You must be leader to disband your faction.")
        }
    }

    /*
       ~ board ~
    */
    static def getBoard(String worldName, Callback<Board> callback) {
        Schedulers.async().execute {
            def board = boardCache.get(worldName)
            if (board != null) callback.exec(board)
        }
    }

    static def getBoard(World world) {
        return boardCache.get(world.getName())
    }

    static def getBoard(World world, Callback<Board> callback) {
        getBoard(world.getName(), callback)
    }

    static Board getBoardSync(World world) {
        return boardCache.get(world.getName())
    }

    static Board getBoardSync(String worldName) {
        return boardCache.get(worldName)
    }

    static def setupConfig() {
        combatSettings = config.getOrCreateCategory("combat", "§3Combat Settings", Material.NETHERITE_SWORD, ["§3Combat Settings"])
        permSettings = config.getOrCreateCategory("perms", "§3Permission Settings", Material.ANVIL, ["§3Permission Settings"])
        claimSettings = config.getOrCreateCategory("claims", "§3Claim Settings", Material.GRASS_BLOCK, ["§3Claim Settings"])
        economySettings = config.getOrCreateCategory("economy", "§3Economy Settings", Material.GOLD_INGOT, ["§3Economy Settings"])
        messageSettings = config.getOrCreateCategory("messages", "§3Message Settings", Material.PAPER, ["§3Message Settings"])

        factionSettings = config.getOrCreateCategory("faction", "§3Faction Settings", Material.GOLDEN_SWORD, ["§3Faction Settings"])
        factionSettings.addEntries("values", FConst.DEFAULT_FACTION_VALUES.toList(), true)
        factionSettings.addEntries("messages", FConst.DEFAULT_FACTION_MESSAGE_ENTRIES.toList(), true)

        config.queueSave()
    }

    static int getMaxFactionSize() {
        return factionSettings.getConfig("values").getIntEntry(FConst.maxFactionSize.getId()).value
    }

    static double getMaxFactionPower() {
        return factionSettings.getConfig("values").getDoubleEntry(FConst.maxFactionPower.getId()).value
    }

    static int getMaxFactionSize(Faction faction) {
        def maxSize = getMaxFactionSize()

        def upgrade = faction.upgradeData?.upgrades?.find { it.internalId == UpgradeUtil.faction_size } ?: null
        if (upgrade != null) return maxSize + upgrade.level

        return maxSize
    }

    static double getMaxFactionPower(Faction faction) {
        def maxPower = getMaxFactionPower()

        def upgrade = faction.upgradeData?.upgrades?.find { it.internalId == UpgradeUtil.faction_power } ?: null
        if (upgrade != null) return maxPower + (upgrade.level * 25)

        return maxPower
    }

}