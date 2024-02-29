package scripts.factions.eco.lootboxes.animation
//package scripts.factions.lootboxes.animation
//
//import groovy.transform.CompileStatic
//import org.bukkit.*
//import org.bukkit.entity.Player
//import org.bukkit.inventory.ItemStack
//import org.bukkit.util.Vector
//import scripts.shared.features.lootbox.data.LootBox
//import scripts.shared.features.lootbox.data.LootBoxReward
//import scripts.shared.legacy.utils.FastItemUtils
//import scripts.shared.legacy.utils.npc.NPCRegistry
//import scripts.shared.legacy.utils.npc.NPCTracker
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
//class ValentinesOpeningAnimation extends LootboxOpeningAnimation {
//
//    static final double REWARD_ANGLE_MIN = -45D
//    static final double REWARD_ANGLE_MAX = 45D
//    static final Map<Integer, ChatColor> PRESENT_COLORS = [
//            1 : ChatColor.RED,
//            21: ChatColor.DARK_PURPLE,
//            41: ChatColor.DARK_BLUE,
//            61: ChatColor.DARK_GREEN,
//    ]
//
//    Location location
//    double groundLevel
//
//    LootBoxEntity lootBoxEntity
//    CupidNpc cupidNpc
//
//    List<LootBoxReward> rewardsCopy = new ArrayList<>()
//    List<Double> rewardEntityShootAngles = new ArrayList<>()
//    List<RewardEntity> rewardEntities = new ArrayList<>()
//    List<FloatingEntity> clouds = new ArrayList<>()
//
//    ValentinesOpeningAnimation() {
//
//    }
//
//    ValentinesOpeningAnimation(Player player, World world, LootBox lootBox, ItemStack itemStack) {
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
//        player.playSound(location, Sound.ENTITY_VILLAGER_CELEBRATE, 1F, 1F)
//
//        location.setY(location.getY() - 1D)
//        location.setYaw(location.getYaw() % 360F as float)
//        location.setPitch(0F)
//
//        SmallFloatingBlock resourcePackFloatingBlock = new SmallFloatingBlock(world, location, FastItemUtils.withModelId(Material.POPPY, 2), false)
//        resourcePackFloatingBlock.locationOffset = new Vector(0D, -0.75D, 0D)
//        resourcePackFloatingBlock.setGlowColor(ChatColor.RED)
//        resourcePackFloatingBlock.visibilityPredicate = { Player player -> return player.hasResourcePack() } as Predicate<Player>
//        resourcePackFloatingBlock.track()
//
//        SmallFloatingBlock nonResourcePackFloatingBlock = new SmallFloatingBlock(world, location, Material.RED_SHULKER_BOX, false)
//        nonResourcePackFloatingBlock.setGlowColor(ChatColor.RED)
//        nonResourcePackFloatingBlock.visibilityPredicate = { Player player -> return !player.hasResourcePack() } as Predicate<Player>
//        nonResourcePackFloatingBlock.track()
//
//        lootBoxEntity = new LootBoxEntity(world, resourcePackFloatingBlock, nonResourcePackFloatingBlock, 2, groundLevel)
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
//                spawnCupid()
//            }
//
//            if (lootBoxEntity.hasTouchedGround) {
//                if (tick % 2 == 0 && lootBoxEntity.getCurrentModel() < lootBoxEntity.getFinalModel()) {
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
//                    }
//                }
//            }
//
//            lootBoxEntity.move()
//        }
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
//            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1F, 1F)
//        }
//
//        trySpawnRewardEntity(tick)
//        tickRewardEntities()
//    }
//
//    void spawnCupid() {
//        Location spawnLocation = player.getLocation()
//        spawnLocation.add(lootBoxEntity.getLocation().clone().subtract(player.getLocation()).toVector().rotateAroundY(Math.toRadians(20D)))
//        spawnLocation.setY(groundLevel)
//
//        Vector direction = player.getLocation().subtract(spawnLocation).toVector().normalize()
//        spawnLocation.setYaw(-30F + Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())) as float)
//        spawnLocation.setPitch(Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getZ(), 2)) as double)) as float)
//
//        NPCTracker npcTracker = NPCRegistry.get().spawn("valentineslootbox_${player.getUniqueId().toString()}", "§c§lH§f§la§c§lp§f§lp§c§ly §f§lV§c§la§f§ll§c§le§f§ln§c§lt§f§li§c§ln§f§le§c§ls§f§l!", spawnLocation, "d0663b2a-34e2-41be-91cb-82481255d469")
//        npcTracker.setHand(FastItemUtils.createEnchantedItem(Material.BOW, "", []))
//        npcTracker.setChestplate(Material.ELYTRA)
//        npcTracker.turnTowardPlayers = true
//
//        cupidNpc = new CupidNpc(npcTracker)
//    }
//
//    void trySpawnRewardEntity(int tick) {
//        if (tick >= 60 && !rewardsCopy.isEmpty() && tick % 10 == 0) {
//            LootBoxReward lootBoxReward = rewardsCopy.remove(0)
//            ItemStack itemStack = lootBoxReward.getDisplayItem()
//            FloatingEntity floatingEntity = makeFloatingItem(world, lootBoxEntity.getLocation().add(0D, 0.5D, 0D), itemStack, lootBoxReward.glowColor, lootBoxReward.getDisplayName())
//            floatingEntity.track()
//
//            Location particleSpawnLoc = lootBoxEntity.getLocation()
//            10.times {
//                world.spawnParticle(
//                        Particle.EXPLOSION_NORMAL,
//                        particleSpawnLoc.x,
//                        particleSpawnLoc.y,
//                        particleSpawnLoc.z,
//                        0,
//                        ThreadLocalRandom.current().nextGaussian() * 0.2D,
//                        ThreadLocalRandom.current().nextGaussian() * 0.2D,
//                        ThreadLocalRandom.current().nextGaussian() * 0.2D
//                )
//            }
//
//            5.times {
//                world.spawnParticle(
//                        Particle.HEART,
//                        particleSpawnLoc.x + ThreadLocalRandom.current().nextDouble(-2D, 2D),
//                        particleSpawnLoc.y + 1D + ThreadLocalRandom.current().nextDouble(),
//                        particleSpawnLoc.z + ThreadLocalRandom.current().nextDouble(-2D, 2D),
//                        0
//                )
//            }
//
//            Vector vector = location.toVector().subtract(lootBoxEntity.getLocation().toVector()).normalize()
//            vector.rotateAroundY(Math.toRadians(rewardEntityShootAngles.remove(0)))
//            RewardEntity rewardEntity = new RewardEntity(floatingEntity, groundLevel)
//            rewardEntity.setMotion(vector.getX() * 0.1D, 0.9D, vector.getZ() * 0.1D)
//
//            rewardEntities.add(rewardEntity)
//
//            world.playSound(lootBoxEntity.getLocation(), Sound.BLOCK_DISPENSER_LAUNCH, 1F, 1F)
//        }
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
//
//                    3.times {
//                        world.spawnParticle(
//                                Particle.HEART,
//                                itemCenter.x + ThreadLocalRandom.current().nextDouble(-2D, 2D),
//                                itemCenter.y + ThreadLocalRandom.current().nextDouble(),
//                                itemCenter.z + ThreadLocalRandom.current().nextDouble(-2D, 2D),
//                                0
//                        )
//                    }
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
//        clouds.each { it.untrack() }
//        cupidNpc?.remove()
//    }
//
//    static class LootBoxEntity extends WeightedEntity {
//
//        static final int MODEL_FRAMES = 3
//
//        final World world
//        final SmallFloatingBlock nonResourcePackFloatingBlock
//
//        int currentModel
//        int finalModel
//        boolean hasTouchedGround = false
//        int ticksExploding = 0
//
//        LootBoxEntity(World world, SmallFloatingBlock smallFloatingBlock, SmallFloatingBlock nonResourcePackFloatingBlock, int startModel, double groundLevel) {
//            super(smallFloatingBlock, groundLevel)
//
//            this.world = world
//            this.nonResourcePackFloatingBlock = nonResourcePackFloatingBlock
//            this.currentModel = startModel
//            this.finalModel = startModel + MODEL_FRAMES
//        }
//
//        void incrementModel() {
//            currentModel++
//            ((SmallFloatingBlock) floatingEntity).updateItemStack(FastItemUtils.withModelId(Material.POPPY, currentModel))
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
//    static class CupidNpc {
//
//        NPCTracker npcTracker
//
//        CupidNpc(NPCTracker npcTracker) {
//            this.npcTracker = npcTracker
//        }
//
//        void remove() {
//            if (npcTracker != null) {
//                NPCRegistry.get().unregister(npcTracker)
//            }
//        }
//
//    }
//
//}
