package scripts.factions.core.faction.data

import groovy.transform.CompileStatic

@CompileStatic
enum Role {
    SYSTEM("Wilderness", "", "§2", 0),
    RECRUIT("Recruit", "-", 1),
    MEMBER("Member", "+", 2),
    OFFICER("Officer", "*", 3),
    COLEADER("Co-Leader", "♤", 4),
    LEADER("Leader", "♕", 5),
    ADMIN("Admin", "✪", "§4", 6)

    String name
    String prefix
    String color
    int priority

    Role(String name, String prefix = "", String color = "§7", int priority) {
        this.name = name;
        this.prefix = prefix;
        this.color = color;
        this.priority = priority;
    }

    static Role getNextRole(Role role) {
        if (role == SYSTEM) return RECRUIT
        if (role == RECRUIT) return MEMBER
        if (role == MEMBER) return OFFICER
        if (role == OFFICER) return COLEADER
        if (role == COLEADER) return LEADER
        if (role == LEADER) return LEADER
        if (role == ADMIN) return ADMIN
        return RECRUIT
    }

    static Role getPreviousRole(Role role) {
        if (role == SYSTEM) return SYSTEM
        if (role == RECRUIT) return RECRUIT
        if (role == MEMBER) return RECRUIT
        if (role == OFFICER) return MEMBER
        if (role == COLEADER) return OFFICER
        if (role == LEADER) return LEADER
        if (role == ADMIN) return ADMIN
        return RECRUIT
    }

    String getDisplayName() {
        return this.color + this.name
    }
}

