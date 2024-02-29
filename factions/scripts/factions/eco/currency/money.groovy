package scripts.factions.eco.currency


import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.bukkit.Bukkit
import org.bukkit.Location
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.CurrencyUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.utils.Persistent

Map<Integer, Location> topLocations = [
        1: new Location(null, -1073.5, 63, -253.5, 90, 0),
        2: new Location(null, -1074.5, 62, -257.5, 90, 0),
        3: new Location(null, -1074.5, 62, -249.5, 90, 0),
        4: new Location(null, -1075.5, 62, -261.5, 90, 0),
        5: new Location(null, -1075.5, 62, -245.5, 90, 0)
]

CurrencyStorage money = Persistent.of("money", CurrencyUtils.register(
        "money",
        "Money",
        "money",
        [ "money" ],
        true,
        true,
        false,
        "§] §>",
        { balance -> return "§a\$${NumberUtils.format(balance)}" },
        { top ->
//            int amount = top.size() < 5 ? top.size() : 5
//
//            for (int i = 0; i < amount; ++i) {
//                int position = i + 1
//
//                DatabaseUtils.getLatestUsername(top.get(i).getKey(), { username ->
//                    Schedulers.sync().run {
//                        for (int j = 0; j < WorldConfig.SPAWN_WORLDS; ++j) {
//                            World world = Bukkit.getWorld("${WorldConfig.SPAWN_WORLD_PREFIX}${j}")
//                            Location location = topLocations.get(position).clone()
//                            location.setWorld(world)
//
//                            NPCRegistry.get().spawn("${j}_money_top_${position}", "§6§l#${position} §f${username}", location, username, { player ->
//                                player.performCommand("bal ${username}")
//                            })
//                        }
//                    }
//                })
//            }
        },
        BigDecimal.valueOf(50000.0)
)).get() as CurrencyStorage

Exports.ptr("money", money)

Commands.create().handler { command ->
    Bukkit.dispatchCommand(command.sender(), command.args().size() == 0 ? "bal" : "bal ${StringUtils.asString(new ArrayList<>(command.args()))}")
}.register("balance", "ebal", "ebalance", "emoney")

Commands.create().handler { command ->
    Bukkit.dispatchCommand(command.sender(), command.args().size() == 0 ? "moneytop" : "moneytop ${StringUtils.asString(new ArrayList<>(command.args()))}")
}.register("baltop", "balancetop", "ebaltop", "ebalancetop")