package scripts.factions.features.pets.pets

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.starcade.starlight.helper.Schedulers
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.pets.ItemPets
import scripts.factions.features.pets.struct.ItemPet

class CageItemPet extends ItemPet {

    Set<Cage> cages = []

    CageItemPet() {
        super("cage",
                "§<#139AFB>§lCage",
                [
                        "cage pet description 1",
                        "cage pet description 2",
                ],
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGJlNjMyNjc0NmQ3MWQzNTU5MzMxNjFlZDA3NjVkYWRlM2JjYzdiNDdkODRmN2JlN2ExMjk1NzNlMjI3NGViIn19fQ====",
                25.0D,
                25D
        )

        if (!getConfig().hasEntry("defaultRadius"))
        {
            getConfig().addDefault([
                    new DoubleEntry("defaultRadius", 2.0D),
                    new DoubleEntry("radiusPerLevel", .25D)
            ])

            ItemPets.petsConfig.queueSave()
        }

        Schedulers.async().execute {
            cages.each {
                it.destroy()
            }
        }


    }

    @Override
    void onInteract(Player player, double petLevel, PlayerInteractEvent event) {
        def radius = getDefaultRadius() + (getRadiusPerLevel() * petLevel)
        def targets = player.getLocation().getNearbyPlayers(radius, radius, radius)

        incrementLevel(event.getItem(), getDefaultXpIncrease() - ((petLevel - 1) * getXpDecreasePerLevel()))

        targets.remove(player)

        if (targets.isEmpty()) {
            player.sendMessage("§c§lCAGE- No targets found")
            return
        }

        Player closest = null
        targets.each {
            if (closest == null) {
                closest = it
            } else if (it.getLocation().distance(player.getLocation()) < closest.getLocation().distance(player.getLocation())) {
                closest = it
            }
        }

        player.sendMessage("§c§lCAGE- Closest target: ${closest.getName()}")

        boolean cd = !player.isOp()

        if (cd) {
            setCooldown(event.getItem())
        }
    }

    double getDefaultRadius() {
        return getConfig().getDoubleEntry("defaultRadius").value
    }

    double getRadiusPerLevel() {
        return getConfig().getDoubleEntry("radiusPerLevel").value
    }

    class Cage {

        Player player
        int size

        Long endTime = -1L

        List<CageBlock> blocks = []

        Cage(Player player, int size) {
            this.player = player
            this.size = size
        }

        void build()
        {
            Location location = player.getLocation()

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    for (int z = 0; z < size; z++) {
                        Location blockLocation = location.clone().add(x, y, z)
                        CageBlock cageBlock = new CageBlock(blockLocation.getBlock().getType(), blockLocation)
                        blocks.add(cageBlock)
                        blockLocation.getBlock().setType(Material.BARRIER)
                    }
                }
            }
        }

        void destroy() {
            blocks.each {
                it.location.getBlock().setType(it.previousMaterial)
            }
        }

    }

    class CageBlock {

        Material previousMaterial
        Location location

        CageBlock(Material previousMaterial, Location location) {
            this.previousMaterial = previousMaterial
            this.location = location
        }
    }
}
