package scripts.factions.features.pets

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import scripts.shared.core.cfg.Config
import scripts.shared.core.cfg.ConfigCategory
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.entries.StringEntry
import scripts.shared.core.cfg.entries.list.StringListEntry
import scripts.factions.features.pets.struct.ItemPet
import scripts.factions.features.pets.struct.ItemPetData
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ColorUtil

import java.nio.file.Files

@CompileStatic(TypeCheckingMode.SKIP)
class ItemPets {

    static Map<String, ItemPet> registeredPets = Maps.newConcurrentMap()

    static Config petsConfig
    static ConfigCategory petsCategory
    static RegularConfig petsValues

    ItemPets() {
        GroovyScript.addUnloadHook {
            Starlight.unload(petScriptPaths as String[])
            Starlight.unload("~/ItemPetListener.groovy")
        }

        petsConfig = DBConfigUtil.createConfig("pets", "§dPets", [], Material.PLAYER_HEAD)
        petsCategory = petsConfig.getOrCreateCategory("values", "§dPets Values")

        petsValues  = petsCategory.getOrCreateConfig("values")
        petsValues.addDefault([
                new StringListEntry("petLoreFormat", [
                        "%description%",
                        "",
                        "§<#FB5C5C>LEVEL: %level%",
                        "§<#FB5C5C>ABILITY: %ability%",
                        "",
                ]),
                new StringEntry("petNameFormat", "§<#FB5C5C>§l☪ §f§l%petName% §7[§<#FB5C5C>%level%§7]")
        ])

        petsConfig.queueSave()

        registerPets()

        Starlight.watch("~/ItemPetListener.groovy")

        commands()
    }

    static List<String> getLoreFormat() {
        return petsValues.getStringListEntry("petLoreFormat").value
    }

    static String getNameFormat() {
        return petsValues.getStringEntry("petNameFormat").value
    }

    static def commands() {
        SubCommandBuilder cmd = new SubCommandBuilder("pets").defaultAction {
            openPetMenu(it)
        }

        cmd.create("debugitem").requirePermission("starlight.admin").register {ctx ->
            def item = ctx.sender().getInventory().getItemInMainHand()
            if (item == null || item.type == Material.AIR) return

            def petData = ItemPetData.read(item)
            if (petData == null) return

            def pet = registeredPets.get(petData.currentAbility)
            if (pet == null) return

            ctx.reply("§c§lDEBUG- §7UUID: ${petData.petId}")
            ctx.reply("§c§lDEBUG- §7Pet: ${pet.internalName}")
            ctx.reply("§c§lDEBUG- §7Level: ${petData.level}")
            ctx.reply("§c§lDEBUG- §7Ability: ${petData.currentAbility}")
            ctx.reply("§c§lDEBUG- §7Cooldown: ${petData.cdExpiration}")
        }

        cmd.build()
    }

    Set<String> petScriptPaths = Sets.newHashSet()

    void registerPets() {
        File enchantScriptsFolder = new File("${GroovyScript.getCurrentScript().getScript().getParent()}${File.separator}pets")
        Files.walk(enchantScriptsFolder.toPath(), 3).forEach({
            String path = it.toString()
            if (path.endsWith(".groovy")) {
                registerPet(path)
            }
        })
    }

    void registerPet(String enchantPath) {
        if (!petScriptPaths.add(enchantPath)) return

        Starlight.watch(enchantPath)
    }

    static def openPetMenu(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("pets", registeredPets.values().toList(), { ItemPet pet, Integer slot ->
            return createPetItem(pet, UUID.randomUUID(), 1.0D)
        }, page, false, [
                { Player p, ClickType t, Integer s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.type == Material.AIR) return

                    if (t == ClickType.LEFT) {
                        FastInventoryUtils.addOrBox(p.getUniqueId(), p, null, item, null)
                    }
                },
                { Player p, t, s -> openPetMenu(p, page + 1)},
                { Player p, t, s -> openPetMenu(p, page - 1)},
        ])

        menu.openSync(player)
    }

    static ItemStack createPetItem(ItemPet pet, UUID petId, double level) {
        def name = ColorUtil.color(getNameFormat().replaceAll("%petName%", pet.getDisplayName()).replaceAll("%level%", (Math.floor(level) as int).toString()))

        def lore = []

        getLoreFormat().each {
            if (it.contains("%description%")) {
                lore.addAll(pet.getDescription().collect {
                    ColorUtil.color(it.replaceAll("%level%", (Math.floor(level) as int).toString()))
                })
            } else if (it.contains("%ability%")) {
                lore.add(ColorUtil.color(it.replaceAll("%ability%", pet.internalName)))
            } else {
                lore.add(ColorUtil.color(it.replaceAll("%level%", StringUtils.getProgressBar((level - Math.floor(level)) * 100 as long, 100L, 20, '|' as char))))
            }
        }

        def itemStack = FastItemUtils.createBase64Skull(pet.getSkullTexture(), name, lore)

        def petData = new ItemPetData(petId, level)
        petData.currentAbility = pet.internalName
        petData.write(itemStack)

        return itemStack
    }

    static def updatePetItem(ItemStack stack) {
        def petData = ItemPetData.read(stack)
        if (petData == null) return

        def pet = registeredPets.get(petData.getCurrentAbility())
        if (pet == null) return

        def lore = []
        getLoreFormat().each {
            if (it.contains("%description%")) {
                lore.addAll(pet.getDescription().collect {
                    ColorUtil.color(it.replaceAll("%level%", petData.level.toString()))
                })
            } else if (it.contains("%ability%")) {
                lore.add(ColorUtil.color(it.replaceAll("%ability%", pet.internalName)))
            } else {
                def level = petData.level
                lore.add(ColorUtil.color(it.replaceAll("%level%", StringUtils.getProgressBar((level - Math.floor(level)) * 100 as long, 100L, 20, '|' as char))))
            }
        }

        def name = ColorUtil.color(getNameFormat().replaceAll("%petName%", pet.getDisplayName()).replaceAll("%level%", (Math.floor(petData.level) as int).toString()))

        FastItemUtils.setDisplayName(stack, name)
        FastItemUtils.setLore(stack, lore)
    }

}
