package scripts.factions.eco.rewardbox.animation
//package scripts.factions.lootboxes.animation
//
//import groovy.transform.CompileStatic
//import net.minecraft.server.v1_16_R3.EntityRabbit
//import net.minecraft.server.v1_16_R3.EntityTypes
//import org.bukkit.*
//import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
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
//class EasterOpeningAnimation extends LootboxOpeningAnimation {
//
//    static final double REWARD_ANGLE_MIN = -45D
//    static final double REWARD_ANGLE_MAX = 45D
//    static final Material LOOTBOX_ITEM = Material.ENDER_CHEST
//    static final Map<String, Map<String, ?>> EGGS = [
//            purple: [
//                    itemModel: 118,
//                    glow: ChatColor.DARK_PURPLE,
//                    frames: [
//                            [108, 0F],
//                            [109, -30F],
//                            [110, -60F],
//                            [111, -90F],
//                            [112, -90F],
//                            [113, -90F],
//                            [114, -90F],
//                            [115, -90F],
//                            [116, -90F],
//                            [117, -90F]
//                    ].findResults { it as Frame },
//            ],
//            yellow: [
//                    itemModel: 138,
//                    glow: ChatColor.YELLOW,
//                    frames: [
//                            [128, 0F],
//                            [129, -30F],
//                            [130, -60F],
//                            [131, -90F],
//                            [132, -90F],
//                            [133, -90F],
//                            [134, -90F],
//                            [135, -90F],
//                            [136, -90F],
//                            [137, -90F]
//                    ].findResults { it as Frame },
//            ],
//            multi: [
//                    itemModel: 158,
//                    glow: ChatColor.AQUA,
//                    frames: [
//                            [148, 0F],
//                            [149, -30F],
//                            [150, -60F],
//                            [151, -90F],
//                            [152, -90F],
//                            [153, -90F],
//                            [154, -90F],
//                            [155, -90F],
//                            [156, -90F],
//                            [157, -90F]
//                    ].findResults { it as Frame },
//            ]
//    ] as Map<String, Map<String, ?>>
//
//    String egg
//    Location location
//    double groundLevel
//
//    LootBoxEntity lootBoxEntity
//    FloatingEntity rewardCycleEntity
//
//    List<LootBoxReward> rewardsCopy
//    List<LootBoxReward> allRewards
//    List<Double> rewardEntityShootAngles = new ArrayList<>()
//    List<RewardEntity> rewardEntities = new ArrayList<>()
//    List<FloatingEntity> rabbits = new ArrayList<>()
//
//    HologramTracker happyEasterHologram
//
//    EasterOpeningAnimation() {}
//
//    EasterOpeningAnimation(Player player, World world, LootBox lootBox, ItemStack itemStack) {
//        super(player, world, lootBox, itemStack, 180)
//
//        this.egg = EGGS.find { it.value["itemModel"] == itemStack.getItemMeta().getCustomModelData() }.key
//        this.location = player.getLocation()
//        this.groundLevel = location.getY()
//    }
//
//    @Override
//    void start() {
//        rollRewards()
//        rewardsCopy = new ArrayList<>(rolledRewards)
//        allRewards = lootBox.getRewardGroups().findResults { it.getRewards() }.flatten() as List<LootBoxReward>
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
//        player.playSound(location, Sound.ENTITY_TURTLE_LAY_EGG, 1F, 1F)
//
//        location.setY(location.getY() - 1D)
//        location.setYaw((location.getYaw() - 90F) % 360F as float)
//        location.setPitch(0F)
//
//        SmallFloatingBlock resourcePackFloatingBlock = new SmallFloatingBlock(world, location, FastItemUtils.withModelId(LOOTBOX_ITEM, (EGGS[egg]["frames"] as List<Frame>)[0].model), false)
//        resourcePackFloatingBlock.locationOffset = new Vector(0D, 0D, 0D)
//        resourcePackFloatingBlock.setGlowColor(EGGS[egg]["glow"] as ChatColor)
//        resourcePackFloatingBlock.visibilityPredicate = { Player player -> return player.hasResourcePack() } as Predicate<Player>
//        resourcePackFloatingBlock.track()
//
//        SmallFloatingBlock nonResourcePackFloatingBlock = new SmallFloatingBlock(world, location, Material.BROWN_SHULKER_BOX, false)
//        nonResourcePackFloatingBlock.setGlowColor(EGGS[egg]["glow"] as ChatColor)
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
//            if (lootBoxEntity.isMoving()) {
//                world.spawnParticle(Particle.FIREWORKS_SPARK, lootBoxEntity.getLocation(), 0)
//            } else if (lootBoxEntity.isOnGround() && !lootBoxEntity.hasTouchedGround) {
//                lootBoxEntity.hasTouchedGround = true
//
//                spawnRabbits()
//                FloatingEntity rabbit = RandomUtils.getRandom(rabbits)
//                Location location = rabbit.currentLocation.clone().add(0D, 1D, 0D)
//                happyEasterHologram = HologramRegistry.get().spawn("easterlootbox_${player.uniqueId}", location, ["§7ღ §a§lH§b§la§e§lp§d§lp§a§ly §b§lE§e§la§d§ls§a§lt§b§le§e§lr§d§l! §7ღ"], false)
//            }
//
//            if (lootBoxEntity.hasTouchedGround) {
//                List<Frame> frames = EGGS[egg]["frames"] as List<Frame>
//                boolean isEggAnimationFinished = lootBoxEntity.frame >= frames.size() - 1
//                if (!isEggAnimationFinished && tick % 2 == 0) {
//                    if (++lootBoxEntity.frame == 6) {
//                        player.playSound(lootBoxEntity.location, Sound.ENTITY_TURTLE_EGG_BREAK, 1F, 1F)
//                    }
//
//                    lootBoxEntity.updateModel(frames[lootBoxEntity.frame].model)
//                    lootBoxEntity.yaw = (float) (lootBoxEntity.initialRotation + frames[lootBoxEntity.frame].rotation)
//                }
//
//                if (!rewardsCopy.isEmpty()) {
//                    if (isEggAnimationFinished && tick % 4 == 0) {
//                        rewardCycleEntity?.untrack()
//
//                        Location spawnLocation = lootBoxEntity.getLocation().add(0D, 0.5D, 0D)
//                        LootBoxReward reward = RandomUtils.getRandom(allRewards)
//                        rewardCycleEntity = makeFloatingItem(world, spawnLocation, reward.displayItem, reward.glowColor, reward.displayName)
//                        rewardCycleEntity.track()
//
//                        spawnLocation = spawnLocation.clone().add(0D, 0.2D, 0D)
//                        4.times {
//                            world.spawnParticle(Particle.BLOCK_DUST, spawnLocation.x, spawnLocation.y, spawnLocation.z, 10, RandomUtils.getRandom(Material.values().findAll { it.name().endsWith("_WOOL") } as List<Material>).createBlockData())
//                        }
//                    }
//                } else {
//                    rewardCycleEntity?.untrack()
//                }
//
//                if (rewardsCopy.isEmpty() && rewardEntities.isEmpty()) {
//                    int remainingTicks = getDurationTicks() - tick
//                    if (remainingTicks == 30) {
//                        lootBoxEntity.remove()
//                        player.playSound(lootBoxEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
//                    }
//                }
//            }
//
//            lootBoxEntity.move()
//        }
//
//        if (tick >= 60 && tick % 10 == 0) {
//            trySpawnRewardEntity()
//        }
//
//        tickRewardEntities()
//        tickRabbits()
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
//            player.playSound(player.getLocation(), Sound.ENTITY_RABBIT_DEATH, 1F, 1F)
//        }
//    }
//
//    void spawnRabbits() {
//        [-ThreadLocalRandom.current().nextDouble(15D, 20D), ThreadLocalRandom.current().nextDouble(15D, 20D)].each { angle ->
//            Location spawnLocation = player.getLocation()
//            spawnLocation.add(lootBoxEntity.getLocation().clone().subtract(player.getLocation()).toVector().rotateAroundY(Math.toRadians(angle)))
//
//            Vector direction = player.getLocation().subtract(spawnLocation).toVector().normalize()
//            spawnLocation.setYaw(Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())) as float)
//            spawnLocation.setPitch( Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getZ(), 2)) as double)) as float)
//
//            FloatingEntity rabbit = new FloatingEntity(world, spawnLocation, {
//                EntityRabbit entityRabbit = new EntityRabbit(EntityTypes.RABBIT, (world as CraftWorld).getHandle())
//                entityRabbit.setRabbitType(RandomUtils.getRandom([0..5, 99].flatten() as List<Integer>))
//                return entityRabbit
//            })
//
//            rabbits.add(rabbit)
//            rabbit.track()
//
//            for (double y = 0D; y < 0.6D; y += 0.2D) {
//                world.spawnParticle(Particle.BLOCK_DUST, spawnLocation.clone().add(0D, y, 0D), 20, 0D, 0D, 0D, 0.15D, RandomUtils.getRandom(Material.values().findAll { it.name().endsWith("_WOOL") } as List<Material>).createBlockData(), true)
//            }
//        }
//    }
//
//    void tickRabbits() {
//        rabbits.each {
//            Location location = it.getCurrentLocation()
//            Vector direction = player.getLocation().subtract(location).toVector().normalize()
//            it.setYawPitch(
//                    Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())) as float,
//                    Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getZ(), 2)) as double)) as float
//            )
//        }
//    }
//
//    void trySpawnRewardEntity() {
//        if (rewardsCopy.isEmpty()) return
//
//        LootBoxReward lootBoxReward = rewardsCopy.remove(0)
//        ItemStack itemStack = lootBoxReward.getDisplayItem()
//        FloatingEntity floatingEntity = makeFloatingItem(world, lootBoxEntity.getLocation().add(0D, 0.5D, 0D), itemStack, lootBoxReward.glowColor, lootBoxReward.getDisplayName())
//        floatingEntity.track()
//
//        Vector vector = location.toVector().subtract(lootBoxEntity.getLocation().toVector()).normalize()
//        vector.rotateAroundY(Math.toRadians(rewardEntityShootAngles.remove(0)))
//        RewardEntity rewardEntity = new RewardEntity(floatingEntity, groundLevel)
//        rewardEntity.setMotion(vector.getX() * 0.1D, 0.9D, vector.getZ() * 0.1D)
//
//        rewardEntities.add(rewardEntity)
//
//        player.playSound(lootBoxEntity.getLocation(), Sound.BLOCK_DISPENSER_LAUNCH, 1F, 1F)
//    }
//
//    void tickRewardEntities() {
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
//        player.resetPlayerWeather()
//        lootBoxEntity?.remove()
//        rewardEntities.each { it.remove() }
//        rabbits.each { it.untrack() }
//        rewardCycleEntity?.untrack()
//        if (happyEasterHologram != null) {
//            HologramRegistry.get().unregister(happyEasterHologram)
//        }
//    }
//
//    static class LootBoxEntity extends WeightedEntity {
//
//        final World world
//        final SmallFloatingBlock nonResourcePackFloatingBlock
//        final float initialRotation
//
//        int frame = 0
//        boolean hasTouchedGround = false
//
//        LootBoxEntity(World world, SmallFloatingBlock smallFloatingBlock, SmallFloatingBlock nonResourcePackFloatingBlock, double groundLevel) {
//            super(smallFloatingBlock, groundLevel)
//
//            this.initialRotation = smallFloatingBlock.currentLocation.yaw
//            this.world = world
//            this.nonResourcePackFloatingBlock = nonResourcePackFloatingBlock
//        }
//
//        void updateModel(int model) {
//            ((SmallFloatingBlock)floatingEntity).updateItemStack(FastItemUtils.withModelId(LOOTBOX_ITEM, model))
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
//    static class Frame {
//
//        int model
//        float rotation
//
//        Frame(int model, float rotation = 0F) {
//            this.model = model
//            this.rotation = rotation
//        }
//
//    }
//
//}
