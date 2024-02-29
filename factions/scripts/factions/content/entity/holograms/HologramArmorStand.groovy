package scripts.factions.content.entity.holograms

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.Level

class HologramArmorStand extends ArmorStand {

    HologramArmorStand(EntityType<? extends ArmorStand> type, Level world) {
        super(type, world)
    }

    HologramArmorStand(Level world, double x, double y, double z) {
        super(world, x, y, z)
    }

    @Override
    void onSyncedDataUpdated(EntityDataAccessor<?> data) {

    }

}
