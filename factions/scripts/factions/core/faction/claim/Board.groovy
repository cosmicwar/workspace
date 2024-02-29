package scripts.factions.core.faction.claim

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.starcade.starlight.helper.Schedulers
import scripts.factions.data.obj.CL
import scripts.factions.data.obj.SR
import scripts.shared3.utils.Callback

@CompileStatic(TypeCheckingMode.SKIP)
class Board {

    String worldName

    Map<CL, Claim> chunkClaims = Maps.newConcurrentMap()
    Map<SR, Claim> regionClaims = Maps.newConcurrentMap()

    Set<Claim> coreChunkClaims = Sets.newConcurrentHashSet()

    Board(String worldName) {
        this.worldName = worldName
    }

    Board(World world) {
        this.worldName = world.getName()
    }

    World world() {
        return Bukkit.getWorld(this.worldName)
    }

    // dont execute callback if claim is null
    void getClaimAtPos(Location location, Callback<Claim> callback) {
        Schedulers.async().execute {
            def claim = regionClaims.find { it.key.contains(location) } // check region first?
            if (claim != null) {
                callback.exec(claim.value)
                return
            }

            claim = chunkClaims.find { it.key == CL.of(location) }
            if (claim != null) {
                callback.exec(claim.value)
            }
        }
    }

    Claim getClaimAtPos(Location location) {
        def entry = regionClaims.find { it.key.contains(location) }
        if (entry != null) return entry.value

        def cl = CL.of(location)

        entry = chunkClaims.find { it.key == cl }
        if (entry != null) return entry.value
        return null
    }

    Claim getChunkClaim(CL cl) {
        return chunkClaims.get(cl)
    }

    synchronized void getChunkClaim(CL cl, Callback<Claim> callback) {
        Schedulers.async().execute {
            def claim = chunkClaims.get(cl)
            if (claim != null) callback.exec(claim)
        }
    }

    synchronized void getRegionClaim(SR sr, Callback<Claim> callback) {
        Schedulers.async().execute {
            def claim = regionClaims.get(sr)
            if (claim != null) callback.exec(claim)
        }
    }

    synchronized void addChunkClaim(CL cl, Claim claim) {
        Schedulers.async().execute {
            chunkClaims.put(cl, claim)
        }
    }

    synchronized void addRegionClaim(SR sr, Claim claim) {
        Schedulers.async().execute {
            regionClaims.put(sr, claim)
        }
    }

    synchronized void removeChunkClaim(CL cl, Callback<Claim> callback = {}) {
        Schedulers.async().execute {
            callback.exec(chunkClaims.remove(cl))
        }
    }

    synchronized void removeRegionClaim(SR sr, Callback<Claim> callback = {}) {
        Schedulers.async().execute {
            callback.exec(regionClaims.remove(sr))
        }
    }

}
