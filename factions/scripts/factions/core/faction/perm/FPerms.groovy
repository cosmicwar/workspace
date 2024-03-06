package scripts.factions.core.faction.perm

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.meta.tags.ItemTagType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.perm.access.Access
import scripts.factions.core.faction.perm.access.AccessData
import scripts.factions.core.faction.perm.access.TargetType
import scripts.shared.data.obj.CL
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared3.utils.Callback

/*
  ~ perms ~
*/
class FPerms {

    private static NamespacedKey PERM_ID = new NamespacedKey(Starlight.plugin, "faction_perm_id")

    FPerms() {
        GroovyScript.addUnloadHook {
            Factions.fCommand.subCommands.removeIf { it.aliases.find {
                it.equalsIgnoreCase("perms") || it.equalsIgnoreCase("access")
            } != null }

            Factions.fCommand.build()
        }

        commands()
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        fCommand.create("perms", "permissions", "perm", "permission").description("Display Faction Permissions").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (cmd.args().size() == 0) {
                    if (!member.isRoleAtleast(Role.COLEADER)) cmd.reply("§3You must be at least a §eCo-Leader §3to use this command.")
                    else openPermsPreview(faction, member)
                    return
                }
            }
        }

        fCommand.create("access").description("Display Faction Access").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
                if (!member.isRoleAtleast(Role.COLEADER)) cmd.reply("§3You must be at least a §eCo-Leader §3to use this command.")

                if (cmd.args().size() == 0) {
                    openAccessPreview(cmd.sender(), faction, member)
                    return
                }

                if (cmd.args().size() == 1) {
                    String name = cmd.arg(0).parseOrFail(String)
                    def target = Bukkit.getPlayer(name)
                    if (target != null) {
                        openPlayerAccessEdit(cmd.sender(), member, faction, Factions.getMember(target.getUniqueId(), false))
                        return
                    }

                    def targetFaction = Factions.getFactionByName(name)

                    if (targetFaction == null) {
                        cmd.reply("§3A faction with the name §e${name}§3 does not exist.")
                        return
                    }

                    openFactionAccessEdit(cmd.sender(), member, faction, targetFaction)
                }
            }
        }
    }

    static def openPermsPreview(Faction faction, Member member, int page = 1) {
        def player = Bukkit.getPlayer(member.getId())
        if (player == null) return

        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3Faction Permissions", new ArrayList<Permission>(Factions.defaultPermissions.values()), { Permission permission, Integer slot ->
            List<String> lore = new ArrayList<>(permission.description)
            lore.addAll([
                    "",
                    "§3Required Role: §e${permission.getFactionRequiredRole(faction).getDisplayName()}",
            ])

            if (permission.hasFactionAccess(faction) && permission.getFactionRequiredRole(faction) != permission.requiredRole) {
                lore.add("")
                lore.add("§aDEFAULT: §7${permission.requiredRole.getDisplayName()}")
            }

            def item = FastItemUtils.createItem(permission.material, "§3${permission.getName()}", lore, false)

            FastItemUtils.setCustomTag(item, PERM_ID, ItemTagType.STRING, permission.internalId)

            return item
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null) return

                    if (!FastItemUtils.hasTagFast(item, PERM_ID)) return

                    String internalId = FastItemUtils.getCustomTag(item, PERM_ID, ItemTagType.STRING)
                    if (internalId == null) return

                    selectRole(p) { role ->
                        def factionAccess = faction.getSelfAccess(internalId)
                        if (factionAccess == null) {
                            faction.addSelfAccess(new Access(internalId, role))
                        } else {
                            factionAccess.requiredRole = role
                        }

                        faction.queueSave()
                        openPermsPreview(faction, member, page)
                    }
                },
                { Player p, ClickType t, int s -> openPermsPreview(faction, member, page - 1) },
                { Player p, ClickType t, int s -> openPermsPreview(faction, member, page + 1) }
        ])

        menu.openSync(player)
    }

    static def openAccessPreview(Player player, Faction faction, Member member) {
        MenuBuilder menu = new MenuBuilder(27, "§3Faction Access")

        menu.set(2, 3, FastItemUtils.createItem(Material.BOOK, "§aPlayer Permissions", [
                "",
                "§aClick me to view the Player Permissions.",
                "§a${faction.getMemberAccess().size()}§7/${faction.getAccessData().size()}", // faction.getMaxAccessCount() TODO
                ""
        ], false), { p, t, s ->
            openPlayerAccess(player, faction, member)
        })

        menu.set(2, 5, FastItemUtils.createItem(Material.BOOK, "§cFaction Permissions", [
                "",
                "§cClick me to view the Faction Permissions.",
                "§a${faction.getFactionAccess().size()}§7/${faction.getAccessData().size()}", // faction.getMaxAccessCount() TODO
                ""
        ], false), { p, t, s ->
            openFactionAccess(player, faction, member)
        })

        menu.set(2, 7, FastItemUtils.createItem(Material.BOOK, "§aChunk Permissions", [
                "",
                "§aClick me to view the Chunk Permissions.",
                "§a${faction.getChunkAccess().size()}§7/${faction.getAccessData().size()}", // faction.getMaxAccessCount() TODO
                ""
        ], false), { p, t, s ->
            openPlayerAccess(player, faction, member)
        })

        menu.openSync(player)
    }

    private static NamespacedKey P_ACCESS_VIEW = new NamespacedKey(Starlight.plugin, "p_access_view")
    private static NamespacedKey F_ACCESS_VIEW = new NamespacedKey(Starlight.plugin, "f_access_view")
    private static NamespacedKey C_ACCESS_VIEW = new NamespacedKey(Starlight.plugin, "c_access_view")
    private static NamespacedKey C_ACCESS_TYPE = new NamespacedKey(Starlight.plugin, "c_access_type")

    static def openFactionAccess(Player player, Faction faction, Member member, int page = 1) {
        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3Faction Access §7(${faction.getFactionAccess().size()})", faction.getFactionAccess().toList(), { AccessData accessData, Integer slot ->
            def item = FastItemUtils.createItem(Material.PAPER, "§3${accessData.getRelation(faction).color + accessData.getTargetName()}", [], false)

            FastItemUtils.setCustomTag(item, F_ACCESS_VIEW, ItemTagType.STRING, accessData.getTargetId().toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.getType() == Material.AIR) return

                    if (!FastItemUtils.hasTagFast(item, F_ACCESS_VIEW)) return

                    String targetId = FastItemUtils.getCustomTag(item, F_ACCESS_VIEW, ItemTagType.STRING)
                    if (targetId == null) return

                    def targetFaction = Factions.getFaction(UUID.fromString(targetId), false)
                    if (targetFaction == null) return

                    openFactionAccessEdit(player, member, faction, targetFaction)
                },
                { Player p, ClickType t, int s -> openFactionAccess(player, faction, member, page + 1) },
                { Player p, ClickType t, int s -> openFactionAccess(player, faction, member, page - 1) },
                { Player p, ClickType t, int s -> openAccessPreview(player, faction, member) }
        ])

        menu.openSync(player)
    }

    static def openPlayerAccess(Player player, Faction faction, Member member, int page = 1) {
        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3Player Access §7(${faction.getMemberAccess().size()})", faction.getMemberAccess().toList(), { AccessData accessData, Integer slot ->
            def item = FastItemUtils.createItem(Material.PAPER, "§3${accessData.getRelation(faction).color + accessData.getTargetName()}", [], false)

            FastItemUtils.setCustomTag(item, P_ACCESS_VIEW, ItemTagType.STRING, accessData.getTargetId().toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.getType() == Material.AIR) return

                    if (!FastItemUtils.hasTagFast(item, P_ACCESS_VIEW)) return

                    String targetId = FastItemUtils.getCustomTag(item, P_ACCESS_VIEW, ItemTagType.STRING)
                    if (targetId == null) return

                    def targetMember = Factions.getMember(UUID.fromString(targetId), false)
                    if (targetMember == null) return

                    openPlayerAccessEdit(player, member, faction, targetMember)
                },
                { Player p, ClickType t, int s -> openPlayerAccess(player, faction, member, page + 1) },
                { Player p, ClickType t, int s -> openPlayerAccess(player, faction, member, page - 1) },
                { Player p, ClickType t, int s -> openAccessPreview(player, faction, member) }
        ])

        menu.openSync(player)
    }

    static def openChunkAccess(Player player, Faction faction, Member member, int page = 1) {
        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3Chunk Access §7(${faction.getChunkAccess().size()})", faction.getChunkAccess().toList(), { AccessData accessData, Integer slot ->
            def lore = [
                    "",
                    "§aClick me to view the Chunk Permissions.",
                    ""
            ]

            String type
            if (accessData.accessor == TargetType.PLAYER) {
                type = "§aPlayer"
            } else {
                type = "§cFaction"
            }

            lore.add("")
            lore.add("§aChunk: §7${accessData.accessChunk.getX()}§7, §7${accessData.accessChunk.getZ()}")
            lore.add("§aLocation: §7${accessData.accessChunk.getWorldName()} X:${accessData.accessChunk.getX() << 4} Y:${accessData.accessChunk.getZ() << 4}")
            lore.add("§aStart: §7${accessData.accessStart}")
            lore.add("§aEnd: §7${accessData.accessEnd == -1 ? "Never" : accessData.accessEnd}")
            lore.add("")
            lore.add("§3Accessor Type: $type")
            lore.add("§3Accessor: ${accessData.getTargetName()}")
            lore.add("")
            lore.add("§aClick me to view the Chunk Permissions.")

            def item = FastItemUtils.createItem(Material.PAPER, "§3${accessData.getRelation(faction).color + accessData.getTargetName()}", [], false)

            FastItemUtils.setCustomTag(item, C_ACCESS_VIEW, ItemTagType.STRING, accessData.getTargetId().toString())
            FastItemUtils.setCustomTag(item, C_ACCESS_TYPE, ItemTagType.STRING, accessData.accessor.name())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.getType() == Material.AIR) return

                    if (!FastItemUtils.hasTagFast(item, C_ACCESS_VIEW)) return

                    String targetId = FastItemUtils.getCustomTag(item, C_ACCESS_VIEW, ItemTagType.STRING)
                    if (targetId == null) return

                    String targetType = FastItemUtils.getCustomTag(item, C_ACCESS_TYPE, ItemTagType.STRING)
                    if (targetType == null) return

                    def type = TargetType.valueOf(targetType)

                    if (type == TargetType.PLAYER) {
                        def targetMember = Factions.getMember(UUID.fromString(targetId), false)
                        if (targetMember == null) return

                        openChunkAccessEdit(player, member, faction, accessData.accessChunk, UUID.fromString(targetId), type)
                    } else {
                        def targetFaction = Factions.getFaction(UUID.fromString(targetId), false)
                        if (targetFaction == null) return

                        openChunkAccessEdit(player, member, faction, accessData.accessChunk, UUID.fromString(targetId), type)
                    }

                    def targetMember = Factions.getMember(UUID.fromString(targetId), false)
                    if (targetMember == null) return

                    openChunkAccessEdit(player, member, faction, targetMember)
                },
                { Player p, ClickType t, int s -> openChunkAccess(player, faction, member, page + 1) },
                { Player p, ClickType t, int s -> openChunkAccess(player, faction, member, page - 1) },
                { Player p, ClickType t, int s -> openAccessPreview(player, faction, member) }
        ])

        menu.openSync(player)
    }

    private static NamespacedKey P_ACCESS_EDIT = new NamespacedKey(Starlight.plugin, "p_access_edit")
    private static NamespacedKey F_ACCESS_EDIT = new NamespacedKey(Starlight.plugin, "f_access_edit")
    private static NamespacedKey C_ACCESS_EDIT = new NamespacedKey(Starlight.plugin, "c_access_edit")

    static def openPlayerAccessEdit(Player viewer, Member member, Faction faction, Member targetMember, int page = 1) {
        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3${targetMember.getName()} Access", new ArrayList<Permission>(Factions.defaultPermissions.findAll {!it.value.baseFactionOnly }.values()), { Permission permission, Integer slot ->
            def access = faction.getAccess(targetMember.getId(), TargetType.PLAYER)
            if (access != null) {
                if (access.access.find { it.internalId == permission.internalId } != null) {
                    def item = FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§a${permission.getName()}", [
                            "",
                            "§aClick me to remove this permission.",
                            ""
                    ], false)

                    FastItemUtils.setCustomTag(item, P_ACCESS_EDIT, ItemTagType.STRING, permission.internalId)

                    return item
                }
            }

            def lore = [
                    "",
                    "§aClick me to add this permission.",
                    ""
            ]

            lore.addAll(permission.description)

            def item = FastItemUtils.createItem(permission.material, "§3${permission.getName()}", lore, false)

            FastItemUtils.setCustomTag(item, P_ACCESS_EDIT, ItemTagType.STRING, permission.internalId)

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null) return

                    if (!FastItemUtils.hasTagFast(item, P_ACCESS_EDIT)) return

                    String internalId = FastItemUtils.getCustomTag(item, P_ACCESS_EDIT, ItemTagType.STRING)
                    if (internalId == null) return

                    //true
                    def playerAccess = faction.getAccess(targetMember.getId(), TargetType.PLAYER)
                    if (playerAccess == null) {
                        AccessData accessData = new AccessData(targetMember.getId(), TargetType.PLAYER)
                        accessData.access.add(new Access(internalId))
                        faction.addAccess(accessData)
                    } else {
                        if (playerAccess.access.find { it.internalId == internalId } != null) {
                            playerAccess.access.removeIf { it.internalId == internalId }

                            if (playerAccess.access.isEmpty()) {
                                faction.removeAccess(playerAccess) // clear empty data
                            }
                        } else {
                            playerAccess.access.add(new Access(internalId))
                        }
                    }

                    faction.queueSave()
                    openPlayerAccessEdit(viewer, member, faction, targetMember, page)
                },
                { Player p, ClickType t, int s -> openPlayerAccessEdit(viewer, member, faction, targetMember, page + 1) },
                { Player p, ClickType t, int s -> openPlayerAccessEdit(viewer, member, faction, targetMember, page - 1) },
                { Player p, ClickType t, int s -> openAccessPreview(viewer, faction, member) }
        ])

        menu.openSync(viewer)
    }

    static def openFactionAccessEdit(Player viewer, Member member, Faction faction, Faction targetFaction, int page = 1) {
        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3${targetFaction.getName()} Access", new ArrayList<Permission>(Factions.defaultPermissions.findAll {!it.value.baseFactionOnly }.values()), { Permission permission, Integer slot ->
            def access = faction.getAccess(targetFaction.getFactionId(), TargetType.FACTION)
            if (access != null) {
                if (access.access.find { it.internalId == permission.internalId } != null) {
                    def item = FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§a${permission.getName()}", [
                            "",
                            "§aClick me to remove this permission.",
                            ""
                    ], false)

                    FastItemUtils.setCustomTag(item, F_ACCESS_EDIT, ItemTagType.STRING, permission.internalId)

                    return item
                }
            }

            def lore = [
                    "",
                    "§aClick me to add this permission.",
                    ""
            ]

            lore.addAll(permission.description)

            def item = FastItemUtils.createItem(permission.material, "§3${permission.getName()}", lore, false)

            FastItemUtils.setCustomTag(item, F_ACCESS_EDIT, ItemTagType.STRING, permission.internalId)

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null) return

                    if (!FastItemUtils.hasTagFast(item, F_ACCESS_EDIT)) return

                    String internalId = FastItemUtils.getCustomTag(item, F_ACCESS_EDIT, ItemTagType.STRING)
                    if (internalId == null) return

                    //true
                    def factionAccess = faction.getAccess(targetFaction.getFactionId(), TargetType.FACTION)
                    if (factionAccess == null) {
                        AccessData accessData = new AccessData(targetFaction.getFactionId(), TargetType.FACTION)
                        accessData.access.add(new Access(internalId))
                        faction.addAccess(accessData)
                    } else {
                        if (factionAccess.access.find { it.internalId == internalId } != null) {
                            factionAccess.access.removeIf { it.internalId == internalId }

                            if (factionAccess.access.isEmpty()) {
                                faction.removeAccess(factionAccess) // clear empty data
                            }
                        } else {
                            factionAccess.access.add(new Access(internalId))
                        }
                    }

                    faction.queueSave()
                    openFactionAccessEdit(viewer, member, faction, targetFaction, page)
                },
                { Player p, ClickType t, int s -> openFactionAccessEdit(viewer, member, faction, targetFaction, page + 1) },
                { Player p, ClickType t, int s -> openFactionAccessEdit(viewer, member, faction, targetFaction, page - 1) },
                { Player p, ClickType t, int s -> openAccessPreview(viewer, faction, member) }
        ])

        menu.openSync(viewer)
    }

    static def openChunkAccessEdit(Player viewer, Member member, Faction faction, CL targetChunk, UUID target, TargetType targetType, int page = 1) {
        MenuBuilder menu

        String title
        String type
        if (TargetType.PLAYER == targetType) {
            def player = Bukkit.getPlayer(target)
            title = "§3${player ? player.name : Bukkit.getOfflinePlayer(target).name} Access"
        } else {
            title = "§3${Factions.getFaction(target, false).getName()} Access"
        }

        menu = MenuUtils.createPagedMenu("§3${title} Access", new ArrayList<Permission>(Factions.defaultPermissions.findAll {!it.value.baseFactionOnly }.values()), { Permission permission, Integer slot ->
            def access = faction.getAccess(target, targetType)
            if (access != null) {
                if (access.access.find { it.internalId == permission.internalId } != null) {
                    def lore = [
                            "",
                            "§aClick me to remove this permission.",
                            ""
                    ]

                    def item = FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§a${permission.getName()}", [
                            "",
                            "§aClick me to remove this permission.",
                            ""
                    ], false)

                    FastItemUtils.setCustomTag(item, F_ACCESS_EDIT, ItemTagType.STRING, permission.internalId)

                    return item
                }
            }

            def lore = [
                    "",
                    "§aClick me to add this permission.",
                    ""
            ]

            lore.addAll(permission.description)

            def item = FastItemUtils.createItem(permission.material, "§3${permission.getName()}", lore, false)

            FastItemUtils.setCustomTag(item, C_ACCESS_EDIT, ItemTagType.STRING, permission.internalId)

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null) return

                    if (!FastItemUtils.hasTagFast(item, C_ACCESS_EDIT)) return

                    String internalId = FastItemUtils.getCustomTag(item, C_ACCESS_EDIT, ItemTagType.STRING)
                    if (internalId == null) return

                    //true
                    def chunkAccess = faction.getAccess(target, targetType)
                    if (chunkAccess == null) {
                        AccessData accessData = new AccessData(target, targetType)
                        accessData.accessChunk = targetChunk
                        accessData.access.add(new Access(internalId))
                        faction.addAccess(accessData)
                    } else {
                        if (chunkAccess.access.find { it.internalId == internalId } != null) {
                            chunkAccess.access.removeIf { it.internalId == internalId }

                            if (chunkAccess.access.isEmpty()) {
                                faction.removeAccess(chunkAccess) // clear empty data
                            }
                        } else {
                            chunkAccess.access.add(new Access(internalId))
                        }
                    }

                    faction.queueSave()
                    openChunkAccessEdit(viewer, member, faction, targetChunk, target, targetType, page)
                },
                { Player p, ClickType t, int s -> openChunkAccessEdit(viewer, member, faction, targetChunk, target, targetType, page + 1) },
                { Player p, ClickType t, int s -> openChunkAccessEdit(viewer, member, faction, targetChunk, target, targetType, page - 1) },
                { Player p, ClickType t, int s -> openAccessPreview(viewer, faction, member) }
        ])

        menu.openSync(viewer)
    }

    static def selectRole(Player player, Callback<Role> callback) {
        MenuBuilder builder = new MenuBuilder(9, "§7Select a Role")
        int index = 0
        Role.values().each { role ->
            if (role == Role.ADMIN) return

            def lore = ["",
                        "§fClick me to select role §7${role.getDisplayName()}",
                        ""
            ]

            if (role == Role.SYSTEM) {
                lore.add("§c§nWARNING:§c This role is for ALL players only.")
            }

            def stack = FastItemUtils.createItem(
                    Material.PAPER,
                    "§7${role.getDisplayName()}",
                    lore,
                    false
            )

            builder.set(index, stack, { p, t, s ->
                return callback.exec(role)
            })

            index++
        }

        builder.openSync(player)
    }

}
