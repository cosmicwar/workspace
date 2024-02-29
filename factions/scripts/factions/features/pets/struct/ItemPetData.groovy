package scripts.factions.features.pets.struct

import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import scripts.factions.features.pets.ItemPets
import scripts.shared.utils.PersistentItemData

class ItemPetData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "ItemPetData")

    final UUID petId

    String currentAbility = "none"

    double level = 1.0D

    Long cdExpiration = -1L

    ItemPetData(UUID petId, double level) {
        this.petId = petId
        this.level = level
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static ItemPetData read(ItemStack itemStack) {
        return read(itemStack, DATA_KEY, ItemPetData.class) as ItemPetData
    }

    @BsonIgnore
    ItemPet getPet() {
        if (currentAbility == "none") return null

        return ItemPets.registeredPets.get(currentAbility)
    }
}
