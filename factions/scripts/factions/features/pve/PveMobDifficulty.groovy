package scripts.factions.features.pve

enum PveMobDifficulty {

    ENRAGED_BOSS,
    BOSS,
    HARD,
    MEDIUM,
    EASY

    boolean isBoss() {
        return this == ENRAGED_BOSS || this == BOSS
    }

}