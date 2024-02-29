package scripts.factions.features.pets.struct

enum ItemPetTier
{
    SIMPLE("Simple", "&7"),
    ADVANCED("Advanced", "&e"),
    ELITE("Elite", "&6"),
    ULTIMATE("Ultimate", "&c"),
    MYTHIC("Mythic", "&d"),
    LEGENDARY("Legendary", "&5"),
    COSMIC("Cosmic", "&b"),
    CELESTIAL("Celestial", "&3"),
    DIVINE("Divine", "&a"),
    IMMORTAL("Immortal", "&2"),
    GODLY("Godly", "&9")

    String prefix
    String color

    ItemPetTier(String prefix, String color)
    {
        this.prefix = prefix
        this.color = color
    }
}