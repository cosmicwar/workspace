package scripts

import org.starcade.starlight.helper.Schedulers
import scripts.shared.database.Standard
import scripts.shared.systems.ServerCache
import scripts.shared.systems.SpoofCache
import scripts.shared.utils.Gson
import scripts.shared.utils.SpoofSettings
import scripts.shared3.Redis

class Bungee {
    int players

    Bungee(int players) {
        this.players = players
    }
}

def spoofSmooth = 0

Schedulers.async().runRepeating({
    def spoofed = SpoofCache.spoofs.values().sum() as Integer
    def spoofedPlayers = spoofed

    ServerCache.servers.forEach({ key, value ->
        if (value.type.endsWith("_cloned")) return;
        def online = value.players - value.spoofed
        double multi = (double) (SpoofSettings.spoofMultiplier + (value.name == "pacific3" ? 1.0 : 0))
        def calculatedSpoof = (online * multi).toInteger() - online
        def identity = "${Standard.CONFIG_SPOOF}${value.address}".toString()
        def fetchedSpoof = SpoofCache.get(value.address)
        def newValue = 0

        def min = SpoofSettings.customMins.getOrDefault(value.name, SpoofSettings.minCount)

        if (!value.canSpoof) {
            if (fetchedSpoof == 0) return
            else newValue = 0
        } else {
            if ((online == 0 && min == 0) || SpoofSettings.banned.contains(value.name)) {
                if (fetchedSpoof == 0) return
                else newValue = 0
            } else {
                if (fetchedSpoof > calculatedSpoof && fetchedSpoof >= min) {
                    newValue = fetchedSpoof - 1
                } else if (fetchedSpoof < calculatedSpoof || fetchedSpoof < min) {
                    if ((SpoofSettings.maxCount == -1 || spoofed < SpoofSettings.maxCount || fetchedSpoof < min)) {
                        newValue = fetchedSpoof + 1
                    } else {
                        newValue = fetchedSpoof
                    }
                } else {
                    newValue = fetchedSpoof
                }
            }
        }
        //println("${identity}(${value.name}): ${online}/${newValue}/${value.spoofed}/${calculatedSpoof}/${SpoofSettings.minCount}")
        Redis.getGlobal().async { redis -> redis.setex(identity, 60 * 5, newValue.toString()) }
    })


    if (spoofSmooth != 0) {
        if (spoofedPlayers != spoofSmooth) {
            if (spoofSmooth > 0 && spoofSmooth > spoofedPlayers) spoofSmooth--
            else spoofSmooth++
        }
    } else spoofSmooth = spoofedPlayers

    if (spoofSmooth == null) {
        spoofSmooth = 0
    }

    Redis.getGlobal().async { redis -> redis.setex(Standard.KEY_PREFIX_BUNGEE + "spoofbungee", 60 * 5, Gson.gson.toJson(new Bungee(spoofSmooth))) }
}, 0, 1)
