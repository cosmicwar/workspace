package scripts.factions.eco.lootboxes.animation
//package scripts.factions.lootboxes.animation
//
//import groovy.transform.CompileStatic
//import net.minecraft.server.v1_16_R3.*
//import org.bukkit.*
//import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
//import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
//import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
//import org.bukkit.entity.Player
//import org.bukkit.inventory.ItemStack
//import org.bukkit.inventory.meta.FireworkMeta
//import org.bukkit.util.Vector
//import scripts.shared.features.lootbox.data.LootBox
//import scripts.shared.features.lootbox.data.LootBoxReward
//import scripts.shared.legacy.utils.FastItemUtils
//import scripts.shared.legacy.utils.PacketUtils
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
//class NewYearOpeningAnimation extends LootboxOpeningAnimation {
//
//    static final double REWARD_ANGLE_MIN = -45D
//    static final double REWARD_ANGLE_MAX = 45D
//
//    Location location
//    double groundLevel
//    LootBoxEntity lootBoxEntity
//
//    List<LootBoxReward> rewardsCopy = new ArrayList<>()
//    List<Double> rewardEntityShootAngles = new ArrayList<>()
//    List<RewardEntity> rewardEntities = new ArrayList<>()
//    List<Firework> fireworks = new ArrayList<>()
//
//    NewYearOpeningAnimation() {
//
//    }
//
//    NewYearOpeningAnimation(Player player, World world, LootBox lootBox, ItemStack itemStack) {
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
//        location.setYaw((location.getYaw() - 180F) % 360F as float)
//        location.setPitch(0F)
//
//        SmallFloatingBlock resourcePackFloatingBlock = new SmallFloatingBlock(world, location, FastItemUtils.withModelId(Material.CYAN_SHULKER_BOX, LootBoxEntity.START_MODEL), false)
//        resourcePackFloatingBlock.locationOffset = new Vector(0D, 0D, 0D)
////        resourcePackFloatingBlock.setGlowColor(PRESENT_COLORS.getOrDefault(startModel, ChatColor.RED))
//        resourcePackFloatingBlock.visibilityPredicate = { Player player -> return player.hasResourcePack() } as Predicate<Player>
//        resourcePackFloatingBlock.track()
//
//        SmallFloatingBlock nonResourcePackFloatingBlock = new SmallFloatingBlock(world, location, Material.CYAN_SHULKER_BOX, false)
////        nonResourcePackFloatingBlock.setGlowColor(PRESENT_COLORS.getOrDefault(startModel, ChatColor.RED))
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
//                spawnFireworks(lootBoxEntity.getLocation(), 5)
//            }
//
//            if (lootBoxEntity.hasTouchedGround) {
//                if (lootBoxEntity.getCurrentModel() < LootBoxEntity.END_MODEL) {
//                    lootBoxEntity.incrementModel()
//
//                    if (lootBoxEntity.getCurrentModel() == LootBoxEntity.END_MODEL) {
//                        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 10F, 1F)
//                    }
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
//        if (tick == durationTicks - 20) {
//            spawnFireworks(player.getLocation(), 10)
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
//        }
//
//        trySpawnRewardEntity(tick)
//        tickRewardEntities()
//
//        long now = System.currentTimeMillis()
//        fireworks.removeIf {
//            if (now > it.removeAt) {
//                it.remove()
//                return true
//            }
//
//            return false
//        }
//    }
//
//    void spawnFireworks(Location origin, int count) {
//        count.times {
//            Location spawnLocation = origin.clone()
//            spawnLocation.add(lootBoxEntity.getLocation().clone().subtract(player.getLocation()).toVector().rotateAroundY(Math.toRadians(ThreadLocalRandom.current().nextDouble(-20D, 20D)))).add(0D, 0.5D, 0D)
//
//            ItemStack fireworkItem = new ItemStack(Material.FIREWORK_ROCKET)
//            FireworkMeta meta = fireworkItem.getItemMeta() as FireworkMeta
//            FireworkEffect.Builder builder = FireworkEffect.builder()
//            builder.with(FireworkEffect.Type.BALL).withColor([
//                    Color.fromRGB(ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255), ThreadLocalRandom.current().nextInt(255)),
//            ])
//            meta.addEffect(builder.build())
//            meta.setPower(1)
//            fireworkItem.setItemMeta(meta)
//
//            spawnFirework(spawnLocation, fireworkItem, ThreadLocalRandom.current().nextLong(250L, 1000L))
//        }
//    }
//
//    void spawnFirework(Location location, ItemStack fireworkItem, long ttl) {
//        EntityFireworks fireworksEntity = new EntityFireworks(
//                (location.getWorld() as CraftWorld).getHandle(),
//                null,
//                location.getX(),
//                location.getY(),
//                location.getZ(),
//                CraftItemStack.asNMSCopy(fireworkItem)
//        )
//
//        PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(fireworksEntity)
//        PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(fireworksEntity.getId(), fireworksEntity.getDataWatcher(), true)
//        List<EntityPlayer> viewers = location.getNearbyEntitiesByType(Player.class, 16D).collect {(it as CraftPlayer).handle}
//        viewers.each {
//            it.playerConnection.sendPacket(packetPlayOutSpawnEntity)
//            it.playerConnection.sendPacket(packetPlayOutEntityMetadata)
//        }
//
//        PacketPlayOutEntityStatus packetPlayOutEntityStatus = new PacketPlayOutEntityStatus(fireworksEntity, (byte) 17)
//        PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(fireworksEntity.getId())
//        fireworks.add(new Firework(viewers.findResults { it.uniqueID} as Set<UUID>, [packetPlayOutEntityStatus, packetPlayOutEntityDestroy], System.currentTimeMillis() + ttl))
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
//        fireworks.each { it.remove() }
//    }
//
//    static class LootBoxEntity extends WeightedEntity {
//
//        static final int START_MODEL = 1
//        static final int END_MODEL = 26
//
//        final World world
//        final SmallFloatingBlock nonResourcePackFloatingBlock
//
//        int currentModel
//        boolean hasTouchedGround = false
//        int ticksExploding = 0
//
//        LootBoxEntity(World world, SmallFloatingBlock smallFloatingBlock, SmallFloatingBlock nonResourcePackFloatingBlock, double groundLevel) {
//            super(smallFloatingBlock, groundLevel)
//
//            this.world = world
//            this.nonResourcePackFloatingBlock = nonResourcePackFloatingBlock
//            this.currentModel = START_MODEL
//        }
//
//        void incrementModel() {
//            currentModel++
//            ((SmallFloatingBlock)floatingEntity).updateItemStack(FastItemUtils.withModelId(Material.CYAN_SHULKER_BOX, currentModel))
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
//    static class Firework {
//
//        Set<UUID> viewers
//        List<Packet> removePackets
//        long removeAt
//
//        Firework(Set<UUID> viewers, List<Packet> removePackets, long removeAt) {
//            this.viewers = viewers
//            this.removePackets = removePackets
//            this.removeAt = removeAt
//        }
//
//        void remove() {
//            viewers.findResults { Bukkit.getPlayer(it) }.each { player ->
//                removePackets.each { packet ->
//                    PacketUtils.send(player, packet)
//                }
//            }
//        }
//
//    }
//
//}
