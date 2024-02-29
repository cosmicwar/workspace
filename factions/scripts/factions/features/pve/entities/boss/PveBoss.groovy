package scripts.factions.features.pve.entities.boss

import org.bukkit.ChatColor
import org.bukkit.Color
import scripts.factions.features.pve.PveMobDifficulty

interface PveBoss {

    BossType getBossType()

    Map<UUID, Float> getDamageTracker()

    Color getParticleColor()

    ChatColor getGlowColor()

    boolean isEnraged()

    PveMobDifficulty getDifficulty()
}
