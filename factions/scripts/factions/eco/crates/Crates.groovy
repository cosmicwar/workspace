package scripts.factions.eco.crates

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.core.faction.FCBuilder
import scripts.factions.data.obj.Position
import scripts.factions.data.uuid.UUIDDataManager
import scripts.factions.eco.crates.api.Crate
import scripts.factions.eco.loottable.LootTableHandler
import scripts.factions.util.PromptUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils

import static scripts.shared.legacy.utils.MenuUtils.createPagedMenu

@CompileStatic(TypeCheckingMode.SKIP)
class Crates {

    static NamespacedKey placedCrateKey = new NamespacedKey(Starlight.plugin, "placedCrateKey")

    Map<UUID, Crate> editingLocations = [:]

    Crates() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(Crate.class).saveAll(false)
        }

        UUIDDataManager.register("crates", Crate.class)

        Schedulers.sync().execute {
            UUIDDataManager.getAllData(Crate.class).each {
                it.loadCrates()
            }
        }

        commands()
        events()
    }

    static Collection<Crate> getCrates() { return UUIDDataManager.getAllData(Crate.class) }
    static boolean hasCrate(String name) { return getCrates().any { it.internalName == name } }
    static Crate getCrate(String name) { return getCrates().find { it.internalName == name } }
    static Crate getCrate(UUID uuid) { return UUIDDataManager.getData(uuid, Crate.class) }

    def deleteCrate(Crate crate) {
        crate.placedCrates.each {
            if (it.key.block.type == Material.CHEST) {
                it.key.block.type = Material.AIR
            }

            it.value.removeHologram()
        }

        UUIDDataManager.getByClass(Crate.class).delete(crate.id)
    }

    def commands() {
        FCBuilder command = new FCBuilder("crate", "crates").defaultAction {
            if (it.isOp()) {
                openCrates(it)
            }
        }

        command.build()
    }

    def events() {
        Events.subscribe(BlockPlaceEvent.class).handler {event ->
            def crate = editingLocations[event.player.uniqueId]
            if (crate == null) return

            def ps = Position.of(event.blockPlaced.location)

            if (crate.placedLocations.contains(ps)) {
                event.player.sendMessage("§cCrate already placed here")
                return
            }

            crate.placedLocations.add(ps)
            crate.queueSave()

            event.player.sendMessage("§aCrate placed")
        }

        Events.subscribe(BlockBreakEvent.class).handler {event ->

        }

        Events.subscribe(PlayerInteractEvent.class).handler {event ->
            def block = event.clickedBlock
            if (block == null || block.type != Material.CHEST) return

            def chest = block.state as Chest
            if (DataUtils.hasTag(chest, placedCrateKey, PersistentDataType.STRING)) {
                def id = UUID.fromString(DataUtils.getTag(chest, placedCrateKey, PersistentDataType.STRING))
                def crate = getCrate(id)
                if (crate == null) return

                event.player.sendMessage("interacted w/ crate")
            }
        }
    }

    NamespacedKey crateIdKey = new NamespacedKey(Starlight.plugin, "crate_editor_id")

    def openCrates(Player player, int page = 1) {
        MenuBuilder menu

        menu = createPagedMenu("§bCrates", getCrates().toList(), { Crate crate, Integer slot ->
            def item = FastItemUtils.createItem(Material.CHEST, "§b" + crate.internalName, [
                "§7Click to edit this crate",
            ])

            DataUtils.setTag(item, crateIdKey, PersistentDataType.STRING, crate.id.toString())

            return item
        }, page, false, [
                { Player p, ClickType t, Integer slot ->
                    def item = menu.get().getItem(slot)

                    if (item == null || item.type.isAir()) return

                    def id = DataUtils.getTag(item, crateIdKey, PersistentDataType.STRING)
                    try {
                        def crate = getCrate(UUID.fromString(id))
                        if (crate == null) return

                        openCrateEditor(p, crate)
                    } catch (Exception ignored) {}
                },
                { Player p, ClickType t, Integer slot ->
                    openCrates(p, page + 1)
                },
                { Player p, ClickType t, Integer slot ->
                    openCrates(p, page - 1)
                },
        ])

        menu.set(menu.get().size - 4, FastItemUtils.createItem(Material.LIME_DYE, "§aCreate a new crate", []), { p, t, s ->
            p.closeInventory()
            
            PromptUtils.prompt(p, "Enter the name of the crate", { name ->
                if (hasCrate(name)) {
                    p.sendMessage("§cA crate with that name already exists")
                    return
                }

                def crate = getCrate(UUID.randomUUID())
                crate.internalName = name

                crate.queueSave()

                openCrateEditor(p, crate)
            })
        })

        menu.openSync(player)
    }

    def openCrateEditor(Player player, Crate crate) {
        MenuBuilder menu = new MenuBuilder(18, "§bEditing " + crate.internalName)

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.RED_STAINED_GLASS, "§cDelete this crate", []), { p, t, s ->
            MenuUtils.createConfirmMenu(p, "§cDelete Crate?", FastItemUtils.createItem(Material.BARRIER, "§cDelete?", []), {
                deleteCrate(crate)
                openCrates(p)
            }, {
                openCrateEditor(p, crate)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§b${crate.crateKeyName}", [
                ""
        ], false), { p, t, s ->
            PromptUtils.prompt(p, "Enter the name of the crate key", { name ->
                crate.crateKeyName = name.replaceAll('&', '§')
                crate.queueSave()
                openCrateEditor(p, crate)
            })
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§bKey Lore", crate.crateKeyLore, false), { p, t, s ->
            SelectionUtils.selectStringList(p, crate.crateKeyLore) {
                crate.crateKeyLore = it
                crate.queueSave()
                openCrateEditor(p, crate)
            }
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(crate.crateKeyMaterial, "§bKey Material", [
                "§7Current: §b${crate.crateKeyMaterial.toString()}",
                "§aClick to change the material"
        ], false), { p, t, s ->
            SelectionUtils.selectMaterial(p) {
                crate.crateKeyMaterial = it
                crate.queueSave()
                openCrateEditor(p, crate)
            }
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§bKey Custom Model Data", [
                "§7Current: §b${crate.crateKeyCustomModelData}",
                "§aClick to change the model data"
        ], false), { p, t, s ->
            SelectionUtils.selectInteger(p, "§aEnter the custom model data", [0,1,2,3,4,5,6,7]) {
                crate.crateKeyCustomModelData = it
                crate.queueSave()
                openCrateEditor(p, crate)
            }
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§bPlaced Locations", [
                "§7Click to enable "
        ], false), { p, t, s ->
            if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                crate.placedLocations.clear()
                crate.queueSave()
                openCrateEditor(p, crate)
            } else {
                SelectionUtils.selectPositionList(p, crate.placedLocations.toList()) {
                    crate.placedLocations = it
                    crate.queueSave()
                    openCrateEditor(p, crate)
                }
            }
        })

        def lore = [
                "§aThe §bloot table §ais what determines the §brewards",
                "§athat can be obtained from this §bcrate§a.",
        ]

        if (crate.lootTableId != null) {
            lore.add("")
            lore.add("§aclick to view")
            lore.add("§aright-click to assign")
            lore.add("")
            lore.add("§aCurrent: §b${crate.getLootTable().getName()}")
        } else {
            lore.add("")
            lore.add("§7click to assign")
        }

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.NETHERITE_INGOT, "§a§lLoot-Table", lore, false), { p, t, s ->
            if (crate.lootTableId != null) {
                if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                    LootTableHandler.openCategories(p, 1, {
                        if (it == null) {
                            openCrateEditor(p, crate)
                        } else {
                            crate.lootTableId = it.getInternalId()
                            crate.queueSave()
                            openCrateEditor(p, crate)
                        }
                    })
                } else {
                    LootTableHandler.openTableGui(p, crate.getLootTable(), 1, {
                        openCrateEditor(p, crate)
                    }, {
                        crate.queueSave()
                    })
                }
            } else {
                LootTableHandler.openCategories(p, 1, {
                    if (it == null) {
                        openCrateEditor(p, crate)
                    } else {
                        crate.lootTableId = it.getInternalId()
                        crate.queueSave()
                        openCrateEditor(p, crate)
                    }
                })
            }
        })

        menu.set(17, FastItemUtils.createItem(Material.RED_DYE, "§cBack", []), { p, t, s ->
            openCrates(p)
        })

        menu.openSync(player)
    }

}
