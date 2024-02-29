package scripts.factions.core.meteor.mine

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.shared.legacy.objects.Region

@CompileStatic(TypeCheckingMode.SKIP)
class Mine {

    String name
    String world
    int resetPercentage
    Region region
    Material block

    Mine(String name, String world, int resetPercentage, Region region, Material block) {
        this.name = name
        this.world = world
        this.resetPercentage = resetPercentage
        this.region = region
        this.block = block
    }

}