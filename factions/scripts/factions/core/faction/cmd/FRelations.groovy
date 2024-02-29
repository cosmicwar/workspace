package scripts.factions.core.faction.cmd

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.command.context.PlayerContext
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.log.Logs
import scripts.factions.content.log.v2.api.Log
import scripts.factions.content.log.v2.api.LogType
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.FConst
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.core.faction.perm.perms.cmd.InvitePerm
import scripts.factions.core.faction.perm.perms.cmd.KickPerm
import scripts.factions.core.faction.perm.perms.cmd.RelationChangePerm
import scripts.factions.core.faction.perm.perms.cmd.RenamePerm
import scripts.factions.data.uuid.UUIDDataManager
import scripts.shared.legacy.utils.StringUtils

@CompileStatic(TypeCheckingMode.SKIP)
class FRelations {

    FRelations() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf {
                it.aliases.find {
                    it.equalsIgnoreCase("enemy")
                            || it.equalsIgnoreCase("create")
                            || it.equalsIgnoreCase("leave")
                            || it.equalsIgnoreCase("rename")
                            || it.equalsIgnoreCase("neutral")
                            || it.equalsIgnoreCase("truce")
                            || it.equalsIgnoreCase("ally")
                            || it.equalsIgnoreCase("kick")
                            || it.equalsIgnoreCase("join")
                            || it.equalsIgnoreCase("invite")
                            || it.equalsIgnoreCase("leader")
                            || it.equalsIgnoreCase("coleader")
                            || it.equalsIgnoreCase("officer")
                            || it.equalsIgnoreCase("promote")
                            || it.equalsIgnoreCase("demote")
                            || it.equalsIgnoreCase("reldebug")
                } != null
            }
            Factions?.fCommand?.build()
        }

        commands()
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        fCommand.create("create").usage("<name>").description("Create a faction.").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                String name = cmd.arg(0).parseOrFail(String)

                if (member.getFactionId() != null) {
                    def faction = Factions.getFaction(member.getFactionId(), false)

                    if (faction != null) {
                        if (faction.systemFactionData == null) {
                            cmd.reply("§3You are already in a faction.")
                            return
                        }
                    }
                }

                if (name.size() < Factions.factionSettings.getConfig("values").getIntEntry(FConst.minFactionNameLength.id).value) {
                    cmd.reply("§3The faction name must be at least §e${Factions.factionSettings.getConfig("values").getIntEntry(FConst.minFactionNameLength.id).value}§3 characters long.")
                    return
                }

                if (name.size() >= Factions.factionSettings.getConfig("values").getIntEntry(FConst.maxFactionNameLength.id).value) {
                    cmd.reply("§3The faction name cannot be longer than §e${Factions.factionSettings.getConfig("values").getIntEntry(FConst.maxFactionNameLength.id).value}§3 characters long.")
                    return
                }

                if (Factions.getFactionByName(name) != null) {
                    cmd.reply("§3A faction with the name §e${name}§3 already exists.")
                    return
                }

                def faction = Factions.createFaction(name)
                Factions.setFaction(member, faction, Role.LEADER)

                def log = new Log(LogType.FACTION, faction.id, faction.getName())
                log.initiatorType = LogType.PLAYER
                log.initiatorId = member.id
                log.initiatorName = member.getName()
                log.title = "Created Faction"
                log.logMessage = ["§e${member.getName()} §3has created the faction §e${faction.getName()}§3.".toString()]

                Logs.insertLog(faction.id, LogType.FACTION, log)
                Logs.insertLog(member.id, LogType.PLAYER, log)

                cmd.reply("§3You have created the faction §e${faction.getName()}§3.")
            }
        }

        fCommand.create("leave").description("Leave a Faction.").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (faction.systemFactionData != null) {
                    if (faction.name.equalsIgnoreCase("wilderness")) {
                        cmd.reply("§3You are not in a faction.")
                        return
                    }
                }

                if (member.role == Role.LEADER || faction.getLeaderId() == member.getId()) {
                    cmd.reply("§3You cannot leave the faction as a leader, use §e§n/f disband§3 instead.")
                    return
                }

                if (faction.getMembers().size() == 1) {
                    cmd.reply("§3You are the only member, use §e§n/f disband§3 instead.")
                    return
                }

                Factions.removeMember(member, true) { success ->
                    if (success) {
                        def log = new Log(LogType.FACTION, faction.id, faction.getName())
                        log.initiatorType = LogType.PLAYER
                        log.initiatorId = member.id
                        log.initiatorName = member.getName()
                        log.title = "Left Faction"
                        log.logMessage = ["§e${member.getName()} §3has left the faction §e${faction.getName()}§3.".toString()]

                        Logs.insertLog(faction.id, LogType.FACTION, log)
                        Logs.insertLog(member.id, LogType.PLAYER, log)
                        cmd.reply("§3You have left the faction §e${faction.getName()}§3.")
                    } else {
                        cmd.reply("§3An error occurred while leaving the faction.")
                    }
                }
            }
        }

        fCommand.create("rename").usage("<name>").description("Rename a faction.").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (!RenamePerm.canAccess(faction, member)) {
                    Players.msg(cmd.sender(), "§] §> §cError: Lacking Permission.")
                    return
                }

                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a new name.")
                    return
                }

                String name = cmd.arg(0).parseOrFail(String)

                if (name.size() < Factions.factionSettings.getConfig("values").getIntEntry(FConst.minFactionNameLength.id).value) {
                    cmd.reply("§3The faction name must be at least §e${Factions.factionSettings.getConfig("values").getIntEntry(FConst.minFactionNameLength.id).value}§3 characters long.")
                    return
                }

                if (name.size() >= Factions.factionSettings.getConfig("values").getIntEntry(FConst.maxFactionNameLength.id).value) {
                    cmd.reply("§3The faction name cannot be longer than §e${Factions.factionSettings.getConfig("values").getIntEntry(FConst.maxFactionNameLength.id).value}§3 characters long.")
                    return
                }

                if (name == faction.getName()) {
                    cmd.reply("§3The faction name is already §e${name}§3.")
                    return
                }

                if (Factions.getFactionByName(name) != null) {
                    cmd.reply("§3A faction with the name §e${name}§3 already exists.")
                    return
                }

                def log = new Log(LogType.FACTION, faction.id, faction.getName())
                log.initiatorType = LogType.PLAYER
                log.initiatorId = member.id
                log.initiatorName = member.getName()
                log.title = "Renamed Faction"
                log.logMessage = ["§e${member.getName()} §3has renamed the faction from §c${faction.getName()} §a3to §a${name}§3.".toString()]

                faction.setName(name)
                faction.queueSave()

                Logs.insertLog(faction.id, LogType.FACTION, log)
                Logs.insertLog(member.id, LogType.PLAYER, log)

                cmd.reply("§3You have renamed the faction to §e${name}§3.")
            }
        }

        fCommand.create("enemy").usage("Enemy a faction.").register { cmd ->
            handleRelationCommand(cmd, RelationType.ENEMY)
        }

        fCommand.create("neutral").usage("Neutral a faction.").register { cmd ->
            handleRelationCommand(cmd, RelationType.NEUTRAL)
        }

        fCommand.create("truce").usage("Truce a faction.").register { cmd ->
            handleRelationCommand(cmd, RelationType.TRUCE)
        }

        fCommand.create("ally").requirePermission("faction.dev").register { cmd ->
            handleRelationCommand(cmd, RelationType.ALLY)
        }

        fCommand.create("kick").requirePermission("faction.dev").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd, { faction, member ->
                if (!KickPerm.canAccess(faction, member)) {
                    Players.msg(cmd.sender(), "§] §> §cError: Lacking Permission.")
                    return
                }

                def targetMember = cmd.arg(0).parseOrFail(Member)
                if (targetMember.getFactionId() != faction.getId()) {
                    Players.msg(cmd.sender(), "§] §> §cError: Target is not in your faction.")
                    return
                }

                if (targetMember.isRoleAtleast(member.getRole())) {
                    Players.msg(cmd.sender(), "§] §> §cError: You cannot kick a member with a higher role.")
                    return
                }

                Factions.removeMember(targetMember, true) { success ->
                    if (success) {
                        def log = new Log(LogType.FACTION, targetMember.id, targetMember.getName())
                        log.title = "Kicked Member"
                        log.logMessage = ["§e${member.getName()} §3has kicked §e${targetMember.getName()}§3 from the faction §e${faction.getName()}§3.".toString()]

                        Logs.insertLog(faction.id, LogType.FACTION, log)
                        Logs.insertLog(member.id, LogType.PLAYER, log)
                        Logs.insertLog(targetMember.id, LogType.PLAYER, log)

                        faction.msg("§e${targetMember.getDisplayName()}§3 has been kicked from the faction by §e${member.getDisplayName()}§3.")
                        cmd.reply("§3You have kicked §e${targetMember.getDisplayName()}§3 from the faction.")

                        def targetPlayer = Bukkit.getPlayer(targetMember.getId())
                        if (targetPlayer) {
                            Players.msg(targetPlayer, "§3You have been kicked from the faction §e${faction.getName()}§3.")
                        }
                    } else {
                        cmd.reply("§3An error occurred while kicking the member.")
                    }
                }
            })
        }

        fCommand.create("join").usage("<name>").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                boolean bypass = member.role == Role.ADMIN

                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a faction to join.")
                    return
                }

                if (!bypass) {
                    if (member.getFactionId() != null && member.getFactionId() != Factions.wildernessId) {
                        cmd.reply("§3You are already in a faction.")
                        return
                    }
                }

                String name = cmd.arg(0).parseOrFail(String)
                def target = Bukkit.getPlayer(name)
                if (target != null) {
                    def targetMember = Factions.getMember(target.getUniqueId())

                    if (targetMember.getFactionId() == null) {
                        cmd.reply("§3That player is not in a faction.")
                        return
                    }

                    def faction = Factions.getFaction(targetMember.getFactionId(), false)
                    if (faction == null) {
                        cmd.reply("§3That player is not in a faction.")
                        return
                    }

                    if (faction.systemFactionData != null && !bypass) {
                        cmd.reply("§3You cannot join a system faction.")
                        return
                    }

                    if (!faction.isOpen() && !bypass) {
                        if (!faction.getPendingInvites().containsKey(member.getId())) {
                            cmd.reply("§3That faction is not open.")
                            return
                        }
                    }

                    if (faction.getMembers().size() >= Factions.getMaxFactionSize(faction)) {
                        cmd.reply("§3That faction is full.")
                        return
                    }

                    faction.getPendingInvites().remove(member.getId())
                    Factions.setFaction(member, faction)

                    def log = new Log(LogType.FACTION, member.id, member.getName())
                    log.title = "Joined Faction"
                    log.logMessage = ["§e${member.getName()} §3has joined the faction §e${faction.getName()}§3.".toString()]

                    Logs.insertLog(faction.id, LogType.FACTION, log)
                    Logs.insertLog(member.id, LogType.PLAYER, log)

                    faction.msg("§e${member.getDisplayName()}§3 has joined the faction.")
                    cmd.reply("§3You have joined the faction §e${faction.getName()}§3.")
                    return
                }

                def faction = Factions.getFactionByName(name)

                if (faction == null) {
                    cmd.reply("§3A faction with the name §e${name}§3 does not exist.")
                    return
                }

                if (faction.systemFactionData != null && !bypass) {
                    cmd.reply("§3You cannot join a system faction.")
                    return
                }

                if (!faction.isOpen() && !bypass) {
                    if (!faction.getPendingInvites().containsKey(member.getId())) {
                        cmd.reply("§3That faction is not open.")
                        return
                    }
                }

                if (faction.getMembers().size() >= Factions.getMaxFactionSize(faction)) {
                    cmd.reply("§3That faction is full.")
                    faction.msg("§e${member.getDisplayName()}§3 tried to join the faction but it was full.")
                    return
                }

                faction.getPendingInvites().remove(member.getId())
                Factions.setFaction(member, faction)

                def log = new Log(LogType.FACTION, member.id, member.getName())
                log.title = "Joined Faction"
                log.logMessage = ["§e${member.getName()} §3has joined the faction §e${faction.getName()}§3.".toString()]

                Logs.insertLog(faction.id, LogType.FACTION, log)
                Logs.insertLog(member.id, LogType.PLAYER, log)

                faction.msg("§e${member.getDisplayName()}§3 has joined the faction.")
                cmd.reply("§3You have joined the faction §e${faction.getName()}§3.")
            }
        }

        fCommand.create("invite").usage("<name>").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (!InvitePerm.canAccess(faction, member)) {
                    Players.msg(cmd.sender(), "§] §> §cError: Lacking Permission.")
                    return
                }

                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a player to invite.")
                    return
                }

                String name = cmd.arg(0).parseOrFail(String)
                def target = Bukkit.getPlayer(name)
                UUID targetId = null
                String targetName = ""
                if (target == null) {
                    def op = Bukkit.getOfflinePlayer(name)
                    targetId = op.getUniqueId()
                    targetName = op.getName()
                } else {
                    targetId = target.getUniqueId()
                    targetName = target.getName()
                }

                def targetMember = Factions.getMember(targetId, false)
                if (targetMember == null) {
                    cmd.reply("§3A player with the name §e${name}§3 does not exist.")
                    return
                }

                if (targetMember.getFactionId() != null && targetMember.getFactionId() != Factions.wildernessId) {
                    cmd.reply("§3That player is already in a faction.")
                    return
                }

                if (faction.getPendingInvites().containsKey(targetMember.getId())) {
                    cmd.reply("§3You have already invited §e${targetName}§3.")
                    return
                }

                faction.msg("§e${targetName}§3 has been invited to the faction by §e${member.getDisplayName()}§3.")
                faction.pendingInvites.put(targetMember.getId(), System.currentTimeMillis())

                def log = new Log(LogType.FACTION, targetMember.id, targetMember.getName())
                log.title = "Invited To Faction"
                log.logMessage = ["§e${member.getName()} §3has invited §e${targetName}§3 to the faction §e${faction.getName()}§3.".toString()]

                Logs.insertLog(faction.id, LogType.FACTION, log)
                Logs.insertLog(member.id, LogType.PLAYER, log)
                Logs.insertLog(targetMember.id, LogType.PLAYER, log)

                cmd.reply("§3You have invited §e${targetName}§3 to the faction.")
                if (target) Players.msg(target, "§3You have been invited to the faction §e${faction.getName()}§3.")
            }
        }

        fCommand.create("leader").description("Leave a Faction.").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (faction.systemFactionData != null) {
                    if (faction.name.equalsIgnoreCase("wilderness")) {
                        cmd.reply("§3You are not in a faction.")
                        return
                    }
                }

                boolean bypass = member.role == Role.ADMIN

                if (!bypass) {
                    if (member.role != Role.LEADER || faction.getLeaderId() != member.getId()) {
                        cmd.reply("§3You must be the leader to transfer the faction.")
                        return
                    }
                }


                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a player to transfer the faction to.")
                    return
                }

                String name = cmd.arg(0).parseOrFail(String)
                def target = Bukkit.getPlayer(name)

                UUID targetId = null
                String targetName = ""
                if (target == null) {
                    def op = Bukkit.getOfflinePlayer(name)
                    targetId = op.getUniqueId()
                    targetName = op.getName()
                } else {
                    targetId = target.getUniqueId()
                    targetName = target.getName()
                }

                def targetMember = Factions.getMember(targetId, false)
                if (targetMember == null) {
                    cmd.reply("§3A player with the name §e${name}§3 does not exist.")
                    return
                }

                if (targetMember.getFactionId() != faction.getId()) {
                    cmd.reply("§3That player is not in your faction.")
                    return
                }

                if (targetMember.getRole() == Role.LEADER) {
                    cmd.reply("§3You cannot transfer the faction to the leader.")
                    return
                }

                if (targetMember.getRole() == Role.ADMIN) {
                    cmd.reply("§3You cannot transfer the faction to an admin.")
                    return
                }

                faction.setLeaderId(targetMember.getId())
                targetMember.role = Role.LEADER
                member.role = Role.COLEADER

                faction.queueSave()
                targetMember.queueSave()
                member.queueSave()

                def log = new Log(LogType.FACTION, targetMember.id, targetMember.getName())
                log.title = "Set To Leader"
                log.logMessage = ["§e${targetMember.getName()} §3has been set to leader by §e${member.getName()}§3.".toString()]

                Logs.insertLog(faction.id, LogType.FACTION, log)
                Logs.insertLog(member.id, LogType.PLAYER, log)
                Logs.insertLog(targetMember.id, LogType.PLAYER, log)

                faction.msg("§e${targetName}§3 has been promoted to §e${targetMember.getRole().name} §3by §e${member.getDisplayName()}§3.")
                cmd.reply("§3You have promoted §e${targetName}§3 to §e${targetMember.getRole().name}§3.")
            }
        }

        fCommand.create("coleader").requirePermission("faction.dev").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd, { faction, member ->
                boolean bypass = member.role == Role.ADMIN

                if (!bypass) {
                    if (!member.isRoleAtleast(Role.LEADER)) {
                        Players.msg(cmd.sender(), "§] §> §cError: Lacking Permission.")
                        return
                    }
                }

                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a player to promote.")
                    return
                }

                String name = cmd.arg(0).parseOrFail(String)
                def target = Bukkit.getPlayer(name)
                UUID targetId = null
                String targetName = ""
                if (target == null) {
                    def op = Bukkit.getOfflinePlayer(name)
                    targetId = op.getUniqueId()
                    targetName = op.getName()
                } else {
                    targetId = target.getUniqueId()
                    targetName = target.getName()
                }

                def targetMember = Factions.getMember(targetId, false)
                if (targetMember == null) {
                    cmd.reply("§3A player with the name §e${name}§3 does not exist.")
                    return
                }

                if (targetMember.getFactionId() != faction.getId()) {
                    cmd.reply("§3That player is not in your faction.")
                    return
                }

                if (targetMember.getRole() == Role.LEADER) {
                    cmd.reply("§3You cannot promote the leader.")
                    return
                }

                if (targetMember.getRole() == Role.ADMIN) {
                    cmd.reply("§3You cannot promote an admin.")
                    return
                }

                if (targetMember.getRole() == Role.COLEADER) {
                    cmd.reply("§3You cannot promote a co-leader.")
                    return
                }

                targetMember.role = Role.COLEADER
                targetMember.queueSave()

                faction.msg("§e${targetName}§3 has been promoted to §e${targetMember.getRole().name} §3by §e${member.getDisplayName()}§3.")
                cmd.reply("§3You have promoted §e${targetName}§3 to §e${targetMember.getRole().name}§3.")
            })
        }

        fCommand.create("officer").requirePermission("faction.dev").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd, { faction, member ->
                boolean bypass = member.role == Role.ADMIN

                if (!bypass) {
                    if (!member.isRoleAtleast(Role.COLEADER)) {
                        Players.msg(cmd.sender(), "§] §> §cError: Lacking Permission.")
                        return
                    }
                }

                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a player to promote.")
                    return
                }

                String name = cmd.arg(0).parseOrFail(String)
                def target = Bukkit.getPlayer(name)
                UUID targetId = null
                String targetName = ""
                if (target == null) {
                    def op = Bukkit.getOfflinePlayer(name)
                    targetId = op.getUniqueId()
                    targetName = op.getName()
                } else {
                    targetId = target.getUniqueId()
                    targetName = target.getName()
                }

                def targetMember = Factions.getMember(targetId, false)
                if (targetMember == null) {
                    cmd.reply("§3A player with the name §e${name}§3 does not exist.")
                    return
                }

                if (targetMember.getFactionId() != faction.getId()) {
                    cmd.reply("§3That player is not in your faction.")
                    return
                }

                if (targetMember.getRole() == Role.LEADER) {
                    cmd.reply("§3You cannot promote the leader.")
                    return
                }

                if (targetMember.getRole() == Role.ADMIN) {
                    cmd.reply("§3You cannot promote an admin.")
                    return
                }

                if (targetMember.getRole() == Role.COLEADER) {
                    cmd.reply("§3You cannot promote a co-leader.")
                    return
                }

                if (targetMember.getRole() == Role.OFFICER) {
                    cmd.reply("§3You cannot promote an officer.")
                    return
                }

                targetMember.role = Role.OFFICER
                targetMember.queueSave()

                faction.msg("§e${targetName}§3 has been promoted to §e${targetMember.getRole().name} §3by §e${member.getDisplayName()}§3.")
                cmd.reply("§3You have promoted §e${targetName}§3 to §e${targetMember.getRole().name}§3.")
            })
        }

        fCommand.create("promote").usage("<name>").description("Promote a member.").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                boolean bypass = member.role == Role.ADMIN

                if (!bypass) {
                    if (!member.isRoleAtleast(Role.COLEADER)) {
                        cmd.reply("§3You must be at least a co-leader to promote a member.")
                        return
                    }
                }

                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a member to promote.")
                    return
                }

                String name = cmd.arg(0).parseOrFail(String)
                def target = Bukkit.getPlayer(name)
                UUID targetId = null
                String targetName = ""
                if (target == null) {
                    def op = Bukkit.getOfflinePlayer(name)
                    targetId = op.getUniqueId()
                    targetName = op.getName()
                } else {
                    targetId = target.getUniqueId()
                    targetName = target.getName()
                }

                def targetMember = Factions.getMember(targetId, false)
                if (targetMember == null) {
                    cmd.reply("§3A player with the name §e${name}§3 does not exist.")
                    return
                }

                if (targetMember.getFactionId() != faction.getId()) {
                    cmd.reply("§3That player is not in your faction.")
                    return
                }

                if (targetMember.getRole() == Role.LEADER) {
                    cmd.reply("§3You cannot a leader, use §e§n/f leader§3 instead.")
                    return
                }

                if (targetMember.getRole() == Role.ADMIN) {
                    cmd.reply("§3You cannot promote an admin.")
                    return
                }

                if (targetMember.getRole() == Role.OFFICER && !member.isRoleAtleast(Role.LEADER)) {
                    cmd.reply("§3You cannot promote an officer.")
                    return
                }

                if (targetMember.getRole() == Role.COLEADER) {
                    cmd.reply("§3You cannot promote a co-leader, use §e§n/f leader§3 instead.")
                    return
                }

                targetMember.role = Role.getNextRole(targetMember.getRole())
                targetMember.queueSave()

                faction.msg("§e${targetName}§3 has been promoted to §e${targetMember.getRole().name} §3by §e${member.getDisplayName()}§3.")
                cmd.reply("§3You have promoted §e${targetName}§3 to §e${targetMember.getRole().name}§3.")
            }
        }

        fCommand.create("demote").requirePermission("faction.dev").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd, { faction, member ->
                boolean bypass = member.isRoleAtleast(Role.ADMIN)

                if (!bypass) {
                    if (!member.isRoleAtleast(Role.COLEADER)) {
                        cmd.reply("§3You must be at least a co-leader to promote a member.")
                        return
                    }
                }

                if (cmd.args().size() == 0) {
                    cmd.reply("§3You must specify a member to promote.")
                    return
                }

                String name = cmd.arg(0).parseOrFail(String)
                def target = Bukkit.getPlayer(name)
                UUID targetId = null
                String targetName = ""
                if (target == null) {
                    def op = Bukkit.getOfflinePlayer(name)
                    targetId = op.getUniqueId()
                    targetName = op.getName()
                } else {
                    targetId = target.getUniqueId()
                    targetName = target.getName()
                }

                def targetMember = Factions.getMember(targetId, false)
                if (targetMember == null) {
                    cmd.reply("§3A player with the name §e${name}§3 does not exist.")
                    return
                }

                if (targetMember.getFactionId() != faction.getId()) {
                    cmd.reply("§3That player is not in your faction.")
                    return
                }

                if (targetMember.getRole() == Role.LEADER) {
                    cmd.reply("§3You cannot demote the leader.")
                    return
                }

                if (targetMember.getRole() == Role.ADMIN) {
                    cmd.reply("§3You cannot demote an admin.")
                    return
                }

                if (targetMember.getRole() == Role.COLEADER && member.role != Role.LEADER) {
                    cmd.reply("§3You cannot demote an officer.")
                    return
                }

                targetMember.role = Role.getPreviousRole(targetMember.getRole())
                targetMember.queueSave()

                faction.msg("§e${targetName}§3 has been demoted to §e${targetMember.getRole().name} §3by §e${member.getDisplayName()}§3.")
                cmd.reply("§3You have demoted §e${targetName}§3 to §e${targetMember.getRole().name}§3.")
            })
        }

        fCommand.create("reldebug").register { ctx ->
            FCommandUtil.memberFromCommand(ctx) { member ->
                UUIDDataManager.getAllData(Faction).each {
                    ctx.reply("${it.name} - ${Factions.getRelationType(member, it).toString()}")
                }
            }
        }

        fCommand.build()
    }

    static def handleRelationCommand(PlayerContext ctx, RelationType type) {
        FCommandUtil.factionMemberFromCommand(ctx, { faction, member ->
            if (!RelationChangePerm.canAccess(faction, member)) {
                Players.msg(ctx.sender(), "§] §> §cError: Lacking Permission.")
            }

            def targetFaction = ctx.arg(0).parseOrFail(Faction)

            if (targetFaction.systemFactionData != null) {
                ctx.reply("§3You cannot ${type.getDisplayName()} §3a system faction.")
                return
            }

            if (targetFaction == faction) {
                ctx.reply("§3You cannot ${type.getDisplayName()} §3your own faction.")
                return
            }

            def relation = faction.getRelation(targetFaction)
            if (relation != null) {
                if (relation.type == type) {
                    ctx.reply("§3You are already ${type.getPluralDisplayName()} §3with §e${type.color + targetFaction.getName()}§3.")
                    return
                }
            }

            if (!type.requiresBoth) {
                Factions.setRelationType(faction, targetFaction, faction.id, type) { success ->
                    if (!success) {
                        ctx.reply("§3An error occurred while ${type.getPastDisplayName()} §3the faction.")
                        return
                    }

                    targetFaction.pendingRelationChanges.remove(faction.getId())
                    faction.pendingRelationChanges.remove(targetFaction.getId())

                    def log = new Log(LogType.FACTION, faction.id, faction.getName())
                    log.initiatorType = LogType.PLAYER
                    log.initiatorId = member.id
                    log.initiatorName = member.getName()
                    log.title = type.getDisplayName()
                    log.logMessage = ["§e${member.getName()} §3has ${type.getPastDisplayName()} §3the faction §e${type.color + targetFaction.getName()}§3.".toString()]

                    Logs.insertLog(faction.id, LogType.FACTION, log)
                    Logs.insertLog(member.id, LogType.PLAYER, log)

                    def targetLog = new Log(LogType.FACTION, targetFaction.id, targetFaction.getName())
                    log.initiatorType = LogType.FACTION
                    log.initiatorId = faction.id
                    log.initiatorName = faction.getName()
                    log.title = type.getDisplayName()
                    log.logMessage = ["§e${faction.getName()} §3has ${type.getPastDisplayName()} §3the faction §e${type.color + targetFaction.getName()}§3.".toString()]

                    Logs.insertLog(targetFaction.id, LogType.FACTION, targetLog)

                    targetFaction.msg("${type.color + faction.getName()}§a has ${type.getPastDisplayName()} §ayour faction.")
                    faction.msg("§e${member.getDisplayName()} §ahas ${type.getPastDisplayName()} §athe faction §e${type.color + targetFaction.getName()}§a.")
                    ctx.reply("§aYou have ${type.getPastDisplayName()} §athe faction §e${type.color + targetFaction.getName()}§a.")
                }
                return
            }

            if (faction.pendingRelationChanges.containsKey(targetFaction.getId())) {
                def key = faction.pendingRelationChanges.get(targetFaction.getId())
                if (key == type) {
                    ctx.reply("§3You have already sent a request to ${type.getDisplayName()} §e${type.color + targetFaction.getName()}§3.")
                }
            } else if (targetFaction.pendingRelationChanges.containsKey(faction.getId())) {
                def key = targetFaction.pendingRelationChanges.get(faction.getId())
                if (key == type) { // ally each other, faction had a pending request towards our faction
                    Factions.setRelationType(faction, targetFaction, targetFaction.id, type) { success ->
                        if (!success) {
                            ctx.reply("§3An error occurred while ${type.getPastDisplayName()} §3the faction.")
                            return
                        }

                        targetFaction.pendingRelationChanges.remove(faction.getId())

                        def log = new Log(LogType.FACTION, faction.id, faction.getName())
                        log.initiatorType = LogType.PLAYER
                        log.initiatorId = member.id
                        log.initiatorName = member.getName()
                        log.title = type.getDisplayName()
                        log.logMessage = ["§e${member.getName()} §3has ${type.getPastDisplayName()} §3the faction §e${type.color + targetFaction.getName()}§3.".toString()]

                        Logs.insertLog(faction.id, LogType.FACTION, log)
                        Logs.insertLog(member.id, LogType.PLAYER, log)

                        def targetLog = new Log(LogType.FACTION, targetFaction.id, targetFaction.getName())
                        log.initiatorType = LogType.FACTION
                        log.initiatorId = faction.id
                        log.initiatorName = faction.getName()
                        log.title = type.getDisplayName()
                        log.logMessage = ["§e${faction.getName()} §3has ${type.getPastDisplayName()} §3the faction §e${type.color + targetFaction.getName()}§3.".toString()]

                        Logs.insertLog(targetFaction.id, LogType.FACTION, targetLog)

                        targetFaction.msg("${type.color + faction.getName()}§a has ${type.getPastDisplayName()} §ayour faction.")
                        faction.msg("§e${member.getDisplayName()} §ahas ${type.getPastDisplayName()} §athe faction §e${type.color + targetFaction.getName()}§a.")
                        ctx.reply("§aYou have ${type.getPastDisplayName()} §athe faction §e${type.color + targetFaction.getName()}§a.")
                    }
                }
            } else { // send request?
                faction.pendingRelationChanges.put(targetFaction.getId(), type)

                def relationType = Factions.getRelationType(targetFaction, faction)
                targetFaction.msg("${relationType.color + faction.getName()}§3 has sent a${type.getDisplayName().charAt(0) == 'A'.toCharacter() ? "n" : ""} ${type.getDisplayName()}§3 request to §3your faction.")
                relationType = Factions.getRelationType(faction, targetFaction)
                faction.msg("§e${member.getDisplayName()} §ahas sent a${type.getDisplayName().charAt(0) == 'A'.toCharacter() ? "n" : ""} ${type.getDisplayName()} request to §e${relationType.color + targetFaction.getName()}§a.")
                ctx.reply("§3You have sent a request to ${type.getDisplayName()} §e${relationType.color + targetFaction.getName()}§3.")
            }
        })
    }

}
