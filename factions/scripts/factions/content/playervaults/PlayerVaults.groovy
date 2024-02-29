package scripts.factions.content.playervaults

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import scripts.shared.legacy.Vault
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.Pair
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Function

Commands.create().assertPermission("playervaults.commands.reset").handler { command ->
    MySQL.getAsyncDatabase().execute("DELETE FROM playervaults WHERE ${DatabaseUtils.getServerIdExpression()}")
    command.reply("§aCleared database!")
}.register("resetplayervaults")

class VaultInfo {
    public String name
    public int id
    public Material icon
}

static void openVault(Player player, int n, boolean silent = false) {
    int maxVaults = getMaxVaults(player)

    if (maxVaults == 0) {
        Players.msg(player, "§c§lERROR §> §fYou do not have any vaults!")
        return
    }
    if (n < 1 || n > maxVaults) {
        Players.msg(player, "§c§lERROR §> §f/pv <§e1§f-§e${maxVaults}§f>")
        return
    }
    UUID uuid = player.getUniqueId()
    String name = player.getName()

    loadVault(uuid, name, n, { vault ->
        if (vault == null) {
            createVault(uuid, n, { result ->
                if (result == 0) {
                    Players.msg(player, "§c§lERROR §> §fSynchronization error!")
                    return
                }
                openVault(player, n)
            })
            return
        }
        Schedulers.sync().run { showVault(player, vault, silent) }
    })
}

static void openVaultOther(Player player, int n, String name, boolean silent = false) {
    DatabaseUtils.getId(name, { uuid, username, target ->
        if (uuid == null) {
            Players.msg(player, "§] §> §a${username} §fhas never joined the server before!")
            return
        }
        loadVault(uuid, username, n, { vault ->
            if (vault == null) {
                Players.msg(player, "§c§lERROR §> §e${username} §fhas not created §evault #${n} §fyet!")
                return
            }
            Schedulers.sync().run { showVault(player, vault, silent) }
        })
    })
}

static void createVault(UUID owner, int n, Consumer<Integer> consumer) {
    MySQL.getAsyncDatabase().executeUpdate("INSERT IGNORE INTO playervaults (player_id_least, player_id_most, server_id, vault_id, name, data, icon) VALUES (?, ?, ?, ?, NULL, NULL, NULL)", { statement ->
        statement.setLong(1, owner.getLeastSignificantBits())
        statement.setLong(2, owner.getMostSignificantBits())
        statement.setString(3, Temple.templeId)
        statement.setInt(4, n)
    }, { result ->
        consumer.accept(result)
    })
}

static void loadVault(UUID owner, String ownerName, int n, Consumer<Vault> consumer) {
    MySQL.getAsyncDatabase().executeQuery("SELECT name, data, icon FROM playervaults WHERE player_id_least = ? AND player_id_most = ? AND server_id = ? AND vault_id = ? LIMIT 1", { statement ->
        statement.setLong(1, owner.getLeastSignificantBits())
        statement.setLong(2, owner.getMostSignificantBits())
        statement.setString(3, Temple.templeId)
        statement.setInt(4, n)
    }, { result ->
        if (!result.next()) {
            consumer.accept(null)
        } else {
            Vault vault = new Vault()
            vault.number = n
            vault.ownerId = owner
            vault.ownerName = ownerName
            vault.name = result.getString(1)
            vault.data = result.getString(2)
            try {
                vault.icon = Material.valueOf(result.getString(3))
            } catch (Exception ignore) {
                vault.icon = PlayerVaultUtils.VAULT_MATERIAL
            }
            consumer.accept(vault)
        }
    })
}

static void loadVaultInfos(UUID owner, Consumer<Map<Integer, VaultInfo>> consumer) {
    MySQL.getAsyncDatabase().executeQuery("SELECT vault_id, name, icon FROM playervaults WHERE player_id_least = ? AND player_id_most = ? AND ${DatabaseUtils.getServerIdExpression()}", { statement ->
        statement.setLong(1, owner.getLeastSignificantBits())
        statement.setLong(2, owner.getMostSignificantBits())
    }, { result ->
        Map<Integer, VaultInfo> ret = new HashMap<>()

        while (result.next()) {
            VaultInfo vaultInfo = new VaultInfo()
            vaultInfo.name = result.getString(2)
            vaultInfo.id = result.getInt(1)
            try {
                vaultInfo.icon = Material.valueOf(result.getString(3))
            } catch (Exception ignored) {
                vaultInfo.icon = PlayerVaultUtils.VAULT_MATERIAL
            }
            ret.put(result.getInt(1), vaultInfo)
        }
        Schedulers.sync().run { consumer.accept(ret) }
    })
}

