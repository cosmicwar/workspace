package scripts.factions.features.duel.arena

import org.bukkit.Material
import scripts.factions.data.obj.Position

enum ArenaType {
    NETHER("nether", "§cNether", "nether_arena", Material.NETHERRACK),
    PLAINS("plains", "§aPlains", "plains_arena", Material.GRASS_BLOCK),
    BEACH("beach", "§eBeach", "beach_arena", Material.BUCKET),
    RURAL("rural", "§6Rural", "rural_arena", Material.COBWEB),
    RED_ORANGE("red_orange", "§6Red Orange", "red_orange_arena", Material.ORANGE_TERRACOTTA),
    END("end", "§5End", "end_arena", Material.END_STONE),
    INDUSTRIAL("industrial", "§8Industrial", "industrial_arena", Material.PISTON),
    LOTUS("lotus", "§dLotus", "lotus_arena", Material.LILY_OF_THE_VALLEY),
    DICE("dice", "§fDice", "dice_arena", Material.WHITE_WOOL),
    ICE("ice", "§bIce", "ice_arena", Material.PACKED_ICE)

    String internalName, displayName, schemName

    Material material

    ArenaType(String internalName, String displayName, String schemName, Material material = Material.NETHERITE_SWORD) {
        this.internalName = internalName
        this.displayName = displayName
        this.schemName = schemName
        this.material = material
    }
}