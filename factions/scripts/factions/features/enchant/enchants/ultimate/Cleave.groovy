package scripts.factions.features.enchant.enchants.ultimate
//package scripts.factions.features.enchant.enchants.axe
//
//import org.starcade.starlight.helper.Schedulers
//import org.bukkit.Color
//import org.bukkit.Location
//import org.bukkit.Particle
//import org.bukkit.Sound
//import org.bukkit.entity.LivingEntity
//import org.bukkit.entity.Player
//import org.bukkit.event.entity.EntityDamageByEntityEvent
//import org.bukkit.inventory.ItemStack
//import scripts.factions.features.enchant.struct.EnchantmentTier
//import scripts.factions.features.enchant.struct.CustomEnchantment
//import scripts.factions.features.enchant.utils.EnchantUtils
//
//class scripts.factions.features.enchant.enchants.ultimate.Cleave extends CustomEnchantment {
//
//    Closure<Double> damageCalc
//
//    scripts.factions.features.enchant.enchants.ultimate.Cleave() {
//        super("cleave", EnchantmentTier.SIMPLE)
//    }
//
//    @Override
//    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
//        if (!EnchantUtils.isBoss(target) || !proc(player, enchantLevel) || isEnchantDamage(player)) return
//
//        Location playerLocation = player.getLocation()
//        double radius = Math.min(4D, Math.ceil(playerLocation.distance(target.getLocation())))
//        int yaw = playerLocation.getYaw() + 90F as int
//        int delay = 0
//        double height = 0D
//        for (int i = yaw - 45; i < yaw + 45; i += 10) {
//            double angle = Math.toRadians(i)
//            final double heightOffset = height
//            Schedulers.async().runLater({
//                for (double r = 0D; r <= radius; r += 0.5D) {
//                    double x = r * Math.cos(angle)
//                    double z = r * Math.sin(angle)
//
//                    Location location = player.location.add(x, heightOffset + (r * 0.2D), z)
//                    player.world.spawnParticle(Particle.REDSTONE, location, 1, new Particle.DustOptions(Color.ORANGE, 1))
//                }
//            }, delay++)
//            height += 0.15D
//        }
//
//        player.playSound(target.getLocation(), Sound.BLOCK_BASALT_BREAK, 1F, 1F)
//        damage(player, target, damageCalc.call(enchantLevel))
//    }
//
//    @Override
//    void onReload() {
//        super.onReload()
//        this.damageCalc = getConfig()["damageCalc"] as Closure<Double>
//    }
//
//}