static void renameVault(UUID playerId, int vaultId, String name, Runnable callback) {
    MySQL.getAsyncDatabase().executeUpdate("UPDATE playervaults SET name = ? WHERE player_id_least = ? AND player_id_most = ? AND server_id = ? AND vault_id = ? LIMIT 1", { statement ->
        statement.setString(1, name)
        statement.setLong(2, playerId.getLeastSignificantBits())
        statement.setLong(3, playerId.getMostSignificantBits())
        statement.setString(4, Temple.templeId)
        statement.setInt(5, vaultId)
    }, { result ->
        callback.run()
    })
}

static void showVault(Player player, Vault vault, boolean silent) {
    if (!player.isOnline()) {
        return
    }
    if (PlayerVaultUtils.LOCKED.contains(vault.id)) {
        Players.msg(player, "§c§lERROR §> §fVault is already open!")
        return
    }
    if (PlayerVaultUtils.VAULTS.containsKey(player.getUniqueId())) {
        Players.msg(player, "§c§lERROR §> §fYou already have a vault open!")
        return
    }
    String title = vault.name

    if (title == null) {
        title = "§8Player Vault ${vault.number}"
    } else {
        title = "§8${title}"
    }
    Inventory inventory = Bukkit.createInventory(player, 54, title)

    if (vault.data != null) {
        FastInventoryUtils.fromStringAndApply(vault.data, inventory)
    }
    player.openInventory(inventory)
    PlayerVaultUtils.LOCKED.add(vault.id)
    PlayerVaultUtils.VAULTS.put(player.getUniqueId(), Pair.of(vault, inventory))

    if (silent) {
        return
    }
    if (player.getUniqueId() == vault.ownerId) {
        Players.msg(player, "§aOpened vault #${vault.number}")
    } else {
        Players.msg(player, "§aOpened ${vault.ownerName}'s vault #${vault.number}")
    }
}

static MenuEvent getClickEvent(String name, int vaultId) {
    return { p, t, s ->
        if (p.getName() != name) {
            openVaultOther(p, vaultId, name, true)
            return
        }
        if (t == ClickType.RIGHT) {
            openVaultRename(p, vaultId)
            return
        }
        if (t == ClickType.MIDDLE) {
            openVaultIcon(p, vaultId)
            return
        }
        openVault(p, vaultId, true)
    }
}

static void openVaultMenu(Player player, UUID owner = player.getUniqueId(), String ownerName = player.getName()) {
    Player target = Bukkit.getPlayer(owner)
    int maxVaults = target == null ? PlayerVaultUtils.MAX_VAULTS : getMaxVaults(target)

    /*
    if (maxVaults == 0) {
        Players.msg(player, player == target ? "§c§lERROR §> §fYou do not have any vaults!" : "§c§lERROR §> §e${ownerName} §fdoes not have any vaults!")
        return
    }
     */
    if (player != target && maxVaults == 0) {
        Players.msg(player, "§c§lERROR §> §e${ownerName} §fdoes not have any vaults!")
        return
    }
    loadVaultInfos(owner, { vaultInfos ->
        if (!player.isOnline()) {
            return
        }
        Function<Integer, String> defaultTitle = { i -> return "§aPlayer Vault #${i}" }
        MenuBuilder builder = new MenuBuilder((int) Math.ceil(PlayerVaultUtils.MAX_VAULTS / 9.0D) * 9, player.getUniqueId() != owner ? "${ownerName}'s Vaults" : "Vaults")

        for (int i = 1; i <= maxVaults; i++) {
            ItemStack item = new ItemStack(PlayerVaultUtils.VAULT_MATERIAL)

            if (!vaultInfos.containsKey(i)) {
                FastItemUtils.setDisplayName(item, player.getName() != ownerName ? "§f${defaultTitle.apply(i)} §7§o(Not Created)" : "§aPlayer Vault #${i}")
            } else {
                String title = vaultInfos.get(i).name

                item = new ItemStack(vaultInfos.get(i).icon)

                if (title == null) {
                    title = defaultTitle.apply(i)
                }
                title = "§f${title}"
                FastItemUtils.setDisplayName(item, title)

                List<String> lore = [
//                        "",
//                        "§7§o(/pv ${i})",
//                        "",
//                        "§9 * §eLeft Click to Open "
                ]
//                if (player.getName() == ownerName) {
//                    lore.add("§9 * §bRight Click to Rename ")
//                    lore.add("§5 * §dMMB to Change Icon")
//                }
                FastItemUtils.setLore(item, lore)
            }
            builder.set(i - 1, item, getClickEvent(ownerName, i))
        }
        for (int i = maxVaults; i < PlayerVaultUtils.MAX_VAULTS; ++i) {
            builder.set(i, FastItemUtils.createItem(PlayerVaultUtils.VAULT_MATERIAL, "§c${defaultTitle.apply(i + 1)}", [
                    "",
                    "§b * Buy a rank @ §e§n§:store§r ",
                    "§b  for access to this vault!"
            ]), { p, t, s -> })
        }
        player.openInventory(builder.get())
    })
}

