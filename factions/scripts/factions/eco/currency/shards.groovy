package scripts.factions.eco.currency

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.CurrencyUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.utils.Persistent

CurrencyStorage shards = Persistent.of("shards", CurrencyUtils.register(
        "shards",
        "Shards",
        "shards",
        [ "shard", "shards" ],
        false,
        true,
        true,
        "§] §>",
        { balance -> return "§e${NumberUtils.format(balance)} §fshard${ balance.abs() == BigDecimal.ONE ? "" : "s" }" },
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
        }
)).get() as CurrencyStorage

Exports.ptr("shards", shards)

Commands.create().assertPlayer().handler { command ->
    command.sender().performCommand("shardpay ${StringUtils.asString(new ArrayList<>(command.args()))}")
}.register("spay")