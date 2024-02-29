package scripts.exec

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.item.ItemStackBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

Exports.ptr("Votifier:voteUrls", [
        "starcade.org/vote/MCNet"      : "todo-servers1",
        "starcade.org/vote/MCServers": "todo-servers2",
        "starcade.org/vote/BMC"      : "todo-servers3",
        "starcade.org/vote/TOP"    : "todo-servers4",
        "starcade.org/vote/MCPE"     : "todo-servers5",
        "starcade.org/vote/MBS"     : "todo-servers6",
])

Exports.ptr("InteractionRewards:streakRewards", [
        7 : [
                name      : "§ex1 §a§lEach XL Pouch",
                onPurchase: { Player p ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} credits xl")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} beacon xl")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} booster xl")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} crystal xl")
                },
        ],
        14: [
                name      : "§ex1 §a§lEach Massive Pouch",
                onPurchase: { Player p ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} credits massive")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} beacon massive")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} booster massive")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${p.getName()} crystal massive")
                },
        ],
        21: [
                name      : "§ex2 §a§lSkin & Drill Key",
                onPurchase: { Player p ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${p.getName()} skin 2")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${p.getName()} robot 2")
                },
        ]
])