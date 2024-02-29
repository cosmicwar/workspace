package scripts.exec.nexus

import org.starcade.starlight.Starlight

Starlight.watch(
        "scripts/_init.groovy",
        "scripts/shared/features/newtickets.groovy",
        "~/giveaways.groovy",
        "~/announcements.groovy",
        "scripts/shared/features/giveaways/Giveaways.groovy",
        "scripts/shared/features/giveaways/GiveawayTickets.groovy",
        "scripts/shared/features/notifications/Notifications.groovy"
)