package scripts.factions.core.profile.grant

class Rank {

    String internalName

    String prefix = "", suffix = ""
    String nameColor = "§f", chatColor = "§f"

    Set<String> permissions = new HashSet<String>()

    Rank() {}

    Rank(String internalName,
         String prefix = "",
         String suffix = "",
         String nameColor = "§f",
         String chatColor = "§f") {
        this.internalName = internalName
        this.prefix = prefix
        this.suffix = suffix
        this.nameColor = nameColor
        this.chatColor = chatColor
    }

}
