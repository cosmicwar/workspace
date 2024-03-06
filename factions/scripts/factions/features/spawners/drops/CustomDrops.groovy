package scripts.factions.features.spawners.drops

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import scripts.shared.data.string.StringDataManager
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils

import java.util.concurrent.ThreadLocalRandom

@CompileStatic(TypeCheckingMode.SKIP)
class CustomDrops {

    Map<String, CustomDrop> cachedDrops = Maps.newConcurrentMap()

    CustomDrops() {
        GroovyScript.addUnloadHook {
            StringDataManager.getByClass(CustomDrop).saveAll(false)
        }

        StringDataManager.register("customDrops", CustomDrop.class)

        StringDataManager.getAllData(CustomDrop.class).each {
            cachedDrops.put(it.id, it)
        }

        commands()
        events()
    }

    def commands() {
        Commands.create().assertOp().assertPlayer().handler {ctx ->
            cachedDrops.each { mobName, drop ->
                ctx.reply("${mobName}: ${drop.customDrops}")
            }
        }.register("dev/debugdrops")

        Commands.create().assertOp().assertPlayer().handler {ctx ->
            openDropsMenu(ctx.sender())
        }.register("customdrops")

        Commands.create().assertOp().assertPlayer().handler {ctx ->
            StringDataManager.wipe(CustomDrop.class)
            cachedDrops.clear()

            getDrop("blaze")
            getDrop("skeleton")
        }.register("customdropswipe")
    }

    private NamespacedKey key = new NamespacedKey(Starlight.plugin, "custom_drops_edit")

    def openDropsMenu(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("Custom Drops", cachedDrops.values().toList(), { CustomDrop drop, Integer i ->
            def lore = [
                    "§7Drops: §b${drop.customDrops.size()}",
                    "",
                    "§7Left-Click to §bedit §7this drop.",
                    "§7Right-Click to §cremove §7this drop."
            ]

            def item = FastItemUtils.createItem(Material.PAPER, "§b${drop.id}", lore)
            DataUtils.setTag(item, key, PersistentDataType.STRING, drop.id)

            return item
        }, page, false, [
                { Player p, ClickType t, Integer s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.getType() == Material.AIR) return

                    def dropId = DataUtils.getTag(item, key, PersistentDataType.STRING)
                    if (dropId == null) return

                    def drop = getDrop(dropId)
                    if (drop == null) return

                    openDropMenu(p, drop)
                },
                { Player p, ClickType t, Integer s ->
                    openDropsMenu(p, page + 1)
                },
                { Player p, ClickType t, Integer s ->
                    openDropsMenu(p, page - 1)
                }
        ])

        menu.openSync(player)
    }

    def openDropMenu(Player player, CustomDrop drop, int page = 1) {
        MenuBuilder menu

        def items = drop.customDrops.collect {
            try {
                Material material = Material.valueOf(it)
                return new ItemStack(material)
            } catch (Exception e) {
                return FastItemUtils.convertStringToItemStack(it)
            }
        }.findAll { it != null }

        menu = MenuUtils.createPagedMenu("Custom Drops", items, { ItemStack dropItemStack, Integer i ->
            return dropItemStack
        }, page, true, [
                { Player p, ClickType t, Integer s ->
                    def item = menu.get().getItem(s)

                    if (item == null || item.getType() == Material.AIR) return

                    if (t == ClickType.RIGHT) {
                        if (drop.customDrops.remove(FastItemUtils.convertItemStackToString(item))) {
                            drop.queueSave()
                        }

                        if (drop.customDrops.remove(item.getType().toString())) {
                            drop.queueSave()
                        }

                        openDropMenu(p, drop, page)
                    }
                },
                { Player p, ClickType t, Integer s ->
                    openDropMenu(p, drop, page + 1)
                },
                { Player p, ClickType t, Integer s ->
                    openDropMenu(p, drop, page - 1)
                },
                { Player p, ClickType t, Integer s ->
                    openDropsMenu(p)
                }
        ])

        menu.setExternal {p, t, s ->
            if (t == ClickType.LEFT) {
                if (s <= 36 && s >= 0) {
                    def item = p.getInventory().getItem(s).clone()

                    if (item == null || item.getType() == Material.AIR) return

                    item.setAmount(1)

                    drop.customDrops.add(FastItemUtils.convertItemStackToString(item))
                    drop.queueSave()

                    openDropMenu(p, drop, page)
                }
            }
        }

        menu.openSync(player)
    }

    def events() {
        Events.subscribe(EntityDeathEvent.class, EventPriority.LOWEST).handler { event ->
//            Bukkit.broadcastMessage("drops: ${event.getDrops().toString()}")

            def entity = event.getEntity()

            if (entity instanceof Player) return

            def drop = getCachedDrop(entity.type.toString())
            if (drop == null || drop.customDrops.isEmpty()) return

            def items = drop.customDrops.collect {
                try {
                    Material material = Material.valueOf(it)
                    return new ItemStack(material)
                } catch (Exception e) {
                    return FastItemUtils.convertStringToItemStack(it)
                }
            }.findAll { it != null }

            def killer = entity.getKiller()
            def multiplier = 0

            if (killer != null) {
                def item = killer.getInventory().getItemInMainHand()
                if (item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) > 0) {
                    multiplier = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)
                }
            }

            def random = ThreadLocalRandom.current()

            def dropCount = event.getDrops().size()
            event.getDrops().clear()

            for (int i = 0; i < dropCount; i++) {
                def item = RandomUtils.getRandom(items)
                item.setAmount(random.nextInt(0 + multiplier, 1 + multiplier))

                if (item.amount <= 0) continue

                event.drops.add(item)
            }
        }
    }

    CustomDrop getCachedDrop(String mobName) {
        return cachedDrops.get(mobName.toLowerCase())
    }

    CustomDrop getDrop(String mobName) {
        mobName = mobName.toLowerCase()

        def cachedDrop = cachedDrops.get(mobName)
        if (cachedDrop != null) return cachedDrop

        def drop = StringDataManager.getData(mobName, CustomDrop.class, true)

        return cachedDrops.put(mobName, drop)
    }

}
