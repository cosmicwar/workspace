package scripts.exec.nova1

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import scripts.factions.cfg.WorldConfig
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.legacy.wrappers.Console

import java.util.concurrent.ThreadLocalRandom

def world = Bukkit.getWorld(WorldConfig.SPAWN_WORLD_PREFIX)

List<Map<String, Object>> crates = [
        [
                id          : "vote",
                model       : 1,
//                particle: Particle.toNMS(Particle.REDSTONE, new Particle.DustOptions(Color.LIME, 1)),
                color       : "§a",
                title       : "§a§lVoting Crate",
                titleOffsetX: 0.5D,
                titleOffsetZ: 0.5D,
                location    : new Location(world, -16, 180, 42, -170, 69),
        ],
        [
                id          : "skin",
                model       : 2,
//                particle: CraftParticle.toNMS(Particle.REDSTONE, new Particle.DustOptions(Color.AQUA, 1)),
                color       : "§b",
                title       : "§b§lSkin Crate",
                titleOffsetX: 0.5D,
                titleOffsetZ: 0.5D,
                location    : new Location(world, -23, 180, 35, -100, 69),
        ],
        [
                id          : "solar",
                model       : 1,
//                particle: CraftParticle.toNMS(Particle.REDSTONE, new Particle.DustOptions(Color.FUCHSIA, 1)),
                color       : "§6",
                title       : "§6§lSolar Crate",
                titleOffsetX: 0.5D,
                titleOffsetZ: 0.5D,
                location    : new Location(world, -20, 180, 39, -135, 69),
        ],
        [
                id          : "astral",
                model       : 6,
//                particle: CraftParticle.toNMS(Particle.REDSTONE, new Particle.DustOptions(Color.WHITE, 1)),
                color       : "§3",
                title       : "§3§lAstral Crate",
                titleOffsetX: 0.5D,
                titleOffsetZ: 0.5D,//-2 180 42
                location    : new Location(world, -2, 180, 42, 170, 69),
        ],
        [
                id          : "galactic",
                model       : 8,
//                particle: CraftParticle.toNMS(Particle.REDSTONE, new Particle.DustOptions(Color.ORANGE, 1)),
                color       : "§e",
                title       : "§e§lGalactic Crate",
                titleOffsetX: 0.5D,
                titleOffsetZ: 0.5D,//5 180 35
                location    : new Location(world, 5, 180, 35, 100, 69),
        ],
        [
                id           : "monthly",
                model        : 3,
//                particle: CraftParticle.toNMS(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1)),
                color        : "§c",
                title        : "§c§lMonthly Crate",
                titleOffsetX : 0.5D,
                titleOffsetZ : 0.5D,
                location     : new Location(world, 2, 180, 39, 135, 69),
                final_rewards: [
                        [
                                title   : "§a§ltest",
                                icon    : Material.END_CRYSTAL,
                                chance  : 100,
                                commands: [
                                        "rbc &6&lCRATES&8&L » &d{name} &fhas won &4&L5x MASSIVE CRYSTAL POUCHES&f from a Monthly Crate!"
                                ]
                        ],
                ]

        ]
]

Exports.ptr("crates", crates)
crates.each {
    String crateKey = it.get("id")
    (Exports.ptr("statistics:register") as Closure<Void>)?.call("crate-${crateKey}", "${it.get("color")}${StringUtils.capitalize(crateKey)} crates Opened")
}

Commands.create().assertPermission("commands.gkitrandomkey").assertUsage("<player>").handler { command ->
    String crate = getRandom([
            monthly : 1,
            skin    : 2,
            galactic: 11,
            astral  : 28,
            solar   : 60,
            vote    : 80
    ])
    Console.dispatchCommand("givecratekey ${command.rawArg(0)} ${crate} 1")
}.register("gkitrandomkey")

// Needed monthly crate chance to be under 1%, old random system doesn't allow that since its capped at 100%
static getRandom(Map<String, Integer> entries) {
    int size = 0
    for (def entry : entries) {
        size += entry.value
    }

    List<String> keys = new ArrayList<>(size)

    for (def entry : entries.entrySet()) {
        for (int i = 0; i < entry.getValue(); ++i) {
            keys.add(entry.getKey())
        }
    }

    return keys.get(ThreadLocalRandom.current().nextInt(keys.size()))
}
