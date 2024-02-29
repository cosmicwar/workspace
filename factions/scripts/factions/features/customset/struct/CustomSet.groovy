package scripts.factions.features.customset.struct

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.google.common.collect.Maps
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.list.StringListEntry
import scripts.factions.features.customset.CustomSets
import scripts.factions.features.customset.data.CustomSetItemData
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Persistent

import java.util.function.Function

abstract class CustomSet {
    String internalName
    CustomSetWeapon weapon = null

    RegularConfig config

    Map<UUID, Long> abilitiyCooldown = Persistent.of("sets-ability-cd", Maps.<UUID, Long> newConcurrentMap()).get()

    boolean canHaveCrystal = true

    Function<String, String> colorRemapper = { String string ->
        if (getColors().isEmpty()) return ColorUtil.color("§f$string")

        def detectedFormat = "" // overrides format if detected

        if (string.startsWith("§<bold>")) {
            string = string.replace("§<bold>", "")
            detectedFormat = "§l"
        }

        if (string.startsWith("§<italic>")) {
            string = string.replace("§<italic>", "")
            detectedFormat = "§o"
        }

        if (string.startsWith("§<underline>")) {
            string = string.replace("§<underline>", "")
            detectedFormat = "§n"
        }

        if (string.startsWith("§<strike>")) {
            string = string.replace("§<strike>", "")
            detectedFormat = "§m"
        }

        if (string.startsWith("§<obfuscated>")) {
            string = string.replace("§<obfuscated>", "")
            detectedFormat = "§k"
        }

        boolean rainbow = false
        if (string.contains("§<rainbow>")) {
            string = string.replace("§<rainbow>", "")
            rainbow = true
        }

        if (rainbow) return ColorUtil.rainbow(string, getColors() as String[], detectedFormat)

        def hexSize = getColors().size()

        if (hexSize == 1) {
            string = string.replaceAll("§<primaryColor>", "§<${getColors().get(0)}>${detectedFormat}")
            return ColorUtil.color("§f$string")
        }

        if (hexSize == 2) {
            string = string.replaceAll("§<primaryColor>", "§<${getColors().get(0)}>${detectedFormat}")
            string = string.replaceAll("§<secondaryColor>", "§<${getColors().get(1)}>${detectedFormat}")
            return ColorUtil.color("§f$string")
        }

        return ColorUtil.color("§f$string")
    }

    CustomSet() {}

    CustomSet(String internalName,
              String displayName,
              List<String> description = [],
              String equippedMessage = "",
              String unequippedMessage = "",
              String helmetName = "Helmet",
              String chestPlateName = "Chestplate",
              String leggingsName = "Leggings",
              String bootsName = "Boots",
              List<String> colors = [],
              List<String> crystalLore = []
    ) {
        this.internalName = internalName

        getConfig().addDefault([
                new StringEntry("displayName", displayName),
                new StringEntry("equippedMessage", equippedMessage),
                new StringEntry("unequippedMessage", unequippedMessage),
                new StringEntry("helmetName", helmetName),
                new StringEntry("chestPlateName", chestPlateName),
                new StringEntry("leggingsName", leggingsName),
                new StringEntry("bootsName", bootsName),
                new StringListEntry("description", description),
                new StringListEntry("colors", colors)
        ])

        getCrystalConfig().addDefault([
                new StringListEntry("crystalLore", crystalLore)
        ])

        CustomSets.config.queueSave()
    }

    String getDisplayName() {
        return getConfig().getStringEntry("displayName").value
    }

    String getBoldDisplayName() {
        return colorRemapper.apply("§<bold>§<rainbow>${getDisplayName()}")
    }

    String getColoredDisplayName() {
        return colorRemapper.apply("§<rainbow>${getDisplayName()}")
    }

    List<String> getDescription() {
        return getConfig().getStringListEntry("description").value
    }

    List<String> getColors() {
        return getConfig().getStringListEntry("colors").value
    }

    String getEquippedMessage() {
        return getConfig().getStringEntry("equippedMessage").value
    }

    String getUnequippedMessage() {
        return getConfig().getStringEntry("unequippedMessage").value
    }

    List<String> getCrystalLore() {
        return getCrystalConfig().getStringListEntry("crystalLore").value
    }

    String getSetPrefix() {
        return colorRemapper.apply("§<bold>§<primaryColor>${getDisplayName()[0]}")
    }

    RegularConfig getConfig() {
        return config == null ? config = CustomSets.sets.getOrCreateConfig(this.internalName) : config
    }

    RegularConfig getCrystalConfig() {
        return config == null ? config = CustomSets.crystals.getOrCreateConfig(this.internalName) : config
    }

    void sendEquippedMessage(Player player) {
        if (equippedMessage != "") {
            Players.msg(player, "")
            Players.msg(player, colorRemapper.apply(equippedMessage))
            Players.msg(player, "")
        }
    }

    void sendUnequippedMessage(Player player) {
        if (unequippedMessage != "") {
            Players.msg(player, "")
            Players.msg(player, colorRemapper.apply(unequippedMessage))
            Players.msg(player, "")
        }
    }

