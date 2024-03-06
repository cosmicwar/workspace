package scripts.scoreboard.tab
//package scripts.factions.content.scoreboard.tab
//
//class test {
//}


/*
load(`loads/nms.js`)
load(`loads/playerdatautil.js`)
load(`loads/protocollib.js`)
load(`loads/vanishutil.js`)

// ProtocolLib
const PlayerInfoData = Java.type("com.comphenix.protocol.wrappers.PlayerInfoData")
const PlayerInfoAction = Java.type("com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction")
const NativeGameMode = Java.type("com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode")
const WrappedGameProfile = Java.type("com.comphenix.protocol.wrappers.WrappedGameProfile")
const WrappedChatComponent = Java.type("com.comphenix.protocol.wrappers.WrappedChatComponent")
const WrappedSignedProperty = Java.type("com.comphenix.protocol.wrappers.WrappedSignedProperty")

const MAX_ITEMS = 80

const tags = exports.ptr(`patches/content/tags`)
const staff = exports.ptr(`slash/staff`)
const serverlinker = exports.ptr(`server/skyblock/network/serverlinker`)
const globalplayers = exports.ptr(`patches/network/globalplayers`)
const skins = exports.ptr(`patches/utility/skins`)

const skinProfileCache = exports.getOrDefault(`${mwd}.skinProfileCache`, {})
let elements = exports.getOrDefault(`${mwd}.elements`, [])

// this cache is REQUIRED for the properties diff check
function getSkinProfile(skin) {
    const cached = skinProfileCache[skin.texture]
    if (!cached) {
        return (skinProfileCache[skin.texture] = makeGameProfile(-1, skin))
    }
    return cached
}

function diff(oldElements, newElements) {
    const result = []
    for (let index = 0; index < Math.max(oldElements.length, newElements.length); index++) {
        const oldElement = oldElements[index]
        const newElement = newElements[index]

        //addition/removal
        if (!!newElement && !oldElement) {
            result.push({
                action: `add`,
                element: newElement
            })
            continue
        }
        if (!newElement && !!oldElement) {
            result.push({
                action: `remove`,
                element: oldElement
            })
            continue
        }

        if (!oldElement || !newElement) {
            //no more diff to do
            continue
        }

        //element changes
        if (!oldElement.text.equals(newElement.text)) {
            result.push({
                action: `remove`,
                element: oldElement
            })
            result.push({
                action: `add`,
                element: newElement
            })
            continue
        }
        if (oldElement.text != newElement.text) {
            result.push({
                action: `text`,
                element: newElement
            })
        }
        if (oldElement.ping != newElement.ping) {
            result.push({
                action: `ping`,
                element: newElement
            })
        }
    }
    return result
}

function filterChange(changes, action) {
    return changes
        .filter((change) => change.action == action)
        .map((change) => makeInfoData(change.element))
        .filter(infoData => !!infoData) // Filter out non-existent info datas
}

function getPacketsForDiff(changes) {
    return [
        makePacket(PlayerInfoAction.REMOVE_PLAYER, filterChange(changes, `remove`)),
        makePacket(PlayerInfoAction.ADD_PLAYER, filterChange(changes, `add`)),
        makePacket(PlayerInfoAction.UPDATE_DISPLAY_NAME, filterChange(changes, `text`)),
        makePacket(PlayerInfoAction.UPDATE_LATENCY, filterChange(changes, `ping`))
    ].filter((packet) => !!packet)
}

function makePacket(action, infoDatas) {
    if (!infoDatas || infoDatas.length == 0) {
        return null
    }

    let infoDatasList = new ArrayList()
    for (let index in infoDatas) {
        infoDatasList.add(infoDatas[index])
    }

    let packet = createPacket(PacketType.Play.Server.PLAYER_INFO)
    packet.getPlayerInfoAction().write(0, action)
    packet.getPlayerInfoDataLists().write(0, infoDatasList)
    return packet
}

function makeInfoData({ gameProfile, ping, text }) {
    if (!(gameProfile instanceof WrappedGameProfile)) {
        return
    }

    return new PlayerInfoData(
        gameProfile,
        ping,
        NativeGameMode.SURVIVAL,
        WrappedChatComponent.fromText(colorize(text))
    )
}

function makeGameProfile(index, skinOrProfile) { // skin = { texture: "", signature: "" }, or WrappedGameProfile
    const name = `00000${index}`.slice(-5)
    const profile = new WrappedGameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name)

    if (skinOrProfile) {
        if (skinOrProfile instanceof WrappedGameProfile) {
            const propertySet = skinOrProfile.getProperties().get(`textures`)
            if (!propertySet.isEmpty()) {
                profile.getProperties().put(`textures`, propertySet.iterator().next())
            }
        } else if (skinOrProfile.texture) {
            profile.getProperties().put(`textures`, new WrappedSignedProperty(`textures`, skinOrProfile.texture, skinOrProfile.signature))
        }
    }

    return profile
}

function makeElement(index, text, ping = 69, skinOrProfile = skins().get(`graysquare`)) {
    return {
        index,
        text,
        ping,
        gameProfile: makeGameProfile(index, skinOrProfile instanceof WrappedGameProfile ? skinOrProfile : getSkinProfile(skinOrProfile))
    }
}

function writePlayers(result, players) {
    players.forEach(({ uuid, displayName }) => result.push(makeElement(result.length, displayName, 0, skins().getPlayer(uuid))))
}

function generateList() {
    const result = []

    const online = globalplayers()
        .getPlayersOnServer(`skyblock/`)
        .filter(({ vanished }) => !vanished)
        .sort((a, b) => {
            const tagDiff = a.tagIndex - b.tagIndex
            if (tagDiff == 0) {
                return a.name.toLowerCase().compareTo(b.name.toLowerCase())
            }
            return tagDiff
        })
    const staff = online.filter(({ staff }) => staff)

    if (staff.length > 0) {
        result.push(makeElement(result.length, `&c&lSTAFF &7(${staff.length})`, 0, skins().getDotForColor(`c`)))
        writePlayers(result, staff)
        result.push(makeElement(result.length, ` `))
    }

    online
        .filter(({ staff }) => !staff)
        .map(({ tagIndex }) => tagIndex)
        .filter((tagIndex, arrayIndex, array) => array.indexOf(tagIndex) == arrayIndex)
        .forEach((index) => {
            const players = online.filter(({ tagIndex }) => tagIndex == index)
            const tagDisplay = players[0].tag.length == 0 ? `&7&lNO RANK` : players[0].tag
            const colorCharacter = tagDisplay[1]
            result.push(makeElement(result.length, `${tagDisplay} &7(${players.length})`, 0, skins().getDotForColor(colorCharacter)))
            writePlayers(result, players)
            result.push(makeElement(result.length, ` `))
        })

    for (let i = result.length; i <= MAX_ITEMS; i++) {
        result.push(makeElement(result.length, ` `))
    }

    // cut off the ending elements as otherwise the tab
    // list becomes flickery when updating
    result.length = MAX_ITEMS
    return result
}

bukkit.registerEvent(PlayerJoinEvent.class, (event) => {
    bukkit.runAsyncLater(() => {
        sendPacket(event.getPlayer(), makePacket(
            PlayerInfoAction.ADD_PLAYER,
            elements.map((element) => makeInfoData(element))
        ))
    }, 10)
})

bukkit.runAsyncTimer(
    () => {
        const newElements = generateList()
        const packets = getPacketsForDiff(diff(elements, newElements))
        if (packets.length > 0) {
            bukkit.runSync(() => {
                packets.forEach((packet) => {
                    bukkit.getPlayers().forEach(player => sendPacket(player, packet))
                })
            })
        }
        exports.put(`${mwd}.elements`, (elements = newElements))
    },
    0,
    20
)

*/