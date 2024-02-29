package scripts.factions.features.spawners

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.World
import org.bukkit.entity.EntityType
import scripts.factions.data.DataObject
import scripts.shared.legacy.objects.Position
import scripts.shared.legacy.utils.RandomUtils

@CompileStatic(TypeCheckingMode.SKIP)
class CSpawner extends DataObject {

    String spawnerId

    String world = ""
    Position position = new Position()
    EntityType spawnerType = null

    int spawnerStackSize = 1

    int minSpawnerDelay = 100
    int maxSpawnerDelay = 600

    CSpawner() {}

    CSpawner(String spawnerId) {
        this.spawnerId = spawnerId
    }

    CSpawner(String spawnerId, EntityType spawnerType) {
        this.spawnerId = spawnerId
        this.spawnerType = spawnerType
        this.spawnerStackSize = 1
    }

    CSpawner(String spawnerId, double x, double y, double z, World world, EntityType spawnerType) {
        this.spawnerId = spawnerId
        this.spawnerType = spawnerType
        this.position.x = x
        this.position.y = y
        this.position.z = z

        this.world = world.getName()
    }

    CSpawner(double x, double y, double z, World world, EntityType spawnerType) {
        this("$x:$y:$z:$world.name", x, y, z, world, spawnerType)
    }

    @BsonIgnore
    void update() {
        this.minSpawnerDelay = minDurationAlgo()
        this.maxSpawnerDelay = maxDurationAlgo()
    }

    /**
     * Spawner Count Algorithm
     * @param spawner
     * @return spawn count based on stack size
     */
    @BsonIgnore
    int spawnCountAlgo() {
        def count = getSpawnerStackSize()

        if (count == 1) return 1

        if (count < 10) {
            return Math.max(2, Math.ceil(RandomUtils.RANDOM.nextDouble(Math.ceil(count / 2D))))
        }

        if (count < 50) {
            return Math.max(10, Math.ceil(RandomUtils.RANDOM.nextDouble(Math.ceil(count / 2.22D))))
        }

        if (count < 100) {
            return Math.max(15, Math.ceil(RandomUtils.RANDOM.nextDouble(Math.ceil(count / 2.15D))))
        }

        return Math.max(30, Math.ceil(RandomUtils.RANDOM.nextDouble(Math.ceil(count / 2.1D))))
    }

    @BsonIgnore
    int minDurationAlgo() {
        def count = getSpawnerStackSize()

        if (count == 1) return 100

        if (count < 10) {
            75
        }

        if (count < 50) {
            50
        }

        if (count < 100) {
            35
        }

        return 20
    }

    @BsonIgnore
    int maxDurationAlgo() {
        def count = getSpawnerStackSize()

        if (count == 1) return 600

        if (count < 10) {
            500
        }

        if (count < 50) {
            400
        }

        if (count < 100) {
            300
        }

        return 200
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return false
    }
}
