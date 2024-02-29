package scripts.factions.core.faction.data.relation

import net.minecraft.ChatFormatting

enum RelationType
{
    MEMBER("a" as char, "§a", "#22D851", "Members"),
    ALLY("d" as char, "§d", "#AF24E7", "Allies", "Allied"),
    TRUCE("b" as char, "§b", "#24DBE7", "Truces", "Truced"),
    NEUTRAL("f" as char, "§f"),
    ENEMY("c" as char, "§c", "#A41313", "Enemies", "Enemied", false)

    char rawColorCode
    String color
    String hex
    String pluralDisplayName
    String pastDisplayName
    boolean requiresBoth

    RelationType(char rawColorCode, String color, String hex = "", String pluralDisplayName = null, String pastDisplayName = null, boolean requiresBoth = true)
    {
        this.rawColorCode = rawColorCode
        this.color = color
        this.hex = hex
        this.pluralDisplayName = pluralDisplayName
        this.pastDisplayName = pastDisplayName
        this.requiresBoth = requiresBoth
    }

    boolean isAtLeast(RelationType other) {
        return this.ordinal() <= other.ordinal()
    }

    boolean isAtMost(RelationType other) {
        return this.ordinal() >= other.ordinal()
    }

    String getDisplayName() {
        return this.color + this.name().toLowerCase().capitalize()
    }

    String getPluralDisplayName() {
        if (this.pluralDisplayName != null)
            return this.color + this.pluralDisplayName

        return this.color + this.name().toLowerCase().capitalize()
    }

    String getPastDisplayName() {
        if (this.pastDisplayName != null)
            return this.color + this.pastDisplayName

        return this.color + this.name().toLowerCase().capitalize()
    }
}

