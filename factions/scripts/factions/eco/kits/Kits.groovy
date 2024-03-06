package scripts.factions.eco.kits

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.shared.core.cfg.utils.SelectionUtils
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.core.cfg.utils.PromptUtils
import scripts.shared.legacy.CooldownUtils
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.*
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent
import scripts.shared.utils.DataUtils

import java.util.concurrent.ConcurrentHashMap

@CompileStatic(TypeCheckingMode.SKIP)
class Kits {

    static Map<UUID, KitEditEntry> edittingKits = new ConcurrentHashMap<>()

    Kits() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(Kit).saveAll(false)
        }

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            UUIDDataManager.getByClass(Kit.class).saveAll(false)
        })

        UUIDDataManager.register("kits", Kit)

        commands()
    }

    static def commands() {
        SubCommandBuilder kits = new SubCommandBuilder("kits", "kit")

        kits.defaultAction {
            openKitMenu(it.getPlayer(), KitType.NORMAL)
        }

        kits.create("createkit").requirePermission("kits.*").usage("<name> <time (s)>").register { ctx ->
            String name = ctx.arg(0).parseOrFail(String.class)
            Integer time = ctx.arg(1).parseOrFail(Integer.class)

            def kit = getKitFromName(name)
            if (kit != null) {
                Players.msg(ctx.sender(), "§c§lKITS §> §fA kit with that name already exists!")
                return
            }

            kit = UUIDDataManager.getData(UUID.randomUUID(), Kit.class, true)
            kit.name = name
            kit.setCd(time)
            kit.queueSave()
            Players.msg(ctx.sender(), "§6§lKITS §> §fYou have created the §6${name} §fkit!")
        }

        kits.create("edit").requirePermission("kits.*").usage("<kit>").register {
            String name = it.arg(0).parseOrFail(String.class)

            Kit kit = getKitFromName(name)
            if (kit == null) return

            openKitEditor(it.sender(), kit)
            kit.queueSave()
        }

        kits.build()

        Commands.create().assertPlayer().handler { ctx ->
            openKitMenu(ctx.sender(), KitType.GKIT)
        }.register("gkits", "gkit")

        Commands.create().assertPlayer().handler { ctx ->
            openKitMenu(ctx.sender(), KitType.VKIT)
        }.register("vkits", "vkit")
    }

    private static NamespacedKey kitKey = new NamespacedKey(Starlight.plugin, "kit_key")

    static def openKitMenu(Player player, KitType type, int page = 1) {
        MenuBuilder menu

        def kits = getKits().findAll { it.type == type }.toList()

        menu = MenuUtils.createPagedMenu(type.title, kits, { Kit kit, Integer slot ->
            def lore = [
                    "§7${kit.description}",
                    "",
                    "§7Priority: §f${kit.priority}",
                    "§7Items: §f${kit.items.size()}",
                    "",
                    "§7Click to redeem!"
            ]

            if (player.isOp()) {
                lore.add("")
                lore.add("§7§oStaff - §aRight click to edit!")
            }

            def item = FastItemUtils.createItem(kit.inventoryMaterial, "§a${kit.name}", lore, false)

            DataUtils.setTag(item, kitKey, PersistentDataType.STRING, kit.id.toString())

            return item
        }, page, false, [
                { Player p, ClickType clickType, Integer slot ->
                    def item = menu.get().getItem(slot)
                    if (item == null || item.type.isAir()) return

                    String kitId = DataUtils.getTag(item, kitKey, PersistentDataType.STRING)
                    if (kitId == null) return

                    def id = UUID.fromString(kitId)

                    def kit = getKits().find { it.id == id && it.type == type }
                    if (kit == null) return


                    if (clickType == ClickType.RIGHT && p.isOp()) {
                        openKitEditor(p, kit)
                        return
                    }

                    def cd = p.isOp() ? -1 : kit.cd
                    if (cd != null && cd > 0) {
                        def remainingCd = CooldownUtils.get(player, "${type.name().toLowerCase()}_${kit.id}", cd * 1000L)
                        // convert to seconds

                        if (remainingCd > 0 && !player.isOp()) {
                            Players.msg(player, "§! §> §fYou must wait §e${TimeUtils.getTimeAmount(remainingCd)} §fbefore using that kit again!")
                            return
                        }

                        CooldownUtils.set(player, "${type.name().toLowerCase()}_${kit.id}")
                    }

                    giveKit(player, kit)
                    Players.msg(player, "§6§lKITS §> §fYou have redeemed the §6${kit.name} §fkit!")
                },
                { Player p, ClickType clickType, Integer slot ->
                    openKitMenu(p, type, page + 1)
                },
                { Player p, ClickType clickType, Integer slot ->
                    openKitMenu(p, type, page - 1)
                }
        ] as List<MenuEvent>)

        menu.openSync(player)
    }

    static def openKitEditor(Player player, Kit kit) {
        if (kit == null) return

        MenuBuilder menu

        menu = new MenuBuilder(54, "§6§lKITS §> §fEditing ${kit.name}")

        def kitEntry = new KitEditEntry(kit)

        edittingKits.put(player.getUniqueId(), kitEntry)

        def items = getKitItems(kit)
        items.eachWithIndex { ItemStack entry, int i ->
            menu.set(i, entry, { Player p, ClickType clickType, Integer slot ->
                if (clickType == ClickType.RIGHT) {
                    kit.items.remove(FastItemUtils.convertItemStackToString(entry))
                    kit.queueSave()
                    openKitEditor(p, kit)
                }
            })
        }

        def row = 5
        def armorContents = getKitArmorContents(kit)
        armorContents.eachWithIndex { ItemStack entry, int i ->
            menu.set(row, i, entry, { Player p, ClickType clickType, Integer slot ->
                if (clickType == ClickType.RIGHT) {
                    kit.armorContents.remove(FastItemUtils.convertItemStackToString(entry))
                    kit.queueSave()
                    openKitEditor(p, kit)
                }
            })
        }

        menu.set(6, 1, FastItemUtils.createItem(Material.BARRIER, "§c§lDELETE KIT", ["§7Click to delete this kit!"], false), { Player p, ClickType clickType, Integer slot ->
            UUIDDataManager.removeOne(kit.id, Kit)
            Players.msg(p, "§6§lKITS §> §fYou have deleted the §6${kit.name} §fkit!")

            openKitMenu(p, kit.type)
        })

        menu.set(6, 2, FastItemUtils.createItem(Material.NAME_TAG, "§6§lRENAME KIT", ["§7Click to rename this kit!"], false), { Player p, ClickType clickType, Integer slot ->
            PromptUtils.prompt(player, "§6§lKITS §> §fType the new name for this kit in chat!") {
                it = it.replace('&', '§')

                kit.name = it
                kit.queueSave()
                openKitEditor(p, kit)
            }
        })

        menu.set(6, 3, FastItemUtils.createItem(Material.PAPER, "§6§lEDIT DESCRIPTION", ["§7Click to edit this kit's description!"], false), { Player p, ClickType clickType, Integer slot ->
            SelectionUtils.selectStringList(p, kit.description) {
                it = it.collect { it.replace('&', '§') }

                kit.description = it
                kit.queueSave()
                openKitEditor(p, kit)
            }
        })

        menu.set(6, 4, FastItemUtils.createItem(kit.inventoryMaterial, "§6§lEDIT INVENTORY ITEM", ["§7Click to edit this kit's inventory item!"], false), { Player p, ClickType clickType, Integer slot ->
            SelectionUtils.selectMaterial(p) {
                if (it != null || it != Material.AIR) {
                    kit.inventoryMaterial = it
                    kit.queueSave()
                    openKitEditor(p, kit)
                }
            }
        })

        // change cooldown
        menu.set(6, 5, FastItemUtils.createItem(Material.CLOCK, "§6§lEDIT COOLDOWN", ["§7Click to edit this kit's cooldown!"], false), { Player p, ClickType clickType, Integer slot ->
            SelectionUtils.selectInteger(p) {
                kit.cd = it
                kit.queueSave()
                openKitEditor(p, kit)
            }
        })

        // change priority
        menu.set(6, 6, FastItemUtils.createItem(Material.ARROW, "§6§lEDIT PRIORITY", ["§7Click to edit this kit's priority!"], false), { Player p, ClickType clickType, Integer slot ->
            SelectionUtils.selectInteger(p) {
                kit.priority = it
                kit.queueSave()
                openKitEditor(p, kit)
            }
        })

        // back button
        menu.set(6, 8, FastItemUtils.createItem(Material.RED_DYE, "§6§lBACK", ["§7Click to go back!"], false), { Player p, ClickType clickType, Integer slot ->
            openKitMenu(p, kit.type)
        })

        menu.setCloseCallback {edittingKits.remove(player.getUniqueId())}

        menu.setExternal { p, t, s ->
            if (!p.isOp()) return

            if (s > 36 || s < 0) return

            ItemStack stack = p.getInventory().getItem(s)
            if (stack == null || stack.type == Material.AIR) return

            MenuUtils.createConfirmMenu(player, "§8Add item", stack, () -> {
                Players.msg(player, "§] §> §cAdded this item to kit ${kit.name}.")

                if (EnchantmentTarget.ARMOR.includes(stack)) {
                    kit.armorContents.add(FastItemUtils.convertItemStackToString(stack))
                } else {
                    kit.items.add(FastItemUtils.convertItemStackToString(stack))
                }

                kit.queueSave()

                Schedulers.sync().runLater({
                    openKitEditor(player, kit)
                }, 1)
            }, () -> {
                Players.msg(player, "§] §> §cSuccessfully stopped adding this reward")
                openKitEditor(player, kit)
            })
        }

        menu.openSync(player)
    }

    static List<ItemStack> getKitItems(Kit kit) {
        def items = kit.items.collect {
            FastItemUtils.convertStringToItemStack(it)
        }.findAll({ it != null })

        return items
    }

    static List<ItemStack> getKitArmorContents(Kit kit) {
        def items = kit.armorContents.collect {
            FastItemUtils.convertStringToItemStack(it)
        }.findAll({ it != null })

        return items
    }

    static def giveKit(Player player, Kit kit) {
        def items = getKitItems(kit)
        def armor = getKitArmorContents(kit)

        armor.each {
            FastInventoryUtils.equipAddOrBox(it, player)
        }
        items.each {
            FastInventoryUtils.addOrBox(player.uniqueId, player, null, it, null)
        }
    }

    static Kit getKitFromName(String name) {
        if (name == null) return null
        for (Kit kit : getKits() ) {
            if (kit.name.trim().toLowerCase() == name.trim().toLowerCase()) return kit
        }
        BroadcastUtils.broadcast("failed to find kit")
        return null
    }

    static Collection<Kit> getKits() {
        return UUIDDataManager.getAllData(Kit.class)
    }
}

class KitEditEntry {
    Kit kit
    List<ItemStack> items

    KitEditEntry(Kit kit) {
        this.kit = kit
        this.items = new ArrayList<>()
    }
}
