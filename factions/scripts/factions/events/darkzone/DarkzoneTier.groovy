package scripts.factions.events.darkzone

import org.bukkit.Material
import scripts.factions.content.mobs.impl.CaveSpiderMob
import scripts.factions.content.mobs.impl.EndermanMob
import scripts.factions.content.mobs.impl.ZombieMob

enum DarkzoneTier
{
    TIER_1("§7(Tier §a§n1§7)", "tier1", 1.0F, 8, Material.END_PORTAL_FRAME, [CaveSpiderMob]),
    TIER_2("§7(Tier §e§n2§7)", "tier2", 2.0F, 7, Material.END_PORTAL_FRAME, [CaveSpiderMob]),
    TIER_3("§7(Tier §c§n3§7)", "tier3", 3.0F, 6, Material.END_PORTAL_FRAME, [CaveSpiderMob]),
    TIER_4("§7(Tier §5§n4§7)", "tier4", 4.0F, 5, Material.END_PORTAL_FRAME, [CaveSpiderMob]),
    TIER_5("§7(Tier §6§n5§7)", "tier5", 5.0F, 4, Material.END_PORTAL_FRAME, [EndermanMob, ZombieMob])

    String displayName
    String internalName

    float scale
    int maxSpawnCount

    Material spawnerMaterial

    List<Class> spawnableTypes

    DarkzoneTier(String displayName, String internalName, float scale = 1.0F, int maxSpawnCount = 7, Material spawnerMaterial = Material.END_PORTAL_FRAME, List<Class> spawnableTypes = [])
    {
        this.displayName = displayName
        this.internalName = internalName
        this.scale = scale
        this.maxSpawnCount = maxSpawnCount
        this.spawnerMaterial = spawnerMaterial
        this.spawnableTypes = spawnableTypes
    }

}