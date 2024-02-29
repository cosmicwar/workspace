package scripts.factions.eco.lootboxes.animation
//package scripts.factions.lootboxes.animation
//
//import groovy.transform.CompileStatic
//import net.minecraft.server.v1_16_R3.*
//import org.apache.commons.lang3.reflect.FieldUtils
//import org.bukkit.*
//import org.bukkit.block.Block
//import org.bukkit.block.BlockFace
//import org.bukkit.craftbukkit.v1_16_R3.CraftChunk
//import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
//import org.bukkit.entity.Player
//import org.bukkit.inventory.ItemStack
//import org.bukkit.util.Vector
//import scripts.shared.features.lootbox.data.LootBox
//import scripts.shared.features.lootbox.data.LootBoxReward
//import scripts.shared.legacy.utils.*
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
//class ChristmasOpeningAnimation extends LootboxOpeningAnimation {
//
//    static final double REWARD_ANGLE_MIN = -45D
//    static final double REWARD_ANGLE_MAX = 45D
//    static final Map<Integer, ChatColor> PRESENT_COLORS = [
//            1: ChatColor.RED,
//            21: ChatColor.DARK_PURPLE,
//            41: ChatColor.DARK_BLUE,
//            61: ChatColor.DARK_GREEN,
//    ]
//
//    Location location
//    double groundLevel
//
//    LootBoxEntity lootBoxEntity
//    SantaNpc santaNpc
//
//    List<LootBoxReward> rewardsCopy = new ArrayList<>()
//    List<Double> rewardEntityShootAngles = new ArrayList<>()
//    List<RewardEntity> rewardEntities = new ArrayList<>()
//    Set<Long> dirtyChunks = new HashSet<>()
//    List<FloatingEntity> snowGolems = new ArrayList<>()
//    List<SnowballEntity> snowballs = new ArrayList<>()
//    List<FloatingEntity> clouds = new ArrayList<>()
//
//    ChristmasOpeningAnimation() {
//
//    }
//
//    ChristmasOpeningAnimation(Player player, World world, LootBox lootBox, ItemStack itemStack) {
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
//        if (player.hasResourcePack()) {
//            player.playSound(location, "scp.bells", 1F, 1F)
//        } else {
//            player.playSound(location, Sound.BLOCK_DISPENSER_LAUNCH, 1F, 1F)
//        }
//
//        for (int x = -1; x <= 1; x++) {
//            for (int z = -1; z <= 1; z++) {
//                dirtyChunks.add(LongHash.toLong((location.getBlockX() >> 4) + x, (location.getBlockZ() >> 4) + z))
//            }
//        }
//
//        world.execute {
//            if (finished.get()) return
//
//            dirtyChunks.each {
//                int x = LongHash.msw(it)
//                int z = LongHash.lsw(it)
//
//                Chunk chunk = world.getChunkAt(x, z)
//                PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk((chunk as CraftChunk).getHandle(), 65535)
//                int[] biomes = FieldUtils.readDeclaredField(packetPlayOutMapChunk, "e", true) as int[]
//                for (int i = 0; i < biomes.length; i++) {
//                    biomes[i] = 12
//                }
//                PacketUtils.send(player, packetPlayOutMapChunk)
//            }
//
//            player.setPlayerWeather(WeatherType.DOWNFALL)
//        }
//
//        location.setY(location.getY() - 1D)
//        location.setYaw((location.getYaw() - 90F) % 360F as float)
//        location.setPitch(0F)
//
//        int startModel = itemStack == null ? 1 : itemStack.getItemMeta().getCustomModelData()
//        SmallFloatingBlock resourcePackFloatingBlock = new SmallFloatingBlock(world, location, FastItemUtils.withModelId(Material.RED_SHULKER_BOX, startModel), false)
//        resourcePackFloatingBlock.locationOffset = new Vector(0D, -1.3D, 0D)
//        resourcePackFloatingBlock.setGlowColor(PRESENT_COLORS.getOrDefault(startModel, ChatColor.RED))
//        resourcePackFloatingBlock.visibilityPredicate = { Player player -> return player.hasResourcePack() } as Predicate<Player>
//        resourcePackFloatingBlock.track()
//
//        SmallFloatingBlock nonResourcePackFloatingBlock = new SmallFloatingBlock(world, location, Material.BROWN_SHULKER_BOX, false)
//        nonResourcePackFloatingBlock.setGlowColor(PRESENT_COLORS.getOrDefault(startModel, ChatColor.RED))
//        nonResourcePackFloatingBlock.visibilityPredicate = { Player player -> return !player.hasResourcePack() } as Predicate<Player>
//        nonResourcePackFloatingBlock.track()
//
//        lootBoxEntity = new LootBoxEntity(world, resourcePackFloatingBlock, nonResourcePackFloatingBlock, startModel, groundLevel)
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
//                Location location = lootBoxEntity.getLocation()
//                world.execute {
//                    if (finished.get()) return
//
//                    Set<BlockPosition> snowBlockLocations = new HashSet<>()
//                    snowBlockLocations.addAll(FastRegionUtils.ellipsoid(location, 1.5D, 0D, 1.5D, true))
//                    10.times {
//                        snowBlockLocations.add(new BlockPosition(location.getBlockX() + ThreadLocalRandom.current().nextInt(-4, 4), groundLevel, location.getBlockZ() + ThreadLocalRandom.current().nextInt(-4, 4)))
//                    }
//
//                    snowBlockLocations.each {
//                        Block block = world.getBlockAt(it.getX(), it.getY(), it.getZ())
//                        if (block.isEmpty() && !block.getRelative(BlockFace.DOWN).isEmpty()) {
//                            Location blockLocation = block.getLocation()
//                            player.sendBlockChange(blockLocation, Material.SNOW.createBlockData("[layers=${blockLocation.distance(location) < 2D ? "2" : "1"}]"))
//                        }
//                    }
//                }
//
////                spawnClouds()
//                spawnSanta()
//                spawnSnowGolems()
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
//        tickGolems()
//    }
//
//    void spawnSanta() {
//        Location spawnLocation = player.getLocation()
//        spawnLocation.add(lootBoxEntity.getLocation().clone().subtract(player.getLocation()).toVector().rotateAroundY(Math.toRadians(20D))).add(0D, 0.5D, 0D)
//
//        Vector direction = player.getLocation().subtract(spawnLocation).toVector().normalize()
//        spawnLocation.setYaw(-30F + Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())) as float)
//        spawnLocation.setPitch( Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getZ(), 2)) as double)) as float)
//
//        NPCTracker npcTracker = NPCRegistry.get().spawn("christmaslootbox_${player.getUniqueId().toString()}", "§c§lM§f§le§c§lr§f§lr§c§ly §f§lC§c§lh§f§lr§c§li§f§ls§c§lt§f§lm§c§la§f§ls§c§l!", spawnLocation, "0ed4f2be-5d0a-4498-a302-07c280c17de2")
//        npcTracker.setHand(FastItemUtils.withModelId(Material.RED_SHULKER_BOX, RandomUtils.getRandom(PRESENT_COLORS.keySet() as List<Integer>)))
//        npcTracker.setSitting(true)
//        npcTracker.turnTowardPlayers = true
//
//        SmallFloatingBlock resourcePackSleigh = new SmallFloatingBlock(world, spawnLocation.clone().add(0D, 1D, 0D), FastItemUtils.withModelId(Material.FEATHER, 1), false)
//        resourcePackSleigh.visibilityPredicate = { Player player -> player.hasResourcePack() }
//        resourcePackSleigh.track()
//
//        FloatingEntity nonResourcePackSleigh = new FloatingEntity(world, spawnLocation, {
//            EntityMinecartRideable entityMinecartRideable = new EntityMinecartRideable(EntityTypes.MINECART, (world as CraftWorld).getHandle())
//            entityMinecartRideable.setYawPitch(100F, 0F)
//            return entityMinecartRideable
//        })
//        nonResourcePackSleigh.locationOffset = new Vector(0D, 0D, 1.4D)
//        nonResourcePackSleigh.visibilityPredicate = { Player player -> !player.hasResourcePack() }
//        nonResourcePackSleigh.track()
//
//        santaNpc = new SantaNpc(npcTracker, resourcePackSleigh, nonResourcePackSleigh)
//    }
//
//    void spawnSnowGolems() {
//        [-15D/*, 15D*/].each { angle ->
//            Location spawnLocation = player.getLocation()
//            spawnLocation.add(lootBoxEntity.getLocation().clone().subtract(player.getLocation()).toVector().rotateAroundY(Math.toRadians(angle)))
//
//            Vector direction = player.getLocation().subtract(spawnLocation).toVector().normalize()
//            spawnLocation.setYaw(Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())) as float)
//            spawnLocation.setPitch( Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getZ(), 2)) as double)) as float)
//
//            FloatingEntity snowGolem = new FloatingEntity(world, spawnLocation, {
//                EntitySnowman entitySnowman = new EntitySnowman(EntityTypes.SNOW_GOLEM, (world as CraftWorld).getHandle())
//                return entitySnowman
//            })
//
//            snowGolems.add(snowGolem)
//            snowGolem.track()
//
//            for (double y = 0D; y < 1.8D; y += 0.2D) {
//                world.spawnParticle(Particle.BLOCK_DUST, spawnLocation.clone().add(0D, y, 0D), 20, 0D, 0D, 0D, 0.15D, Material.SNOW_BLOCK.createBlockData(), true)
//            }
//        }
//    }
//
//    void tickGolems() {
//        snowGolems.each {
//            Location location = it.getCurrentLocation()
//            Vector direction = player.getLocation().subtract(location).toVector().normalize()
//            it.setYawPitch(
//                    Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())) as float,
//                    Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getZ(), 2)) as double)) as float
//            )
//
//            if (ThreadLocalRandom.current().nextDouble() < 0.05D) {
//                FloatingEntity snowball = new FloatingEntity(world, location.clone().add(0D, 1D, 0D), {
//                    EntitySnowball entitySnowball = new EntitySnowball(EntityTypes.SNOWBALL, (world as CraftWorld).getHandle())
//                    return entitySnowball
//                })
//                snowball.attachToArmorStand = true
//
//                SnowballEntity snowballEntity = new SnowballEntity(snowball, groundLevel, direction)
//                snowballs.add(snowballEntity)
//                snowball.track()
//            }
//        }
//
//        snowballs.removeIf({
//            it.move()
//            if (!it.isMoving()) {
//                it.remove()
//                return true
//            }
//
//            return false
//        })
//    }
//
//    void trySpawnRewardEntity(int tick) {
//        if (tick >= 60 && !rewardsCopy.isEmpty() && tick % 10 == 0) {
//            LootBoxReward lootBoxReward = rewardsCopy.remove(0)
//            ItemStack itemStack = lootBoxReward.getDisplayItem()
//            FloatingEntity floatingEntity = makeFloatingItem(world, lootBoxEntity.getLocation().add(0D, 0.5D, 0D), itemStack, lootBoxReward.glowColor, lootBoxReward.getDisplayName())
//            floatingEntity.track()
//
//            Vector vector = location.toVector().subtract(lootBoxEntity.getLocation().toVector()).normalize()
//            vector.rotateAroundY(Math.toRadians(rewardEntityShootAngles.remove(0)))
//            RewardEntity rewardEntity = new RewardEntity(floatingEntity, lootBoxReward.glowColor, groundLevel)
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
//                ChatColor glowColor = rewardEntity.glowColor
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
//                    if (rewardEntity.glowColor != null) {
//                        particles.add(new ParticleUtil.ParticleData(Particle.REDSTONE, new Particle.DustOptions(convertChatColorToColor(rewardEntity.glowColor), 1)))
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
//        world.execute {
//            if (!player.isOnline() || player.world != world) return
//
//            dirtyChunks.each {
//                int x = LongHash.msw(it)
//                int z = LongHash.lsw(it)
//
//                Chunk chunk = world.getChunkAt(x, z)
//                PacketUtils.send(player, new PacketPlayOutMapChunk((chunk as CraftChunk).getHandle(), 65535))
//            }
//        }
//
//        player.resetPlayerWeather()
//        lootBoxEntity?.remove()
//        rewardEntities.each { it.remove() }
//        snowGolems.each { it.untrack() }
//        snowballs.each { it.remove() }
//        clouds.each { it.untrack() }
//        santaNpc?.remove()
//    }
//
//    static class LootBoxEntity extends WeightedEntity {
//
//        static final int MODEL_FRAMES = 10
//
//        final World world
//
//        int currentModel
//        int finalModel
//        boolean hasTouchedGround = false
//        int ticksExploding = 0
//
//        LootBoxEntity(World world, SmallFloatingBlock resourcePackFloatingBlock, SmallFloatingBlock nonResourcePackFloatingBlock, int startModel, double groundLevel) {
//            super([resourcePackFloatingBlock, nonResourcePackFloatingBlock] as List<AbstractFloatingEntity>, resourcePackFloatingBlock.getCurrentLocation(), groundLevel)
//
//            this.world = world
//            this.currentModel = startModel
//            this.finalModel = startModel + MODEL_FRAMES
//        }
//
//        void incrementModel() {
//            currentModel++
//            floatingEntities.findAll { it instanceof SmallFloatingBlock && it.itemStack?.type == Material.RED_SHULKER_BOX }.each {
//                ((SmallFloatingBlock) it).updateItemStack(FastItemUtils.withModelId(Material.RED_SHULKER_BOX, currentModel))
//            }
//        }
//
//    }
//
//    static class RewardEntity extends WeightedEntity {
//
//        ChatColor glowColor
//
//        int ticksOnGround
//        double particleSphereRadius = 1D
//        boolean popped = false
//
//        RewardEntity(AbstractFloatingEntity floatingEntity, ChatColor glowColor, double groundLevel) {
//            super(floatingEntity, groundLevel)
//            this.glowColor = glowColor
//        }
//
//    }
//
//    static class SnowballEntity extends WeightedEntity {
//
//        SnowballEntity(AbstractFloatingEntity floatingEntity, double groundLevel, Vector velocity) {
//            super(floatingEntity, groundLevel)
//
//            this.setMotion(velocity.getX(), velocity.getY(), velocity.getZ())
//        }
//
//    }
//
//    static class SantaNpc {
//
//        NPCTracker npcTracker
//        SmallFloatingBlock resourcePackSleigh
//        FloatingEntity nonResourcePackSleigh
//
//        SantaNpc(NPCTracker npcTracker, SmallFloatingBlock resourcePackSleigh, FloatingEntity nonResourcePackSleigh) {
//            this.npcTracker = npcTracker
//            this.resourcePackSleigh = resourcePackSleigh
//            this.nonResourcePackSleigh = nonResourcePackSleigh
//        }
//
//        void remove() {
//            if (npcTracker != null) {
//                NPCRegistry.get().unregister(npcTracker)
//            }
//
//            resourcePackSleigh?.untrack()
//            nonResourcePackSleigh?.untrack()
//        }
//
//    }
//
//}