static void openVaultMenuOther(Player player, String name) {
    DatabaseUtils.getId(name, { uuid, username, target ->
        if (uuid == null) {
            Players.msg(player, "§] §> §a${username} §fhas never joined the server before!")
            return
        }
        openVaultMenu(player, uuid, name)
    })
}

static void openVaultRename(Player player, int n) {
    SignUtils.openSign(player, ["", "^ ^ ^", "Enter Name"], { lines, p ->
        renameVault(player.getUniqueId(), n, lines[0].isEmpty() ? null : ChatColor.translateAlternateColorCodes((char) '&', lines[0]), {
            openVaultMenu(player)
        })
    })
}

static void openVaultIcon(Player player, int vaultId, int page = 1) {
    Schedulers.async().run({
        List<Material> mats = Material.values().toList()
        List<Material> toRemove = new ArrayList<>()
        for (Material mat : mats) {
            String name = mat.name()
            if (name.endsWith("AIR") || name.contains("LEGACY") || name.contains("WALL") || name.contains("HEAD") || name.contains("ATTACHED")
                    || name.contains("SAPLING") || name == "BEETROOT_SEEDS" || name == "BEETROOTS" || name == "BUBBLE_COLUMN" || name == "CARROTS"
                    || name == "COCOA" || name == "END_GATEWAY" || name == "END_PORTAL" || name == "FIRE" || name == "FROSTED_ICE" || name == "KELP_PLANT"
                    || name == "LAVA" || name.contains("STEM") || name == "NETHER_PORTAL" || name == "POTATOES" || name.contains("POTTED") || name == "MOVING_PISTON"
                    || name == "REDSTONE_WIRE" || name == "SWEET_BERRY_BUSH" || name == "TALL_SEAGRASS" || name == "TRIPWIRE" || name == "WATER" || name.contains("COMMAND")
            ) {
                toRemove.add(mat)
            }
        }
        mats.removeAll(toRemove)

        MenuBuilder builder = MenuUtils.createPagedMenu("§cSelect an icon", mats, { Material mat, Integer i ->
            ItemStack itemStack = FastItemUtils.createItem(mat, StringUtils.capitalize(mat.name().replace("_", " ")), ["§eClick to select!"])
            if (itemStack == null) {
                return FastItemUtils.createItem(Material.BEDROCK, "shite", [])
            }
            return itemStack
        }, page, true, [
                { p, t, s ->
                    Material mat = player.getOpenInventory().getItem(s).type.name()
                    if (mat == null) {
                        return
                    }

                    MySQL.getAsyncDatabase().executeUpdate("UPDATE playervaults SET icon = ? WHERE player_id_least = ? AND player_id_most = ? AND server_id = ? AND vault_id = ? LIMIT 1", { statement ->
                        statement.setString(1, mat.name())
                        statement.setLong(2, player.uniqueId.leastSignificantBits)
                        statement.setLong(3, player.uniqueId.mostSignificantBits)
                        statement.setString(4, Temple.templeId)
                        statement.setInt(5, vaultId)
                    }, { result ->
                        Schedulers.sync().run {
                            player.performCommand("pv")
                        }
                    })
                },
                { p, t, s -> openVaultIcon(player, vaultId, page + 1) },
                { p, t, s -> openVaultIcon(player, vaultId, page - 1) },
                { p, t, s -> openVaultIcon(player, vaultId) }
        ])
        MenuUtils.syncOpen(player, builder)
    })
}