    void addPotionEffect(Player player, PotionEffectType potionEffectType, int amplifier) {
        (Exports.ptr("potionEffects:addEquippedEffect") as Closure)?.call(player, potionEffectType, amplifier as Integer)
        Players.msg(player, ColorUtil.color("§a§l[+] ${getBoldDisplayName()} §8- §7${potionEffectType.getName()} ${amplifier + 1}"))
    }

    void removePotionEffect(Player player, PotionEffectType potionEffectType, int amplifier) {
        (Exports.ptr("potionEffects:removeEquippedEffect") as Closure)?.call(player, potionEffectType, amplifier as Integer)
        Players.msg(player, ColorUtil.color("§c§l[-] ${getBoldDisplayName()} §8- §7${potionEffectType.getName()} ${amplifier + 1}"))
    }

    void addPotionWithDuration(Player player, PotionEffectType potionEffectType, int amplifier, int duration) {
        (Exports.ptr("potionEffects:addPotionWithDuration") as Closure)?.call(player, potionEffectType, amplifier as Integer, duration as Integer)
    }

    ItemStack getHelmet() {
        def helmetName = getConfig().getStringEntry("helmetName").value
        def description = getDescription().collect { line -> colorRemapper.apply(line) }
        def helmet = FastItemUtils.createItem(Material.NETHERITE_HELMET, colorRemapper.apply("§<bold>§<rainbow>${displayName} ${helmetName}"), description, false)

        CustomSetItemData data = new CustomSetItemData(this.internalName, false)
        data.write(helmet)

        return helmet
    }

    ItemStack getChestPlate() {
        def chestPlateName = getConfig().getStringEntry("chestPlateName").value
        def description = getDescription().collect { line -> colorRemapper.apply(line) }
        def chestPlate = FastItemUtils.createItem(Material.NETHERITE_CHESTPLATE, colorRemapper.apply("§<bold>§<rainbow>${displayName} ${chestPlateName}"), description, false)

        CustomSetItemData data = new CustomSetItemData(this.internalName, false)
        data.write(chestPlate)

        return chestPlate
    }

    ItemStack getLeggings() {
        def leggingsName = getConfig().getStringEntry("leggingsName").value
        def description = getDescription().collect { line -> colorRemapper.apply(line) }
        def leggings = FastItemUtils.createItem(Material.NETHERITE_LEGGINGS, colorRemapper.apply("§<bold>§<rainbow>${displayName} ${leggingsName}"), description, false)

        CustomSetItemData data = new CustomSetItemData(this.internalName, false)
        data.write(leggings)

        return leggings
    }

    ItemStack getBoots() {
        def bootsName = getConfig().getStringEntry("bootsName").value
        def description = getDescription().collect { line -> colorRemapper.apply(line) }
        def boots = FastItemUtils.createItem(Material.NETHERITE_BOOTS, colorRemapper.apply("§<bold>§<rainbow>${displayName} ${bootsName}"), description, false)

        CustomSetItemData data = new CustomSetItemData(this.internalName, false)
        data.write(boots)

        return boots
    }

    boolean hasAbilityCd(Player player) {
        if (!abilitiyCooldown.containsKey(player.uniqueId)) return false

        if (abilitiyCooldown.get(player.uniqueId) < System.currentTimeMillis()) {
            abilitiyCooldown.remove(player.uniqueId)
            return false
        }

        return true
    }

    void setAbilityCd(Player player, long duration) {
        abilitiyCooldown.put(player.uniqueId, System.currentTimeMillis() + duration)
    }

    void removeAbilityCd(Player player) {
        abilitiyCooldown.remove(player.uniqueId)
    }

    Long getAbilityCd(Player player) {
        return abilitiyCooldown.get(player.uniqueId)
    }

    void onEquip(Player player) {}

    void onUnequip(Player player) {}

    void onAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {}

    void onKill(Player player, LivingEntity target, EntityDeathEvent event) {}

    void onBossKill(Player player, LivingEntity target) {}

    void onDamaged(Player player, Entity attacker, EntityDamageByEntityEvent event) {}

    void onKnockback(Player player, LivingEntity target, EntityKnockbackByEntityEvent event) {}

    void onKnockedback(Player player, Entity attacker, EntityKnockbackByEntityEvent event) {}

    void onEnvironmentDamaged(Player player, EntityDamageEvent.DamageCause damageCause, EntityDamageEvent event) {}

    void onProjectileLaunch(Player player, Projectile projectile, ProjectileLaunchEvent event) {}

    void onBowShoot(Player player, Projectile projectile, EntityShootBowEvent event) {}

    void onBlockDamage(Player player, Block block, BlockDamageEvent event) {}

    void onBlockBreak(Player player, Block block, BlockBreakEvent event) {}

    void onBlockBreakMonitor(Player player, Block block, BlockBreakEvent event) {}

    void onCrystalEquip(Player player) {}

    void onCrystalUnequip(Player player) {}

    void onCrystalAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {}

    void onCrystalKill(Player player, LivingEntity target, EntityDeathEvent event) {}

    void onCrystalDamaged(Player player, Entity attacker, EntityDamageByEntityEvent event) {}
}