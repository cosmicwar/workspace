package scripts.factions.features.pve.entities.boss

import scripts.factions.features.pve.PveMobDifficulty
import scripts.factions.features.pve.entities.boss.bosses.EstrangedWitchBoss
import scripts.factions.features.pve.entities.boss.bosses.ExplodeyCreeperBoss
import scripts.factions.features.pve.entities.boss.bosses.IronEnforcerBoss

enum BossType {

    ESTRANGED_WITCH(EstrangedWitchBoss.class, "§5§lEstranged Witch"),
    EXPLODEY_CREEPER(ExplodeyCreeperBoss.class, "§a§lExplodey Creeper"),
    IRON_ENFORCER(IronEnforcerBoss.class, "§f§lIron Enforcer"),
//    OXAR_THE_WIZARD(OxarTheWizard.class, "§7§lOxar The Wizard"),
    //SPOOKY_WITHERSKELETON(SpookyWitherSkeletonBoss.class, "§c§l[Boss] §a§lSpooky Wither Skeleton", "§7§l[§4§lENRAGED§7§l] §a§lSpooky Wither Skeleton", EntityTypes.WITHER_SKELETON)

    final Class<? extends PveBoss> clazz
    final String displayName

    BossType(Class<? extends PveBoss> clazz, String displayName) {
        this.clazz = clazz
        this.displayName = displayName
    }

    String getSimpleDisplayName() {
        return displayName
    }

    String getDisplayName(PveMobDifficulty difficulty = PveMobDifficulty.BOSS) {
        return difficulty == PveMobDifficulty.ENRAGED_BOSS ? "§7§l[§4§lENRAGED§7§l] ${getSimpleDisplayName()}" :  "§c§l[Boss] ${getSimpleDisplayName()}"
    }

    static BossType parse(String arg) {
        return values().find {it.name().equalsIgnoreCase(arg)}
    }

}

