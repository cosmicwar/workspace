package scripts.factions.core.faction

import org.bukkit.Bukkit
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.command.context.PlayerContext
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared3.utils.Callback

import java.util.function.BiConsumer

class FCommandUtil {

    /*
       ~ command utils ~
    */

    static def memberFromCommand(PlayerContext cmd, Callback<Member> callback) {
        Schedulers.async().execute {
            def member = Factions.getMember(cmd.sender().getUniqueId(), true)

            if (member != null) {
                callback.exec(member)
            } else {
                cmd.reply("§! §> §cAn error occurred while loading your faction data.")
            }
        }
    }

    static def memberAsTarget(PlayerContext cmd, Callback<Member> callback) {
        Schedulers.async().execute {
            if (cmd.args().size() == 0) return

            def player = cmd.arg(0).parseOrFail(String)
            if (Bukkit.getPlayer(player) != null) {
                def member = Factions.getMember(Bukkit.getPlayer(player).getUniqueId(), true)

                if (member != null) {
                    callback.exec(member)
                } else {
                    cmd.reply("§] §> §3Player not found.")
                }
            } else {
                def member = Factions.getMember(Bukkit.getOfflinePlayer(player).getUniqueId(), true)

                if (member != null) {
                    callback.exec(member)
                } else {
                    cmd.reply("§] §> §3Player not found.")
                }
            }
        }
    }

    static def factionMemberFromCommand(PlayerContext cmd, boolean checkWild = true, BiConsumer<Faction, Member> consumer) {
        Schedulers.async().execute {
            def member = UUIDDataManager.getData(cmd.sender().getUniqueId(), Member)

            if (member != null) {
                def faction = UUIDDataManager.getData(member.getFactionId(), Faction, true)

                if (faction != null) {
                    if (checkWild) {
                        if (faction.id == Factions.wildernessId) {
                            cmd.reply("§3You are not in a faction.")
                            return
                        }
                    }
                    consumer.accept(faction, member)
                } else {
                    Factions.setWilderness(member)
                    cmd.reply("§3You are not in a faction.")
                }
            }
        }
    }

}
