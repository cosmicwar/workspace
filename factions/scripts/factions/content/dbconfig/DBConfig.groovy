package scripts.factions.content.dbconfig


import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.CLEntry
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.SREntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.list.MaterialListEntry
import scripts.factions.content.dbconfig.entries.list.PositionListEntry
import scripts.factions.content.dbconfig.entries.list.SRListEntry
import scripts.factions.content.dbconfig.entries.list.StringListEntry
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.data.DataManager
import scripts.factions.data.obj.CL
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.*
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent
import scripts.shared.utils.DataUtils

@CompileStatic(TypeCheckingMode.SKIP)
class DBConfig {

    static void main(String[] args) {
        DataManager.register("dbconfig", Config.class)

        GroovyScript.addUnloadHook {
            DataManager.getByClass(Config.class).saveAll(false)

            Starlight.unload("~/DBConfigUtil.groovy")
        }

//        FactionDataManager.getAllData(RegularConfig.class).forEach { config ->
//        }

        Exports.ptr("dbcfg:create", { String configId, String displayName, List<String> description = [], Material material = Material.BOOK ->
            return createConfig(configId, displayName, description, material)
        })

        Exports.ptr("dbcfg:get", { String configId, int page = 1 ->
            return getConfig(configId)
        })

        Starlight.watch("~/DBConfigUtil.groovy")

        commands()
    }

    static Config getConfig(String id) {
        return DataManager.getData(id, Config.class)
    }

    static Collection<Config> getConfigs() {
        return DataManager.getAllData(Config.class)
    }

    static Config createConfig(String configId, String displayName, List<String> description = [], Material material = Material.BOOK) {
        Config config = DataManager.getData(configId, Config.class)

        config.displayName = displayName
        config.description = description
        config.material = material

        config.queueSave()

        return config
    }

    static def commands() {
        SubCommandBuilder builder = new SubCommandBuilder("dbconfig", "serverconfig", "cfg").defaultAction { player ->
            Schedulers.async().execute {
                if (!player.isOp()) return

                openConfigGui(player)
            }
        }

        builder.create("testregion").requirePermission("*").register { ctx ->
            def sr = SelectionUtils.getSelection(ctx.sender())
            if (sr == null) {
                ctx.reply("§cYou must make a WorldEdit selection first!")
            } else {
                ctx.reply("x1:${sr.x1} y1:${sr.y1} z1:${sr.z1} x2:${sr.x2} y2:${sr.y2} z2:${sr.z2}")
            }
        }

        builder.build()
    }

    static NamespacedKey CFG_EDIT_KEY = new NamespacedKey(Starlight.plugin, "cfg_edit")
    static NamespacedKey CFG_VIEW_KEY = new NamespacedKey(Starlight.plugin, "cfg_view_edit")
    static NamespacedKey CFG_CATEGORY_KEY = new NamespacedKey(Starlight.plugin, "cfg_category")
    static NamespacedKey CFG_ENTRY_KEY = new NamespacedKey(Starlight.plugin, "cfg_entry_key")
    static NamespacedKey CFG_ENTRY_ID = new NamespacedKey(Starlight.plugin, "cfg_entry_edit")
    static NamespacedKey CFG_REGULAR_KEY = new NamespacedKey(Starlight.plugin, "cfg_category")

    static def openConfigGui(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3Server-Config", getConfigs().toList(), { Config config, Integer i ->
            def lore = []
            lore.addAll(config.description)

            def item = FastItemUtils.createItem(config.material, config.displayName, lore, false)

            DataUtils.setTag(item, CFG_EDIT_KEY, PersistentDataType.STRING, config.getId())

            return item
        }, page, false, [
                { Player p, ClickType t, int slot ->
                    def item = menu.get().getItem(slot)
                    if (item == null || item.type == Material.AIR) return

                    def id = DataUtils.getTag(item, CFG_EDIT_KEY, PersistentDataType.STRING)
                    if (id == null) return

                    def config = getConfig(id)
                    if (config == null) return

                    openConfig(p, config)
                },
                { Player p, ClickType t, int slot ->
                    openConfigGui(p, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    openConfigGui(p, page - 1)
                }
        ] as List<MenuEvent>)

