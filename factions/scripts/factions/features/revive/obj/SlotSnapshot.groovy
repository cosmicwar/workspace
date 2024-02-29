package scripts.factions.features.revive.obj

class SlotSnapshot {

    String item
    Integer slot = null

    SlotSnapshot() {}

    SlotSnapshot(String item, Integer slot) {
        this.item = item
        this.slot = slot
    }

}
