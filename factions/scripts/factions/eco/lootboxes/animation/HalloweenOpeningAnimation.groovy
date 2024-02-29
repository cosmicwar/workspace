package scripts.factions.eco.lootboxes.animation
//package scripts.factions.lootboxes.animation
//
//import groovy.transform.CompileStatic
//import net.minecraft.server.v1_16_R3.EntityArmorStand
//import net.minecraft.server.v1_16_R3.Vector3f
//import org.bukkit.*
//import org.bukkit.entity.Player
//import org.bukkit.inventory.ItemStack
//import org.bukkit.util.Vector
//import scripts.shared.features.holograms.HologramRegistry
//import scripts.shared.features.holograms.HologramTracker
//import scripts.shared.features.lootbox.data.LootBox
//import scripts.shared.features.lootbox.data.LootBoxReward
//import scripts.shared.legacy.utils.FastItemUtils
//import scripts.shared.legacy.utils.RandomUtils
//import scripts.shared.visuals.ParticleUtil
//import scripts.shared.visuals.floating.AbstractFloatingEntity
//import scripts.shared.visuals.floating.FloatingEntity
//import scripts.shared.visuals.floating.SmallFloatingBlock
//import scripts.shared.visuals.floating.WeightedEntity
//
//import java.util.concurrent.ThreadLocalRandom
//import java.util.function.Predicate
//
//@CompileStatic
//class HalloweenOpeningAnimation extends LootboxOpeningAnimation {
//
//    static final double REWARD_ANGLE_MIN = -45D
//    static final double REWARD_ANGLE_MAX = 45D
//
//    static final List<Material> HALLOWEEN_MATERIALS = [Material.PUMPKIN, Material.JACK_O_LANTERN, Material.COBWEB, Material.SPIDER_EYE]
//
//    static final List<ParticleUtil.ParticleData> LOOTBOX_DESPAWN_PARTICLES = [
//            new ParticleUtil.ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.ORANGE, 1)),
//            new ParticleUtil.ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.BLACK, 1))
//    ]
//
//    Location location
//    double groundLevel
//
//    HologramTracker hologramTracker
//    List<Double> rewardEntityShootAngles = new ArrayList<>()
//    List<LootBoxReward> rewardsCopy = new ArrayList<>()
//    LootBoxEntity lootBoxEntity
//    List<RewardEntity> rewardEntities = new ArrayList<>()
//    List<WeightedEntity> miscEntities = new ArrayList<>()
//
//    HalloweenOpeningAnimation() {
//    }
//
//    HalloweenOpeningAnimation(Player player, World world, LootBox lootBox, ItemStack itemStack) {
//        super(player, world, lootBox, itemStack, 180)
//
//        this.location = player.getLocation()
//        this.groundLevel = location.getY()
//    }
//
//    @Override
//    void start() {
//        rollRewards()
//        rewardsCopy.addAll(rolledRewards)
//
//        double totalAngle = REWARD_ANGLE_MAX - REWARD_ANGLE_MIN
//        if (rewardsCopy.size() == 1) {
//            rewardEntityShootAngles.add(0D)
//        } else if (rewardsCopy.size() == 2) {
//            double angle = totalAngle / 3D
//            rewardEntityShootAngles.add(-angle)
//            rewardEntityShootAngles.add(angle)
//        } else {
//            double angleStep = totalAngle / (rewardsCopy.size() - 1)
//            for (double angle = REWARD_ANGLE_MIN; angle <= REWARD_ANGLE_MAX; angle += angleStep) {
//                rewardEntityShootAngles.add(angle)
//            }
//        }
//        Collections.shuffle(rewardEntityShootAngles)
//
//        Location location = player.getEyeLocation()
//        player.playSound(location, Sound.BLOCK_DISPENSER_LAUNCH, 1F, 1F)
//
//        location.setY(location.getY() - 1D)
//        location.setYaw((location.getYaw() + 180F) % 360F as float)
//        location.setPitch(0F)
//
//        SmallFloatingBlock resourcePackFloatingBlock = new SmallFloatingBlock(world, location, FastItemUtils.withModelId(Material.JACK_O_LANTERN, 2), false)
//        resourcePackFloatingBlock.locationOffset = new Vector(0D, -1.05D, 0D)
//        resourcePackFloatingBlock.setGlowColor(ChatColor.GOLD)
//        resourcePackFloatingBlock.visibilityPredicate = { Player player -> return player.hasResourcePack() } as Predicate<Player>
//        resourcePackFloatingBlock.track()
//
//        SmallFloatingBlock nonResourcePackFloatingBlock = new SmallFloatingBlock(world, location, Material.JACK_O_LANTERN, false)
//        nonResourcePackFloatingBlock.setGlowColor(ChatColor.GOLD)
//        nonResourcePackFloatingBlock.visibilityPredicate = { Player player -> return !player.hasResourcePack() } as Predicate<Player>
//        nonResourcePackFloatingBlock.track()
//
//        lootBoxEntity = new LootBoxEntity(resourcePackFloatingBlock, nonResourcePackFloatingBlock, groundLevel)
//
//        double shootVelocity = Math.toRadians(player.getEyeLocation().getYaw() + 90F)
//        lootBoxEntity.setMotion(Math.cos(shootVelocity) * 0.35D, 0.5D, Math.sin(shootVelocity) * 0.35D)
//    }
//
//    @Override
//    void onTick(int tick) {
//        if (lootBoxEntity != null) {
//            if (lootBoxEntity.isMoving()) {
//                world.spawnParticle(Particle.FIREWORKS_SPARK, lootBoxEntity.getLocation(), 0)
//            } else if (lootBoxEntity.isOnGround() && !lootBoxEntity.hasTouchedGround) {
//                lootBoxEntity.hasTouchedGround = true
//            }
//
//            if (lootBoxEntity.hasTouchedGround) {
//                if (tick % 2 == 0 && lootBoxEntity.getCurrentModel() < LootBoxEntity.END_MODEL) {
//                    lootBoxEntity.incrementModel()
//                }
//
//                if (lootBoxEntity.isOnGround() && rewardsCopy.isEmpty() && rewardEntities.isEmpty()) {
//                    int remainingTicks = getDurationTicks() - tick
//                    if (remainingTicks == 30) {
//                        lootBoxEntity.ticksExploding = 1
//                        lootBoxEntity.remove()
//
//                        player.playSound(lootBoxEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
//                        20.times {
//                            world.spawnParticle(Particle.EXPLOSION_LARGE, lootBoxEntity.getLocation().add(
//                                    ThreadLocalRandom.current().nextDouble(-2D, 2D),
//                                    0.5D + ThreadLocalRandom.current().nextDouble(2D),
//                                    ThreadLocalRandom.current().nextDouble(-2D, 2D)
//                            ), 0)
//
//                            FloatingEntity floatingEntity = makeFloatingItem(world, lootBoxEntity.getLocation().add(0D, 0.1D, 0D), new ItemStack(RandomUtils.getRandom(HALLOWEEN_MATERIALS)), RandomUtils.getRandom([ChatColor.BLACK, ChatColor.GOLD]), null)
//                            floatingEntity.track()
//                            WeightedEntity floatingObject = new WeightedEntity(floatingEntity, groundLevel)
//                            floatingObject.setMotion(
//                                    ThreadLocalRandom.current().nextDouble(-0.3D, 0.3D),
//                                    ThreadLocalRandom.current().nextDouble(0.4D, 0.6D),
//                                    ThreadLocalRandom.current().nextDouble(-0.3D, 0.3D)
//                            )
//
//                            miscEntities.add(floatingObject)
//                        }
//                    }
//
//                    if (lootBoxEntity.ticksExploding > 0 && lootBoxEntity.ticksExploding < 10) {
//                        double size = (lootBoxEntity.ticksExploding / 10D) * 3D
//                        if (size > 0D) {
//                            ParticleUtil.spawnParticleSphere(lootBoxEntity.getLocation().add(0D, 0.5D, 0D), size, Math.ceil(size * 8D) as int, LOOTBOX_DESPAWN_PARTICLES)
//                        }
//
//                        lootBoxEntity.ticksExploding++
//                    }
//                }
//
////                lootBoxEntity.setYaw(lootBoxEntity.getYaw() + 11.25F as float)
////                lootBoxEntity.setHeadPose(new Vector3f(0F, lootBoxEntity.getHeadPose().getY() + 11.25F as float, 0F))
//            }
//
//            lootBoxEntity.move()
//        }
//
//        if (tick >= 40 && tick % 20 == 0 && !rewardsCopy.isEmpty()) {
//            lootBoxEntity.addMotion(0D, 0.3D, 0D)
//            LootBoxReward lootBoxReward = rewardsCopy.remove(0)
//            ItemStack itemStack = lootBoxReward.getDisplayItem()
//
//            FloatingEntity floatingEntity = makeFloatingItem(world, lootBoxEntity.getLocation().add(0D, 0.1D, 0D), itemStack, lootBoxReward.glowColor, lootBoxReward.getDisplayName())
//            floatingEntity.track()
//
//            Vector vector = location.toVector().subtract(lootBoxEntity.getLocation().toVector()).normalize()
//            vector.rotateAroundY(Math.toRadians(rewardEntityShootAngles.remove(0)))
//            RewardEntity rewardEntity = new RewardEntity(floatingEntity, groundLevel)
//            rewardEntity.setMotion(
//                    vector.getX() * 0.2D,
//                    0.6D,
//                    vector.getZ() * 0.2D
//            )
//
//            rewardEntities.add(rewardEntity)
//
//            player.playSound(lootBoxEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
//            world.spawnParticle(Particle.EXPLOSION_LARGE, lootBoxEntity.getLocation().add(0D, 0.5D, 0D), 0)
//        }
//
//        rewardEntities.removeIf({ rewardEntity ->
//            if (rewardEntity.isMoving()) {
//                ChatColor glowColor = rewardEntity.getFloatingEntity().getGlowColor()
//                if (glowColor != null) {
//                    world.spawnParticle(Particle.REDSTONE, rewardEntity.getLocation(), 1, new Particle.DustOptions(convertChatColorToColor(glowColor), 2))
//                }
//
//                world.spawnParticle(Particle.FIREWORKS_SPARK, rewardEntity.getLocation(), 0)
//            }
//
//            if (rewardEntity.isOnGround() && ++rewardEntity.ticksOnGround >= 30) {
//                Location itemCenter = rewardEntity.getLocation().add(0D, 0.5D, 0D)
//                if (rewardEntity.particleSphereRadius > 0D) {
//                    List<ParticleUtil.ParticleData> particles = new ArrayList<>()
//                    particles.add(new ParticleUtil.ParticleData(Particle.FIREWORKS_SPARK))
//                    if (rewardEntity.getFloatingEntity().getGlowColor() != null) {
//                        particles.add(new ParticleUtil.ParticleData(Particle.REDSTONE, new Particle.DustOptions(convertChatColorToColor(rewardEntity.getFloatingEntity().getGlowColor()), 1)))
//                    }
//
//                    ParticleUtil.spawnParticleSphere(itemCenter, rewardEntity.getParticleSphereRadius(), Math.ceil(rewardEntity.particleSphereRadius * 8D) as int, particles)
//                    rewardEntity.particleSphereRadius -= 0.15D
//                } else if (!rewardEntity.popped) {
//                    rewardEntity.popped = true
//                    rewardEntity.remove()
//
//                    world.spawnParticle(Particle.EXPLOSION_LARGE, itemCenter, 0)
//                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F)
//                    return true
//                }
//            }
//
//            rewardEntity.move()
//            return false
//        })
//
//        miscEntities.each { it.move() }
//
//        if (isFinalTick(tick)) {
//            10.times {
//                world.spawnParticle(Particle.EXPLOSION_LARGE, lootBoxEntity.getLocation().add(
//                        ThreadLocalRandom.current().nextDouble(-2D, 2D),
//                        0.5D + ThreadLocalRandom.current().nextDouble(2D),
//                        ThreadLocalRandom.current().nextDouble(-2D, 2D)
//                ), 0)
//            }
//
//            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1F, 1F)
//        }
//    }
//
//    @Override
//    void cleanup() {
//        lootBoxEntity?.remove()
//        rewardEntities.each { it.remove() }
//        miscEntities.each { it.remove() }
//        if (hologramTracker != null) {
//            HologramRegistry.get().unregister(hologramTracker)
//        }
//    }
//
//    static class LootBoxEntity extends WeightedEntity {
//
//        static final int START_MODEL = 2
//        static final int END_MODEL = 7
//
//        int currentModel = START_MODEL
//        boolean hasTouchedGround = false
//        int ticksExploding = 0
//        SmallFloatingBlock nonResourcePackFloatingBlock
//
//        LootBoxEntity(SmallFloatingBlock smallFloatingBlock, SmallFloatingBlock nonResourcePackFloatingBlock, double groundLevel) {
//            super(smallFloatingBlock, groundLevel)
//            this.nonResourcePackFloatingBlock = nonResourcePackFloatingBlock
//        }
//
//        void incrementModel() {
//            currentModel++
//            ((SmallFloatingBlock)floatingEntity).updateItemStack(FastItemUtils.withModelId(Material.JACK_O_LANTERN, currentModel))
//        }
//
//        Vector3f getHeadPose() {
//            return ((SmallFloatingBlock)floatingEntity).getEntityArmorStand().r()
//        }
//
//        void setHeadPose(Vector3f vector3f) {
//            EntityArmorStand entityArmorStand = ((SmallFloatingBlock)floatingEntity).getEntityArmorStand()
//            entityArmorStand.setHeadPose(vector3f)
//            floatingEntity.refreshMetaData()
//        }
//
//        @Override
//        void move() {
//            super.move()
//            Location newLocation = getLocation()
//            nonResourcePackFloatingBlock.moveToWithRotation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), yaw, pitch)
//        }
//
//        @Override
//        void remove() {
//            super.remove()
//            nonResourcePackFloatingBlock.untrack()
//        }
//
//    }
//
//    static class RewardEntity extends WeightedEntity {
//
//        int ticksOnGround
//        double particleSphereRadius = 1D
//        boolean popped = false
//
//        RewardEntity(AbstractFloatingEntity floatingEntity, double groundLevel) {
//            super(floatingEntity, groundLevel)
//        }
//
//    }
//
//}
//
