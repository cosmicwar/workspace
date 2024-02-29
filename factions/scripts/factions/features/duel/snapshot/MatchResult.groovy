package scripts.factions.features.duel.snapshot

enum MatchResult {
    WIN("Win"),
    LOSE("Lose"),
    TIE("Tie")

    String displayName

    MatchResult(String displayName) {
        this.displayName = displayName
    }
}
