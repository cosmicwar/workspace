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
//import scripts.shared.features.lootbox.data.LootBox
//import scripts.shared.features.lootbox.data.LootBoxReward
//import scripts.shared.legacy.utils.FastItemUtils
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
//class ThanksgivingOpeningAnimation extends LootboxOpeningAnimation {
//
//    static final double REWARD_ANGLE_MIN = -45D
//    static final double REWARD_ANGLE_MAX = 45D
//
//    Location location
//    double groundLevel
//
//    LootBoxEntity lootBoxEntity
//
//    List<LootBoxReward> rewardsCopy = new ArrayList<>()
//    List<Double> rewardEntityShootAngles = new ArrayList<>()
//    List<RewardEntity> rewardEntities = new ArrayList<>()
//
//    ThanksgivingOpeningAnimation() {
//    }
//
//    ThanksgivingOpeningAnimation(Player player, World world, LootBox lootBox, ItemStack itemStack) {
//        super(player, world, lootBox, itemStack, 200)
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
//        if (player.hasResourcePack()) {
//            player.playSound(location, "mch.thanksgiving", 1F, 1F)
//        } else {
//            player.playSound(location, Sound.BLOCK_DISPENSER_LAUNCH, 1F, 1F)
//        }
//
//        location.setY(location.getY() - 1D)
//        location.setYaw((location.getYaw() + 180F) % 360F as float)
//        location.setPitch(0F)
//
//        SmallFloatingBlock resourcePackFloatingBlock = new SmallFloatingBlock(world, location, FastItemUtils.withModelId(Material.BROWN_SHULKER_BOX, 2), false)
//        resourcePackFloatingBlock.locationOffset = new Vector(0D, -1.95D, 0D)
//        resourcePackFloatingBlock.setGlowColor(ChatColor.YELLOW)
//        resourcePackFloatingBlock.visibilityPredicate = { Player player -> return player.hasResourcePack() } as Predicate<Player>
//        resourcePackFloatingBlock.track()
//
//        SmallFloatingBlock nonResourcePackFloatingBlock = new SmallFloatingBlock(world, location, Material.BROWN_SHULKER_BOX, false)
//        nonResourcePackFloatingBlock.setGlowColor(ChatColor.YELLOW)
//        nonResourcePackFloatingBlock.visibilityPredicate = { Player player -> return !player.hasResourcePack() } as Predicate<Player>
//        nonResourcePackFloatingBlock.track()
//
//        lootBoxEntity = new LootBoxEntity(world, resourcePackFloatingBlock, nonResourcePackFloatingBlock, groundLevel)
//
//        double shootVelocity = Math.toRadians(player.getEyeLocation().getYaw() + 90F)
//        lootBoxEntity.setMotion(Math.cos(shootVelocity) * 0.35D, 0.5D, Math.sin(shootVelocity) * 0.35D)
//    }
//
//    @Override
//    void onTick(int tick) {
//        if (lootBoxEntity != null) {
////            if (tick == 30) {
////                lootBoxEntity.spawnGiblets()
////            }
//            if (tick == 50) {
//                lootBoxEntity.addMotion(0D, 0.35D, 0D)
//            } else if (tick == 60) {
//                lootBoxEntity.updateStatus(LootBoxEntity.Status.EXPLODING)
//            }
//
//            if (tick >= 60 && !rewardsCopy.isEmpty() && tick % 10 == 0) {
//                LootBoxReward lootBoxReward = rewardsCopy.remove(0)
//                ItemStack itemStack = lootBoxReward.getDisplayItem()
//                FloatingEntity floatingEntity = makeFloatingItem(world, lootBoxEntity.getLocation().add(0D, 1.5D, 0D), itemStack, lootBoxReward.glowColor, lootBoxReward.getDisplayName())
//                floatingEntity.track()
//
//                Vector vector = location.toVector().subtract(lootBoxEntity.getLocation().toVector()).normalize()
//                vector.rotateAroundY(Math.toRadians(rewardEntityShootAngles.remove(0)))
//                RewardEntity rewardEntity = new RewardEntity(floatingEntity, groundLevel)
//                rewardEntity.setMotion(vector.getX() * 0.1D, 0.9D, vector.getZ() * 0.1D)
//
//                rewardEntities.add(rewardEntity)
//            }
//
//            lootBoxEntity.tick()
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
//    }
//
//    @Override
//    void cleanup() {
//        lootBoxEntity?.remove()
//        rewardEntities.each { it.remove() }
//    }
//
//    static class LootBoxEntity extends WeightedEntity {
//
//        static final int FLAP_START_MODEL = 2
//        static final int FLAP_END_MODEL = 7
//        static final int EXPLODE_START_MODEL = 9
//        static final int EXPLODE_END_MODEL = 11
//
//        static final Map<GibletType, Giblet> GIBLETS = [
//                (GibletType.BEAK)     : new Giblet(12, [new Giblet.SpawnData(-0.5D, 0F, -25F, 0F, new Vector(0D, 0.975D, 1.35D), new Vector(0D, 0D, 0D))]), // done
//                (GibletType.BODY)     : new Giblet(13, [new Giblet.SpawnData(-0.5D, 0F, 0F, 0F, new Vector(0D, 0.15D, 0D), new Vector(0D, 0D, 0D))]), // done
//                (GibletType.FOOT)     : new Giblet(14, [
//                        new Giblet.SpawnData(-0.5D, 0F, 0F, -112F, new Vector(1.05D, 0.45D, 0D), new Vector(0D, 0D, 0D)), // right
//                        new Giblet.SpawnData(-0.5D, 0F, 0F, 90F, new Vector(-1D, 0.3125D, 0.1D), new Vector(0D, 0D, 0D)) // left
//                ]),
//                (GibletType.HEAD)     : new Giblet(15, [new Giblet.SpawnData(-0.5D, 0F, 45F, 0F, new Vector(0D, 1.3D, -0.05D), new Vector(0D, 0D, 0D))]), // done
//                (GibletType.LEFT_EYE) : new Giblet(16, [new Giblet.SpawnData(-0.5D, 25F, 0F, 0F, new Vector(-0.35D, 1.2D, 0.875D), new Vector(0D, 0D, 0D))]), // done
//                (GibletType.RIGHT_EYE): new Giblet(17, [new Giblet.SpawnData(-0.5D, -23F, 0F, 0F, new Vector(0.525D, 1.3D, 0.925D), new Vector(0D, 0D, 0D))]), // done
//                (GibletType.NECK)     : new Giblet(18, [new Giblet.SpawnData(-0.5D, 0F, 45F, 0F, new Vector(0D, 0.625D, 0.2D), new Vector(0D, 0D, 0D))]), // done
//                (GibletType.TAIL)     : new Giblet(19, [new Giblet.SpawnData(-0.5D, 0F, 67F, 0F, new Vector(0D, 1.85D, -1.6D), new Vector(0D, 0D, 0D))]), // done
//                (GibletType.WING)     : new Giblet(20, [
//                        new Giblet.SpawnData(-0.5D, 0F, 0F, 70F, new Vector(0D, 0.55D, -0.225D), new Vector(0D, 0D, 0D)), // right
//                        new Giblet.SpawnData(-0.5D, 0F, 0F, 110F, new Vector(-1.7D, 1.5D, -0.225D), new Vector(0D, 0D, 0D)) // left
//                ]),
//        ]
//
//        World world
//        SmallFloatingBlock nonResourcePackFloatingBlock
//
//        Status status = Status.FLAPPING
//        int currentModel = FLAP_START_MODEL
//        int flapDirection = 1
//        List<WeightedEntity> giblets = new ArrayList<>()
//
//        LootBoxEntity(World world, SmallFloatingBlock smallFloatingBlock, SmallFloatingBlock nonResourcePackFloatingBlock, double groundLevel) {
//            super(smallFloatingBlock, groundLevel)
//
//            this.world = world
//            this.nonResourcePackFloatingBlock = nonResourcePackFloatingBlock
//        }
//
//        void tick() {
//            switch (status) {
//                case Status.FLAPPING:
//                    currentModel += flapDirection
//                    if (currentModel >= FLAP_END_MODEL || currentModel <= FLAP_START_MODEL) {
//                        flapDirection *= -1
//                    }
//
//                    ((SmallFloatingBlock)floatingEntity).updateItemStack(FastItemUtils.withModelId(Material.BROWN_SHULKER_BOX, currentModel))
//                    break
//                case Status.EXPLODING:
//                    currentModel += 1
//                    if (currentModel > EXPLODE_END_MODEL) {
//                        updateStatus(Status.GIBBED)
//                        return
//                    }
//
//                    ((SmallFloatingBlock)floatingEntity).updateItemStack(FastItemUtils.withModelId(Material.BROWN_SHULKER_BOX, currentModel))
//                    break
//                case Status.GIBBED:
//                    break
//            }
//
//            move()
//        }
//
//        void updateStatus(Status status) {
//            switch (status) {
//                case Status.FLAPPING:
//                    currentModel = FLAP_START_MODEL
//                    break
//                case Status.EXPLODING:
//                    currentModel = EXPLODE_START_MODEL
//                    break
//                case Status.GIBBED:
//                    remove()
//                    spawnGiblets()
//                    break
//            }
//
//            this.status = status
//        }
//
//        void spawnGiblets() {
//            world.playSound(getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
//            world.spawnParticle(Particle.EXPLOSION_HUGE, getLocation().add(0D, 0.1D, 0D), 0)
//            world.spawnParticle(Particle.EXPLOSION_HUGE, getLocation().add(0D, 1D, 0D), 0)
//
//            GIBLETS.each {
//                GibletType gibletType = it.getKey()
//                Giblet gibletData = it.getValue()
//                gibletData.getSpawnDatas().each { spawnData ->
//                    Location spawnLocation = getLocation().add(0D, 0.1D, 0D)
//                    Vector vector = spawnData.getSpawnOffset().clone().rotateAroundY(Math.toRadians(-getYaw()))
//                    spawnLocation.add(vector)
//                    spawnLocation.setYaw((getYaw() + spawnData.getYaw()) % 360F)
//
//                    SmallFloatingBlock floatingEntity = new SmallFloatingBlock(world, spawnLocation, FastItemUtils.withModelId(Material.BROWN_SHULKER_BOX, gibletData.getModel()), false)
//                    floatingEntity.locationOffset = new Vector(0D, SmallFloatingBlock.OFFSET_SMALL.getY() + spawnData.getVisualHeightOffset(), 0D)
//                    floatingEntity.track()
//
//                    EntityArmorStand entityArmorStand = ((SmallFloatingBlock)floatingEntity).getEntityArmorStand()
//                    entityArmorStand.setHeadPose(new Vector3f(spawnData.getPitch(), 0F, spawnData.getRoll()))
//                    floatingEntity.refreshMetaData()
//
//                    WeightedEntity floatingObject = new WeightedEntity(floatingEntity, groundLevel)
//                    vector.normalize().multiply(ThreadLocalRandom.current().nextDouble(0.3D, 0.5D)).add(new Vector(0D, 0.05D, 0D))
//                    floatingObject.setMotion(vector.getX(), vector.getY(), vector.getZ())
//
//                    giblets.add(floatingObject)
//                }
//            }
//        }
//
//        @Override
//        void move() {
//            super.move()
//            Location newLocation = getLocation()
//            nonResourcePackFloatingBlock.moveToWithRotation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), yaw, pitch)
//
//            giblets.each { it.move() }
//        }
//
//        @Override
//        void remove() {
//            super.remove()
//            nonResourcePackFloatingBlock.untrack()
//
//            giblets.each { it.remove() }
//        }
//
//        static enum Status {
//
//            FLAPPING,
//            EXPLODING,
//            GIBBED
//
//        }
//
//        static enum GibletType {
//
//            BEAK,
//            BODY,
//            FOOT,
//            HEAD,
//            LEFT_EYE,
//            RIGHT_EYE,
//            NECK,
//            TAIL,
//            WING
//
//        }
//
//        static class Giblet {
//
//            int model
//            List<SpawnData> spawnDatas
//
//            Giblet(int model, List<SpawnData> spawnDatas) {
//                this.model = model
//                this.spawnDatas = spawnDatas
//            }
//
//            static class SpawnData {
//
//                double visualHeightOffset
//                float yaw, pitch, roll
//                Vector spawnOffset
//                Vector spawnVelocity
//
//                SpawnData(double visualHeightOffset, float yaw, float pitch, float roll, Vector spawnOffset, Vector spawnVelocity) {
//                    this.visualHeightOffset = visualHeightOffset
//                    this.yaw = yaw
//                    this.pitch = pitch
//                    this.roll = roll
//                    this.spawnOffset = spawnOffset
//                    this.spawnVelocity = spawnVelocity
//                }
//
//            }
//
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
