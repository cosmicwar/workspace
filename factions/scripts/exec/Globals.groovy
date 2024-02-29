package scripts.exec

import com.google.common.collect.Sets
import com.google.common.io.BaseEncoding
import org.starcade.starlight.utils.RebootRequired
import org.bukkit.Location
import org.bukkit.World
import scripts.shared.utils.Temple

import java.time.LocalTime
import java.util.function.BiFunction
import java.util.regex.Pattern

// This is to prevent changes to this file from causing other scripts to reload.
// The changes are only needed for the _globals.groovy and the script where the global is used which will have the updated version.
@RebootRequired
class Globals {

    static String username = System.getProperty("user.name")
    static boolean isDev = Temple.templeEnv == "dev"
    static boolean isTest = Temple.templeEnv == "test"
    static boolean isProd = Temple.templeEnv == "prod"
    static boolean isLocal = Temple.templeId.endsWith("_local")
    static boolean isArk = username == "arkham"
    static boolean isMcp = Temple.templebase == "prison"
    static boolean isSby = Temple.templebase == "skyblock"
    static boolean isHub = Temple.templebase == "hub" || Temple.templebase == "survival" || Temple.templebase == "nexus"
    static boolean isPixelmon = isDev ? Temple.templebase == "pixelmon" : username == "pxl"
    static boolean isNexus = Temple.templebase == "nexus"
    static boolean[] featureSet = new boolean[100] // do not use
    static String nexusGamemode = isMcp ? "prison" : isSby ? "skyblock" : isHub ? "survival" : "Unknown"
    static List<String> GAMEMODES = ["prison", "survival", "skyblock"] //For validation purposes
    static Set<String> ACTIVE_TEMPLES = Sets.<String> newConcurrentHashSet()
    private static Pattern templeRegexPattern = Pattern.compile("[0-9]+")
    static String templePrefix = templeRegexPattern.matcher(Temple.templeId).replaceAll("")
    static Closure<Integer> worldTimeFunction = { World world -> return 6000 }

    static String RESOURCE_PACK_URL = null
    static byte[] RESOURCE_PACK_HASH = null

    // UTC
    static LocalTime scheduledRebootTime = null

    // generics
    static Location SPAWN_LOCATION
    static World PVP_WORLD
    static World PVE_WORLD

    static boolean PACK_ALWAYS_LOADED = false
    static boolean STORE = false

    static boolean TRACKING_METRICS = false
    static boolean STAFF_TRACKING_METRICS = false

    static boolean COMBATLOG_NPC = false
    static boolean BATTLEPASS = false

    static boolean ITEM_CF = false
    static double ECO_MULTIPLIER = 1D
    static boolean CUSTOM_TAGS = false
    static boolean NEW_GKITS_COOLDOWNS = false
    static boolean ACTION_BAR_INFO = false
    static boolean LOOTBOXES = false

    //skyblock
    static boolean ENVOYS = false
    static boolean SKILLS = false
    static boolean QUESTS = false
    static boolean TOGGLE_PAYMENTS = false
    static boolean TOGGLE_DROPS = false
    static boolean TOGGLE_SPAWNER_ENTITIES = false
    static boolean RAVAGER_SWORD = false
    static boolean COLLECTIONS = false
    static boolean ENCHANTS_SBY = false
//    static BiFunction<Long, Long, Float> ISLAND_NO_POWER_EFFICIENCY_EVAL = { long used, long max -> return Math.min(1f, Math.min(0f, (float) ((float) max / (float) used))) }
    static boolean SPAWNER_STACKING = true
    static boolean UPGRADABLE_SPAWNERS = false
    static boolean ISLAND_UPGRADABLE_SPAWNERS = false
    static boolean SUPER_SPAWNERS = false
    static boolean ISLAND_UPGRADES_AS_PERKS = false
    static boolean NO_EXP = false
    static boolean MOB_LIMITER = false
    static boolean ISLAND_SPAWNERS_V5 = false
    static boolean FORCED_SPAWNER_LEVELLING = false

    static Map<String, String> SERVER_REMAPPER = new HashMap<>()
    
    //GLOBAL
    static boolean INTERACTION_REWARDS = true
    static boolean CF_VALUES = false
    static boolean GLOBAL_STARDUST = true
    static boolean GLOBAL_BUYCRAFT = true
    static boolean GLOBAL_VOTES = true
    static boolean GLOBAL_TRACKING = true // ???
    static int MIN_PROTOCOL_VERSION = -1
    static boolean UNTRADABLE_CURRENCY = true
    static boolean RULES = false
    static boolean STAFFMODE_GAMEMODE_GLOBAL = true
    static boolean ITEM_SKINS = false
    static boolean statistics = false
    static boolean AUCTION_HOUSE = false
    static boolean STORE_AUTO_LOGIN = true
    static boolean DISCORD_SYNCING = true
    static String STARDUST_NAME = "Stardust"

    //STAFF
    static boolean STAFF = true
    static boolean STAFF2 = false
    static boolean GLOBAL_STAFF_STATS = true

    //RANKS
    static LinkedList<String> STAFF_RANKS = new LinkedList<>([ "owner", "developer", "manager", "admin", "srmod", "mod", "helper", "trial" ])
    static LinkedList<String> DONATOR_RANKS = null
    static LinkedList<String> TAG_RANKS = new LinkedList<>(["partner", "media"])

    //EVENTS
    static boolean HALLOWEEN = false
    static boolean CHRISTMAS_EVENT = false
    static boolean THANKSGIVING_EVENT = false
    static boolean EASTER = false
    static boolean NEW_YEAR_EVENT = false
    static boolean VALENTINES_EVENT = false
    static boolean EASTER_EVENT = false

    static boolean DISABLE_WEATHER = false
}
