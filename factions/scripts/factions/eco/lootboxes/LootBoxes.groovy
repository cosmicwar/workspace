package scripts.factions.eco.lootboxes

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import scripts.exec.Globals
import scripts.factions.eco.rewardbox.animation.RewardBoxOpeningAnimation
import scripts.factions.eco.lootboxes.data.LootBox
import scripts.factions.eco.lootboxes.data.LootBoxReward
import scripts.factions.eco.lootboxes.data.LootBoxRewardGroup
import scripts.shared.legacy.AntiDupeUtils
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.utils.DataUtils
import scripts.shared.utils.Temple

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@CompileStatic
class LootBoxes {

    NamespacedKey lootBoxKey = new NamespacedKey(Starlight.plugin, "lootbox")
    Map<String, LootBox> availableLootBoxes = new ConcurrentHashMap<>()
    Map<UUID, RewardBoxOpeningAnimation> openingBoxes = new ConcurrentHashMap<>()

    LootBoxes() {
        if (!Globals.LOOTBOXES) return

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            if (!GroovyScript.getCurrentScript().getWatchedScripts().contains(it)) return

            Schedulers.sync().runLater({
                reloadConfig()
            }, 1L)
        })

        GroovyScript.addUnloadHook {
            openingBoxes.values().each { it.finish() }
        }

        reloadConfig()
        registerCommands()
        registerEvents()

        Schedulers.async().runRepeating({
            openingBoxes.entrySet().removeIf(entry -> {
                entry.value.tick()
                return entry.value.finished.get()
            })
        }, 50L, TimeUnit.MILLISECONDS, 50L, TimeUnit.MILLISECONDS)

        Exports.ptr("lootboxes:getLootBox", { String id -> return availableLootBoxes.get(id) })
        Exports.ptr("lootboxes:makeLootBoxItemStack", { String id -> return makeLootBox(id) })
    }

    void registerCommands() {
        Commands.create().assertOp().assertUsage("<player> <lootbox> <amount>").handler { c ->
            DatabaseUtils.getId(c.rawArg(0), { UUID uuid, String username, Player player ->
                if (uuid == null) {
                    c.reply("§] §> §a${username} §fhas never joined the server before!")
                    return
                }

                String id = c.rawArg(1).toLowerCase()
                LootBox lootBox = availableLootBoxes.get(id)
                if (lootBox == null) {
                    c.reply("§] §> §c${c.rawArg(1)} is not a valid lootbox id!")
                    return
                }

                int amount = c.arg(2).parseOrFail(Integer.class)
                amount.times {
                    ItemStack lootBoxItem = makeLootBox(id)
                    Starlight.log.info("[LootBoxes] Gave ${username} a ${id} lootbox with id ${FastItemUtils.getId(lootBoxItem)}")
                    FastInventoryUtils.addOrBox(player.getUniqueId(), player, c.sender(), lootBoxItem, "§] §> §fYou received a ${lootBox.getDisplayName()}§f loot box!")
                }

                c.reply("Given loot box!")
            })
        }.register("givelootbox")

        Commands.create().assertOp().assertPlayer().assertUsage("<lootbox>").handler { c ->
            String id = c.rawArg(0).toLowerCase()
            LootBox lootBox = availableLootBoxes.get(id)
            if (lootBox == null) {
                c.reply("§] §> §c${c.rawArg(0)} is not a valid lootbox id!")
                return
            }

            openLootBox(c.sender(), lootBox, null)
        }.register("dev/lootboxes/open")
    }

    void openLootBox(Player player, LootBox lootBox, ItemStack itemStack) {
        openingBoxes.remove(player.getUniqueId())?.finish()

        Class clazz = GroovyScript.getCurrentScript().getLoadedScripts().find { it.path == "${lootBox.getAnimation().replace(".", "/")}.groovy" }.getScriptClazz()
        openingBoxes.put(player.getUniqueId(), clazz.getDeclaredConstructor(Player.class, World.class, LootBox.class, ItemStack.class).newInstance(player, player.getWorld(), lootBox, itemStack) as RewardBoxOpeningAnimation)
    }

    void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class).handler({
            if (it.getHand() != EquipmentSlot.HAND || (it.getAction() != Action.RIGHT_CLICK_AIR && it.getAction() != Action.RIGHT_CLICK_BLOCK)) return

            ItemStack itemStack = it.getItem()
            String lootBoxId = getLootBoxId(itemStack)
            if (lootBoxId == null) return

            it.setCancelled(true)

            Player player = it.getPlayer()
            UUID id = FastItemUtils.getId(itemStack)
            if (id == null || AntiDupeUtils.isDuped(id)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                Players.msg(player, "§! §> §cThis Loot Box is invalid, please contact an admin!")
                return
            }

            LootBox lootBox = availableLootBoxes.get(lootBoxId)
            if (lootBox == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                Players.msg(player, "§! §> §cThis Loot Box is invalid, please contact an admin!")
                return
            }

            FastInventoryUtils.use(player)
            AntiDupeUtils.useId(id, {
                Starlight.log.info("[LootBoxes] ${player.getName()} started opening a ${lootBoxId} loot box with id ${id}")
                openLootBox(player, lootBox, itemStack)
            })
        })

        Events.subscribe(PlayerQuitEvent.class).handler({
            openingBoxes.remove(it.getPlayer().getUniqueId())?.finish()
        })
    }

    void reloadConfig() {
        Starlight.watch("scripts/exec/$Temple.templeId/lootboxes.groovy")

        availableLootBoxes.findResults { it.getValue().getAnimation() }.each { String path ->
            path = "${path.replace(".", "/")}.groovy"
            GroovyScript.getCurrentScript().getLoadedScripts().removeIf({ GroovyScript groovyScript -> groovyScript.getPath() == path })
            Starlight.unload(path)
        }

        availableLootBoxes.clear()
        (Exports.get("lootboxes", [
                example: [
                        displayName: "Example Loot Box",
                        displayLore: [
                                "This lootbox is awesome",
                                "and so cool",
                                "",
                                "{rewards}"
                        ],
                        itemStack: new ItemStack(Material.DIRT),
                        minRewards: 3,
                        maxRewards: 3,
                        rewardGroups: [
                                [
                                        displayName: "Rewards",
                                        loreFormat : "| {reward}",
                                        rewards    : [
                                                [
                                                        weight     : 10D,
                                                        displayName: "1 diamond",
                                                        itemStack  : new ItemStack(Material.DIAMOND),
                                                        commands   : [
                                                                "give {player} diamond 1"
                                                        ]
                                                ],
                                                [
                                                        weight     : 10D,
                                                        displayName: "1 emerald",
                                                        itemStack  : new ItemStack(Material.EMERALD),
                                                        commands   : [
                                                                "give {player} emerald 1"
                                                        ]
                                                ]
                                        ]
                                ]
                        ]
                ]
        ]) as Map<String, ?>).each { lootboxConfig ->
            String lootBoxId = lootboxConfig.key
            LootBox lootBox = new LootBox()
            (lootboxConfig.value as Map<String, ?>).each { configEntry ->
                if (configEntry.key == "rewardGroups") {
                    lootBox.setProperty(configEntry.key, (configEntry.value as List<Map<String, ?>>).findResults { rewardGroup ->
                        LootBoxRewardGroup lootBoxRewardGroup = new LootBoxRewardGroup()
                        rewardGroup.each { property ->
                            if (property.getKey() == "rewards") {
                                lootBoxRewardGroup.setProperty(property.getKey(), (property.getValue() as List<Map<String, ?>>).findResults { rewardMap ->
                                    LootBoxReward lootBoxReward = new LootBoxReward()
                                    rewardMap.each { lootBoxReward.setProperty(it.key, it.value) }
                                    return lootBoxReward
                                } as List<LootBoxReward>)
                            } else {
                                lootBoxRewardGroup.setProperty(property.getKey(), property.getValue())
                            }
                        }

                        return lootBoxRewardGroup
                    })
                } else {
                    lootBox.setProperty(configEntry.key, configEntry.value)
                }
            }

            availableLootBoxes.put(lootBoxId, lootBox)
        }

        availableLootBoxes.values().each {
            Starlight.watch("${it.getAnimation().replace(".", "/")}.groovy")
        }
    }

    String getLootBoxId(ItemStack itemStack) {
        return itemStack == null ? null : DataUtils.getTagString(itemStack, lootBoxKey)
    }

    ItemStack makeLootBox(String lootBoxId) {
        LootBox lootBox = availableLootBoxes.get(lootBoxId)
        ItemStack itemStack = (lootBox.getItemStacks().size() == 1 ? lootBox.getItemStacks().get(0) : RandomUtils.getRandom(lootBox.getItemStacks())).clone()
        FastItemUtils.setDisplayName(itemStack, lootBox.getDisplayName())

        List<String> lore = [
                lootBox.getDisplayLore().findResults {
                    if (it == "{rewards}") {
                        List<String> rewardsLore = lootBox.getRewardGroups().findResults { rewardGroup ->
                            [
                                    rewardGroup.getDisplayName(),
                                    rewardGroup.getRewards().findAll { it.displayOnLore }.findResults { rewardGroup.getLoreFormat().replace("{reward}", it.getDisplayName()) },
                                    ""
                            ]
                        }.flatten() as List<String>

                        rewardsLore.remove(rewardsLore.size() - 1)
                        return rewardsLore
                    } else {
                        return it
                    }
                }
        ].flatten().findResults { it?.toString() } as List<String>

        if (lore.get(lore.size() - 1)?.isEmpty()) {
            lore.remove(lore.size() - 1)
        }

        FastItemUtils.setLore(itemStack, lore)

        FastItemUtils.ensureUnique(itemStack)
        FastItemUtils.setShiny(itemStack)
        DataUtils.setTagString(itemStack, lootBoxKey, lootBoxId)

        return itemStack
    }

}