/*
Commands.create().assertPermission("commands.wipepvrobots").handler { command ->
    MySQL.getAsyncDatabase().executeQuery("SELECT * FROM playervaults WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
        // player_id_least BIGINT NOT NULL, player_id_most BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, vault_id INT NOT NULL, name VARCHAR(16), data
        List<Vault> vaults = new ArrayList<>()

        println "LOADING VAULTS"

        while (result.next()) {
            UUID uuid = new UUID(result.getLong(2), result.getLong(1))
            Vault vault = new Vault()
            vault.number = result.getInt(4)
            vault.ownerId = uuid
            vault.ownerName = ""
            vault.name = result.getString(5)
            vault.data = result.getString(6)
            vaults.add(vault)
        }
        println "LOADED VAULTS"

        Schedulers.sync().run {
            int failed = 0
            Closure<Boolean> isRobot = Exports.ptr("isRobotItem")

            for (Vault vault : vaults) {
                Inventory inventory = Bukkit.createInventory(null, 54, "")

                if (vault.data != null) {
                    try {
                        FastInventoryUtils.fromStringAndApply(vault.data, inventory)
                    } catch (Exception ignore) {
                        ++failed
                        continue
                    }
                    for (int i = 0; i < 54; ++i) {
                        ItemStack item = inventory.getItem(i)

                        if (isRobot.call(item)) {
                            inventory.setItem(i, null)
                        }
                    }
                    saveVault(vault, inventory)
                }
            }
            println "CLEARED VAULTS (${failed}/${vaults.size()} failed)"
        }
    })
}.register("wipepvrobots")

Commands.create().assertPermission("commands.wipepvmaxedtools").handler { command ->
    MySQL.getAsyncDatabase().executeQuery("SELECT * FROM playervaults WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
        // player_id_least BIGINT NOT NULL, player_id_most BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, vault_id INT NOT NULL, name VARCHAR(16), data
        List<Vault> vaults = new ArrayList<>()

        println "LOADING VAULTS"

        while (result.next()) {
            UUID uuid = new UUID(result.getLong(2), result.getLong(1))
            Vault vault = new Vault()
            vault.number = result.getInt(4)
            vault.ownerId = uuid
            vault.ownerName = ""
            vault.name = result.getString(5)
            vault.data = result.getString(6)
            vaults.add(vault)
        }
        println "LOADED VAULTS"

        Schedulers.sync().run {
            int failed = 0
            int cleared = 0
            Closure<Boolean> isOmnitool = Exports.ptr("isOmnitool")

            for (Vault vault : vaults) {
                Inventory inventory = Bukkit.createInventory(null, 54, "")

                if (vault.data != null) {
                    try {
                        FastInventoryUtils.fromStringAndApply(vault.data, inventory)
                    } catch (Exception ignore) {
                        ++failed
                        continue
                    }
                    boolean clearedOne = false

                    for (int i = 0; i < 54; ++i) {
                        ItemStack item = inventory.getItem(i)

                        if (isOmnitool.call(item)) {
                            Map<Enchant, Long> enchantments = EnchantmentUtils.getEnchants(item)

                            if (enchantments == null) {
                                continue
                            }
                            if (enchantments.getOrDefault(Enchant.JACKHAMMER, 0L) == Enchant.JACKHAMMER.maxLevel
                                || enchantments.getOrDefault(Enchant.EXPLOSIVE, 0L) == Enchant.EXPLOSIVE.maxLevel
                                || enchantments.getOrDefault(Enchant.LASER, 0L) == Enchant.LASER.maxLevel) {
                                clearedOne = true
                                inventory.setItem(i, null)
                                ++cleared
                            }
                        }
                    }
                    if (clearedOne) {
                        saveVault(vault, inventory)
                    }
                }
            }
            println "CLEARED VAULTS (${failed}/${vaults.size()} failed, ${cleared} cleared)"
        }
    })
}.register("wipepvmaxedtools")
*/

/*
Commands.create().assertPermission("commands.seemaxedtools").handler { command ->
    for (Player player : Bukkit.getOnlinePlayers()) {
        Inventory inventory = player.getInventory()

        Closure<Boolean> isOmnitool = Exports.ptr("isOmnitool")

        for (int i = 0; i < 36; ++i) {
            ItemStack item = inventory.getItem(i)

            if (isOmnitool.call(item)) {
                Map<Enchant, Long> enchantments = EnchantmentUtils.getEnchants(item)

                if (enchantments == null) {
                    continue
                }
                if (enchantments.getOrDefault(Enchant.JACKHAMMER, 0L) == Enchant.JACKHAMMER.maxLevel
                        || enchantments.getOrDefault(Enchant.EXPLOSIVE, 0L) == Enchant.EXPLOSIVE.maxLevel
                        || enchantments.getOrDefault(Enchant.LASER, 0L) == Enchant.LASER.maxLevel) {
                    command.reply(player.getName())
                }
            }
        }
    }
    command.reply("FINISHED!")
}.register("seemaxedtools")
*/

