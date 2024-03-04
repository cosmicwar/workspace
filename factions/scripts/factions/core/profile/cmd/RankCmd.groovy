package scripts.factions.core.profile.cmd

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.starcade.starlight.Starlight
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.profile.Profiles
import scripts.factions.core.profile.rank.Rank
import scripts.factions.core.profile.rank.permission.RankPermission
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils

class RankCmd {

    RankCmd() {
        commands()
    }

    static def commands() {
        FCBuilder rank = new FCBuilder("rank", "ranks").defaultAction {
            if (it.isOp()) {
                openRankEditor(it)
            }
        }

        rank.build()
    }

    private static NamespacedKey rankKey = new NamespacedKey(Starlight.plugin, "rankEditKey")
    private static NamespacedKey rankPermissionKey = new NamespacedKey(Starlight.plugin, "rankPermissionKey")

    static def openRankEditor(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§6Rank Editor", Profiles.getRanks().toList(), { Rank rank, Integer slot ->
            def item = FastItemUtils.createItem(Material.PAPER, "${rank.nameColor}${rank.internalName}", [
                    "§bPriority: §e${rank.priority}",
                    "§bPrefix: §f${rank.prefix}",
                    "§bSuffix: §f${rank.suffix}",
                    "§bName Color: §f${rank.nameColor}${player.name}",
                    "§bChat Color: §f${rank.chatColor}TEST",
                    "§bPermissions: §a${rank.templePermissions.size()}§7/§c${rank.permissions.size()}",
                    "",
                    "§aClick to edit"
            ])

            DataUtils.setTagString(item, rankKey, rank.id.toString())

            return item
        }, page, false, [
                { Player p, ClickType t, int s ->
                    if (!p.isOp()) return

                    def item = menu.get().getItem(s)

                    if (item == null || item.type.isAir() || !DataUtils.hasTagString(item, rankKey)) return

                    def id = UUID.fromString(DataUtils.getTagString(item, rankKey))
                    def rank = Profiles.getRank(id, false)

                    if (rank == null) return

                    openRankEditor(p, rank)
                },
                { Player p, ClickType t, int s -> openRankEditor(p, page + 1) },
                { Player p, ClickType t, int s -> openRankEditor(p, page - 1) },
        ])

        menu.set(menu.get().size - 4, FastItemUtils.createItem(Material.LIME_DYE, "§bAdd Rank", [
                "§aClick to add a new rank."
        ]), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new rank name...", { input ->
                def rank = Profiles.getRank(UUID.randomUUID(), true)
                rank.internalName = input
                rank.queueSave()

                openRankEditor(p, rank)
            })
        })

        menu.openSync(player)
    }

    static def openRankEditor(Player player, Rank rank) {
        MenuBuilder menu = new MenuBuilder(18, "§6Rank Editor")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.NAME_TAG, "§bInternal Name", [
                "§f${rank.internalName}",
                "",
                "§aClick to edit"
        ], false), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new internal name...", { input ->
                rank.internalName = input
                rank.queueSave()

                openRankEditor(p, rank)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.OAK_SIGN, "§bPriority", [
                "§f${rank.priority}",
                "",
                "§aClick to edit"
        ], false), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectInteger(p, "§bEnter new priority...", [5, 25, 50, 100, 250, 500, 750, 1000], { input ->
                rank.priority = input
                rank.queueSave()

                openRankEditor(p, rank)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BOOK, "§bPrefix", [
                "§f${rank.prefix}",
                "",
                "§aClick to edit"
        ], false), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new prefix...", { input ->
                input = input.replace("&", "§")

                rank.prefix = input
                rank.queueSave()

                openRankEditor(p, rank)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BOOK, "§bSuffix", [
                "§f${rank.suffix}",
                "",
                "§aClick to edit"
        ], false), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new suffix...", { input ->
                input = input.replace("&", "§")

                rank.suffix = input
                rank.queueSave()

                openRankEditor(p, rank)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.WHITE_STAINED_GLASS, "§bName Color", [
                "§f${rank.nameColor}",
                "",
                "§aClick to edit"
        ], false), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new name color...", { input ->
                input = input.replace("&", "§")

                rank.nameColor = input
                rank.queueSave()

                openRankEditor(p, rank)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BLUE_STAINED_GLASS, "§bChat Color", [
                "§f${rank.chatColor}",
                "",
                "§aClick to edit"
        ], false), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new chat color...", { input ->
                input = input.replace("&", "§")

                rank.chatColor = input
                rank.queueSave()

                openRankEditor(p, rank)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§bPermissions", [
                "§bTotal: §a${rank.templePermissions.size()}§7/§c${rank.permissions.size()}",
                "",
                "§aClick to edit"
        ], false), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            openRankPermissions(p, rank)
        })

        menu.set(menu.get().getSize() - 1, FastItemUtils.createItem(Material.RED_DYE, "§cBack", ["§7Return to rank list"]), {p, t, s ->
            openRankEditor(p)
        })

        menu.setCloseCallback {
            rank.queueSave()
            Profiles.updateRank(rank, true)
        }

        menu.openSync(player)
    }

    static def openRankPermissions(Player player, Rank rank, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§6Rank Permissions", rank.permissions.toList(), { RankPermission permission, Integer slot ->
            def item = FastItemUtils.createItem(Material.PAPER, "§b${permission.permission}", [
                    "§aClick to edit"
            ])

            DataUtils.setTagString(item, rankPermissionKey, permission.permissionId.toString())

            return item
        }, page, true, [
            { Player p, ClickType t, int s ->
                if (!p.isOp()) return

                def item = menu.get().getItem(s)
                if (item == null || item.type.isAir() || !DataUtils.hasTagString(item, rankPermissionKey)) return

                def id = UUID.fromString(DataUtils.getTagString(item, rankPermissionKey))

                def permission = rank.permissions.find { it.permissionId == id }
                if (permission == null) return

                openRankPermission(p, rank, permission)
            },
            { Player p, ClickType t, int s -> openRankPermissions(p, rank, page + 1) },
            { Player p, ClickType t, int s -> openRankPermissions(p, rank, page - 1) },
            { Player p, ClickType t, int s -> openRankEditor(p, rank) }
        ])

        menu.set(menu.get().size - 4, FastItemUtils.createItem(Material.LIME_DYE, "§bAdd Permission", ["§7Add a new permission to this rank"]), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new permission...", { input ->
                def permission = new RankPermission(input)
                rank.permissions.add(permission)
                rank.queueSave()

                openRankPermission(p, rank, permission)
            })
        })

        menu.setCloseCallback {
            rank.queueSave()
            Profiles.updateRank(rank, true)
        }

        menu.openSync(player)
    }

    static def openRankPermission(Player player, Rank rank, RankPermission permission) {
        MenuBuilder menu = new MenuBuilder(18, "§6Rank Permission Editor")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.RED_STAINED_GLASS, "§cDelete Permission", [
                "§aCurrent: §7${permission.permission}",
                "",
                "§7Click to delete this permission"
        ]), {p, t, s ->
            if (!p.isOp()) return

            rank.permissions.remove(permission)
            rank.queueSave()

            openRankEditor(p, rank)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§bPermission", [
                "§aCurrent: §7${permission.permission}",
                "",
                "§7Click to edit"
        ]), {p, t, s ->
            if (!p.isOp()) return

            p.closeInventory()
            SelectionUtils.selectString(p, "§bEnter new permission...", { input ->
                permission.permission = input
                rank.queueSave()

                openRankPermission(p, rank, permission)
            })
        })

        menu.set(menu.get().size - 1, FastItemUtils.createItem(Material.RED_DYE, "§cBack", ["§7Return to rank permissions"]), {p, t, s ->
            openRankPermissions(p, rank)
        })

        menu.openSync(player)
    }
}

