package scripts.factions

import org.starcade.starlight.Starlight

Starlight.watch(
        // Shared Systems \\
        "scripts/shared/legacy/currency.groovy",
        "scripts/shared/legacy/2fa.groovy",
        "scripts/shared/legacy/executables.groovy",
        "scripts/shared/legacy/nicknames.groovy",
        "scripts/shared/content/systems/stardust.groovy",
        "scripts/shared/features/actionbar/ActionBars.groovy",
        "~/core/commands/_subcommandbuilder.groovy",
        "~/core/faction/FCommand.groovy",

        "~/content/mobs/MobHandler.groovy",

        // Data \\
        "~/data/DataManager.groovy",
        "~/data/uuid/UUIDDataManager.groovy",


        // Utils \\
        "~/util/ChatPrompt.groovy",


        // Data Handling \\
        "~/content/dbconfig/DBConfig.groovy",

        // World Generation \\
        "~/content/worldgen/WorldManager.groovy",

        // Systems & Content \\
        "~/content/log/Logs.groovy",
        "~/eco/loottable/LootTableHandler.groovy",

        "~/content/clickitem/ClickItems.groovy",
        "~/content/scoreboard/sidebar/SidebarHandler.groovy",
        "~/content/scoreboard/health/HealthDisplay.groovy",

        "~/eco/rewardbox/RewardBoxes.groovy",

        "~/cfg/toggles.groovy",

        // Essentials \\
        "~/content/essentials/Essentials.groovy",

        // Currencies \\
        "scripts/exec/nova1/shop.groovy",
        "~/eco/currency/money.groovy",
        "~/eco/currency/shards.groovy",


        // Spawners \\
        "~/features/spawners/CustomSpawners.groovy",
        "~/features/pve/PveEntityTools.groovy",


        // Shops |  AH  \\
        "~/eco/currency/shardshop.groovy",
        "~/eco/currency/auction.groovy",
        "~/eco/currency/expshop.groovy",


        // FORTNITE \\
        "scripts/shared/features/battlepass/battlepass.groovy",
        "~/eco/misc/BattlePassChallenges.groovy",


        // gambling \\
        "~/eco/gambling/casebattle.groovy",
        "~/eco/gambling/coinflip.groovy",
        "~/eco/gambling/casino.groovy",
        "~/eco/gambling/lotteryV2.groovy",
        "~/eco/gambling/scratchoff.groovy",
        "~/eco/gambling/bar.groovy",

        "~/eco/crates/Crates.groovy",
        "scripts/shared/legacy/claimables.groovy",


        // Core Functionality \\
        "~/core/faction/Factions.groovy",


        // Features \\
        "~/features/enchant/potion/PotionEffects.groovy",

        // cOsMiC \\
        "~/features/enchant/utils/EnchantmentCooldowns.groovy",
        "~/features/enchant/Enchantments.groovy",

        "~/features/pets/ItemPets.groovy",

        // custom sets & pack loader, also loads everything that utilizes pack \\
        "~/features/customset/CustomSets.groovy",
        "~/features/pack/packloader.groovy",

        // player item filter \\
        "~/features/itemfilter/ItemFilter.groovy",

        // wild pvp \\
        "~/features/wild/Wild.groovy",
        "~/features/wildpvp/WildPvpManager.groovy",

        "~/features/customitem//clickenchant/clickenchants.groovy",
        "~/features/customitem/chunkbusters.groovy",
        "~/features/customitem/buckets/GenBuckets.groovy",

        "scripts/shared/features/jumppads.groovy",
        "~/content/playervaults/PlayerVaults.groovy",
        "scripts/shared/legacy/trade.groovy",
        "~/features/tutorial.groovy",
        "~/cosmetics/particles.groovy",

        "~/features/revive/Revives.groovy",

        "~/features/duel/Duels.groovy",

        // Events \\
        "~/events/meteors/Meteors.groovy",
        "~/events/darkzone/Darkzone.groovy",
        "~/events/koth/KOTH.groovy",
        "~/events/stronghold/Strongholds.groovy",
        "~/events/envoy/Envoys.groovy",
        "~/events/arcade/Arcade.groovy",


        // patches \\
//        "~/patches/playernames.groovy",
        "~/patches/bless.groovy",
        "~/patches/playerpatches.groovy",
        "~/patches/blockspatches.groovy",
        "~/patches/commandpatches.groovy",
        "~/patches/entitypatches.groovy",
        "~/patches/inventorypatches.groovy",
        "~/patches/teleportpatches.groovy",
        "~/patches/worldpatches.groovy",

        "~/patch/FPSPatch.groovy",
        "~/patch/InvseePatch.groovy",

        // fixes \\
        "~/fixes/PvpPatches.groovy",
        "~/fixes/hotfixes.groovy",
        "~/cfg/fixes.groovy",
        "~/fixes/holotest.groovy",
        "~/fixes/gsontest.groovy",
        "~/fixes/enttest.groovy",

        "~/cfg/scoreboard.groovy",
        "~/cfg/chat.groovy",

        "~/eco/kits/Kits.groovy",

        "~/content/knockback/Knockback.groovy",
        "~/anticheat/AC.groovy"
)