        menu.openSync(player)
    }

    static def openConfig(Player player, Config config, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3${config.displayName} §3Config", config.getCategories().toList(), { ConfigCategory category, Integer i ->
            def lore = []
            lore.addAll(category.description)

            def item = FastItemUtils.createItem(category.material, "§3${category.displayName}", lore, false)

            DataUtils.setTag(item, CFG_VIEW_KEY, PersistentDataType.STRING, category.getInternalId())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.type == Material.AIR) return

                    String categoryId = DataUtils.getTag(item, CFG_VIEW_KEY, PersistentDataType.STRING)
                    if (categoryId == null) return

                    ConfigCategory category = config.getCategory(categoryId)
                    if (category == null) return

                    openCategory(p, config, category)
                },
                { Player p, ClickType t, int s -> openConfig(p, config, page + 1) },
                { Player p, ClickType t, int s -> openConfig(p, config, page - 1) },
                { Player p, ClickType t, int s -> openConfigGui(p) },
        ] as List<MenuEvent>)

        menu.openSync(player)
    }

    static def openCategory(Player player, Config config, ConfigCategory category, int page = 1) {
            MenuBuilder menu

            menu = MenuUtils.createPagedMenu("§3${category.getDisplayName()} §3Config", category.getConfigs().toList(), { RegularConfig regularConfig, Integer i ->
                def lore = []
                lore.addAll(regularConfig.description)
                lore.add("")
                lore.add("§7Click to Edit")
                lore.add("§7Right-Click to §c§lRESET")

                def item = FastItemUtils.createItem(regularConfig.material, "§3${regularConfig.displayName}", lore, false)

                DataUtils.setTag(item, CFG_REGULAR_KEY, PersistentDataType.STRING, regularConfig.getInternalId())

                return item
            }, page, true, [
                    { Player p, ClickType t, int s ->
                        def item = menu.get().getItem(s)
                        if (item == null || item.type == Material.AIR) return

                        String id = DataUtils.getTag(item, CFG_REGULAR_KEY, PersistentDataType.STRING)
                        if (id == null) return

                        RegularConfig cfg = category.getConfig(id)
                        if (cfg == null) return

                        if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                            cfg.resetToDefault()
                            config.queueSave()

                            openCategory(p, config, category, page)
                            Players.playSound(p, Sound.UI_BUTTON_CLICK)
                            return
                        } else {
                            openRegularConfig(p, config, category, cfg)
                        }
                    },
                    { Player p, ClickType t, int s -> openCategory(p, config, category, page + 1) },
                    { Player p, ClickType t, int s -> openCategory(p, config, category, page - 1) },
                    { Player p, ClickType t, int s -> openConfig(p, config) },
            ] as List<MenuEvent>)

            menu.openSync(player)
        }

    static def openRegularConfig(Player player, Config config, ConfigCategory category, RegularConfig regularConfig, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3${config.getId()} §3Config", regularConfig.getEntries().toList(), { ConfigEntry<?> entry, Integer i ->
            def lore = []

            def material = Material.BOOK

            if (entry.type == ConfigType.SR) {
                def srEntry = entry as SREntry
                if (srEntry.value.x1) { // check if it has data?
                    lore.add("§3World: §a${srEntry.value.world}")
                    lore.add("")
                    lore.add("§3X1: §a${srEntry.value.x1}")
                    lore.add("§3Y1: §a${srEntry.value.y1}")
                    lore.add("§3Z1: §a${srEntry.value.z1}")
                    lore.add("")
                    lore.add("§3X2: §a${srEntry.value.x2}")
                    lore.add("§3Y2: §a${srEntry.value.y2}")
                    lore.add("§3Z2: §a${srEntry.value.z2}")
                } else {
                    lore.add("§cNo Region Selected")
                }

                lore.add("")
                lore.add("§7Click to Select a Region")
                lore.add("§7After making a selection.")

                material = Material.WOODEN_AXE
            } else if (entry.type == ConfigType.CL) {
                def clEntry = entry as CLEntry
                if (clEntry.value.x) { // check if it has data?
                    lore.add("§3World: §a${clEntry.value.worldName}")
                    lore.add("")
                    lore.add("§3X: §a${clEntry.value.x}")
                    lore.add("§3Z: §a${clEntry.value.z}")
                } else {
                    lore.add("§cNo Chunk Selected")
                }

                lore.add("")
                lore.add("§7Click to select the Chunk")
                lore.add("§7you are currently standing in.")

                material = Material.GRASS_BLOCK
            } else if (entry.type == ConfigType.POSITION) {
                def positionEntry = entry as PositionEntry
                if (positionEntry.value.x) { // check if it has data?
                    lore.add("§3World: §a${positionEntry.value.world}")
                    lore.add("")
                    lore.add("§3X: §a${positionEntry.value.x}")
                    lore.add("§3Y: §a${positionEntry.value.y}")
                    lore.add("§3Z: §a${positionEntry.value.z}")

                    if (positionEntry.value.yaw) {
                        lore.add("")
                        lore.add("§3Yaw: §a${positionEntry.value.yaw}")
                    }
                    if (positionEntry.value.pitch) {
                        lore.add("§3Pitch: §a${positionEntry.value.pitch}")
                    }
                } else {
                    lore.add("§cNo Position Selected")
                }

                lore.add("")
                lore.add("§7Click to select the Position")
                lore.add("§7you are currently standing in.")

                material = Material.OAK_SIGN
            } else if (entry.type == ConfigType.MATERIAL) {
                lore.add("§7Material: ${entry.value}")
                lore.add("")
                lore.add("§7Click to Select")

                material = entry.value as Material
            } else if (entry.type == ConfigType.LIST_STRING) {
                def listStringEntry = entry as StringListEntry
                lore.add("")
                lore.addAll(listStringEntry.value)
            } else if (entry.type == ConfigType.LIST_MATERIAL) {
                def listMaterialEntry = entry as MaterialListEntry
                lore.add("")
                listMaterialEntry.value.each {
                    lore.add("§7- ${it.toString()}")
                }
            } else if (entry.type == ConfigType.LIST_POSITION) {
                def listPositionEntry = entry as PositionListEntry
                lore.add("")
                listPositionEntry.value.each {
                    lore.add("§7- ${it.toString()}")
                }
            } else {
                lore.add("§7${entry.value}")
                lore.add("")
                lore.add("§7Click to Edit")
                lore.add("§3Type: §a${entry.type}")
            }

            if (entry.value != entry.defaultValue) {
                lore.add("")
                lore.add("§7Default:")
                lore.addAll(entry.defaultValue.toString())
                lore.add("")
                lore.add("§7Right-Click to §c§lRESET")
            }

            def item = FastItemUtils.createItem(material, "§3${entry.id}", lore, false)

            DataUtils.setTag(item, CFG_ENTRY_KEY, PersistentDataType.STRING, entry.id)
            DataUtils.setTag(item, CFG_ENTRY_ID, PersistentDataType.STRING, entry.type.toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.type == Material.AIR) return

                    String id = DataUtils.getTag(item, CFG_ENTRY_KEY, PersistentDataType.STRING)
                    if (id == null) return

                    String entryType = DataUtils.getTag(item, CFG_ENTRY_ID, PersistentDataType.STRING)
                    if (entryType == null) return

                    def type = ConfigType.valueOf(entryType)
                    if (type == null) return

                    ConfigEntry entry = regularConfig.getEntry(id, type)
                    if (entry == null) return

                    if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                        entry.resetToDefault()
                        config.queueSave()

                        openRegularConfig(p, config, category, regularConfig, page)
                        Players.playSound(p, Sound.UI_BUTTON_CLICK)
                        return
                    }

                    switch (entry.type) {
                        case ConfigType.BOOLEAN:
                            def booleanEntry = entry as BooleanEntry
                            SelectionUtils.selectBoolean(p, { boolean value ->
                                booleanEntry.value = value
                                config.queueSave()

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.INT:
                            def intEntry = entry as IntEntry
                            SelectionUtils.selectInteger(p, { int value ->
                                intEntry.value = value
                                config.queueSave()

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.DOUBLE:
                            def doubleEntry = entry as DoubleEntry
                            SelectionUtils.selectDouble(p, { double value ->
                                doubleEntry.value = value
                                config.queueSave()

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.STRING:
                            def stringEntry = entry as StringEntry
                            SelectionUtils.selectString(p, { String value ->
                                stringEntry.value = value
                                config.queueSave()

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.SR:
                            def srEntry = entry as SREntry

                            def selection = SelectionUtils.getSelection(p)
                            if (selection == null) return

                            SelectionUtils.confirmRegion(p, selection, { boolean value ->
                                if (value) {
                                    srEntry.value = selection
                                    config.queueSave()
                                }

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.CL:
                            def clEntry = entry as CLEntry

                            def chunk = p.getLocation().getChunk()
                            def cl = new CL(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())

                            SelectionUtils.confirmChunk(p, cl, { boolean value ->
                                if (value) {
                                    clEntry.value = cl
                                    config.queueSave()
                                }

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.POSITION:
                            def positionEntry = entry as PositionEntry

                            def position = new Position(p.getLocation().getWorld().getName(), p.getLocation().getX() as int, p.getLocation().getY() as int, p.getLocation().getZ() as int)

                            SelectionUtils.confirmPosition(p, position, { boolean value ->
                                if (value) {
                                    positionEntry.value = position
                                    config.queueSave()
                                }

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.LIST_STRING:
                            def listStringEntry = entry as StringListEntry

                            SelectionUtils.selectStringList(p, listStringEntry.value, { List<String> value ->
                                listStringEntry.value = value
                                config.queueSave()

                                openRegularConfig(p, config, category, regularConfig, page)
                            })
                            break
                        case ConfigType.MATERIAL:
                            def materialEntry = entry as MaterialEntry

                            SelectionUtils.selectMaterial(p) { Material value ->
                                materialEntry.value = value
                                config.queueSave()

                                openRegularConfig(p, config, category, regularConfig, page)
                            }
                            break
                        case ConfigType.LIST_SR:
                            def listSREntry = entry as SRListEntry

                            SelectionUtils.selectRegionList(p, listSREntry.value, { List<SR> value ->
                                if (!value.isEmpty()) {
                                    listSREntry.value = value
                                    config.queueSave()
                                }

                                openRegularConfig(p, config, category, regularConfig, page)
                            })

                            break
//                        case ConfigType.LIST_POSITION:
//                            def listPositionEntry = entry as PositionListEntry
//
//                            SelectionUtils.selectPositionList(p, listPositionEntry.value, { List<Position> value ->
//                                listPositionEntry.value = value
//                                config.queueSave()
//
//                                openRegularConfig(p, config, category, regularConfig, page)
//                            })
//                            break
                    }

                },
                { Player p, ClickType t, int s -> openRegularConfig(p, config, category, regularConfig, page + 1) },
                { Player p, ClickType t, int s -> openRegularConfig(p, config, category, regularConfig, page - 1) },
                { Player p, ClickType t, int s -> openCategory(p, config, category) },
        ] as List<MenuEvent>)

        menu.openSync(player)
    }


}