static void saveVault(Vault vault, Inventory inventory) {
    String serialized = FastInventoryUtils.toString(inventory)

    MySQL.getAsyncDatabase().execute("UPDATE playervaults SET data = ? WHERE ${DatabaseUtils.getServerIdExpression()} AND player_id_least = ? AND player_id_most = ? AND vault_id = ?", { statement ->
        statement.setString(1, serialized)
        statement.setLong(2, vault.ownerId.getLeastSignificantBits())
        statement.setLong(3, vault.ownerId.getMostSignificantBits())
        statement.setInt(4, vault.number)
    })
}

static void saveVault(Player player) {
    UUID uuid = player.getUniqueId()
    Map.Entry<Vault, Inventory> openVault = PlayerVaultUtils.VAULTS.remove(uuid)

    if (openVault == null) {
        return
    }
    Vault vault = openVault.getKey()
    String serialized = FastInventoryUtils.toString(openVault.getValue())
    MySQL.getAsyncDatabase().executeUpdate("UPDATE playervaults SET data = ? WHERE ${DatabaseUtils.getServerIdExpression()} AND player_id_least = ? AND player_id_most = ? AND vault_id = ?", { statement ->
        statement.setString(1, serialized)
        statement.setLong(2, vault.ownerId.getLeastSignificantBits())
        statement.setLong(3, vault.ownerId.getMostSignificantBits())
        statement.setInt(4, vault.number)
    }, { result ->
        if (result != 0) {
            Schedulers.sync().run { PlayerVaultUtils.LOCKED.remove(player.getUniqueId()) }
        }
    })
}

static int getMaxVaults(Player player) {
    for (int i = PlayerVaultUtils.MAX_VAULTS; i > 0; i--) {
        if (player.hasPermission("playervault.${i}")) {
            return i
        }
    }
    return 0
}

/*
Events.subscribe(AsyncPlayerSaveEvent.class, event => {
    event.execute(() => saveVault(event.getPlayer(), event.getAttributes(), event))
})
 */

Events.subscribe(InventoryCloseEvent.class).handler { event ->
    saveVault(event.getPlayer() as Player)
}

Events.subscribe(InventoryOpenEvent.class).handler { event ->
    saveVault(event.getPlayer() as Player)
}

Commands.create().assertPlayer().handler { command ->
    AtomicInteger rebootDelay = Exports.ptr("rebootDelay") as AtomicInteger
    if (rebootDelay != null && rebootDelay.intValue() != -1) {
        command.reply("§] §> §cYou can't do that while the server is restarting!")
        return
    }

    List<String> args = command.args()
    Player player = command.sender()

    if (args.isEmpty()) {
        openVaultMenu(player)
        return
    }
    int n = 0

    try {
        n = Integer.parseInt(args.get(0))
    } catch (Exception ignore) {
        if (player.hasPermission("playervault.admin")) {
            openVaultMenuOther(player, args.get(0))
            return
        }
        Players.msg(player, "§c/pv <n>")
        return
    }
    if (args.size() > 1 && player.hasPermission("playervault.admin")) {
        openVaultOther(player, n, args.get(1))
        return
    }
    openVault(player, n)
}.register("playervault", "pv", "backpack", "bp")

PlayerVaultUtils.init()

class PlayerVaultUtils {
    static Material VAULT_MATERIAL = Material.ENDER_CHEST
    static int MAX_VAULTS = 16

    static Map<UUID, Map.Entry<Vault, Inventory>> VAULTS
    static Set<UUID> LOCKED

    static void init() {
        VAULTS = Persistent.of("pv_vaults", new HashMap<UUID, Map.Entry<Vault, Inventory>>()).get()
        LOCKED = Persistent.of("pv_locked", new HashSet<UUID>()).get()

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS playervaults (player_id_least BIGINT NOT NULL, player_id_most BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, vault_id INT NOT NULL, name VARCHAR(16), data MEDIUMTEXT CHARACTER SET utf8mb4, icon VARCHAR(100), PRIMARY KEY(player_id_least, player_id_most, vault_id, server_id))")
        MySQL.getAsyncDatabase().execute("ALTER TABLE IF EXISTS playervaults MODIFY data MEDIUMTEXT CHARACTER SET utf8mb4")
    }
}