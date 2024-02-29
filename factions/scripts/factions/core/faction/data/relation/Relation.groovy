package scripts.factions.core.faction.data.relation

class Relation {

    UUID targetFactionId
    UUID initiatorId

    RelationType type = RelationType.NEUTRAL

    boolean bothSided = false

    Relation() {
    }

    Relation(UUID targetFactionId, UUID initiatorId, RelationType type) {
        this.targetFactionId = targetFactionId
        this.initiatorId = initiatorId
        this.type = type
    }

}

