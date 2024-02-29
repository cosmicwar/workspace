package scripts.factions.content.dbconfig

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.conversions.Bson
import org.bukkit.Material
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.CLEntry
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.content.dbconfig.entries.ItemStackEntry
import scripts.factions.content.dbconfig.entries.LongEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.SREntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.UUIDEntry
import scripts.factions.content.dbconfig.entries.list.ItemStackEntryList
import scripts.factions.content.dbconfig.entries.list.ItemTypeListEntry
import scripts.factions.content.dbconfig.entries.list.MaterialListEntry
import scripts.factions.content.dbconfig.entries.list.PositionListEntry
import scripts.factions.content.dbconfig.entries.list.SRListEntry
import scripts.factions.content.dbconfig.entries.list.StringListEntry

// Assert Certain UUID's for different config types.

@CompileStatic(TypeCheckingMode.SKIP)
class RegularConfig {

    String internalId

    String displayName
    Material material = Material.BOOK
    List<String> description = []

    Set<BooleanEntry> booleans = Sets.newConcurrentHashSet()
    Set<StringEntry> strings = Sets.newConcurrentHashSet()
    Set<IntEntry> ints = Sets.newConcurrentHashSet()
    Set<DoubleEntry> doubles = Sets.newConcurrentHashSet()
    Set<LongEntry> longs = Sets.newConcurrentHashSet()
    Set<MaterialEntry> materials = Sets.newConcurrentHashSet()
    Set<MaterialListEntry> materialLists = Sets.newConcurrentHashSet()
    Set<UUIDEntry> uuids = Sets.newConcurrentHashSet()

    Set<PositionEntry> positions = Sets.newConcurrentHashSet()
    Set<CLEntry> cls = Sets.newConcurrentHashSet()
    Set<SREntry> srs = Sets.newConcurrentHashSet()
    Set<PositionListEntry> positionLists = Sets.newConcurrentHashSet()
    Set<SRListEntry> srLists = Sets.newConcurrentHashSet()

    // enchant customs
    Set<ItemTypeListEntry> itemTypes = Sets.newConcurrentHashSet()
    Set<StringListEntry> stringLists = Sets.newConcurrentHashSet()

    Set<ItemStackEntry> itemStacks = Sets.newConcurrentHashSet()
    Set<ItemStackEntryList> itemStackLists = Sets.newConcurrentHashSet()

    RegularConfig() {}

    RegularConfig(String id, String displayName, List<String> description = [], Material material = Material.BOOK) {
        this.internalId = id
        this.displayName = displayName
        this.description = description
        this.material = material
    }

    @BsonIgnore
    Collection<ConfigEntry> getEntries() {
        Collection<ConfigEntry> entries = []

        entries.addAll(booleans.collect {it.get() })
        entries.addAll(strings.collect{ it.get() })
        entries.addAll(ints.collect { it.get() })
        entries.addAll(doubles.collect { it.get() })
        entries.addAll(longs.collect { it.get() })
        entries.addAll(materials.collect { it.get() })
        entries.addAll(positions.collect { it.get() })
        entries.addAll(cls.collect { it.get() })
        entries.addAll(srs.collect { it.get() })
        entries.addAll(itemTypes.collect { it.get() })
        entries.addAll(stringLists.collect { it.get() })
        entries.addAll(positionLists.collect { it.get() })
        entries.addAll(materialLists.collect { it.get() })
        entries.addAll(uuids.collect { it.get() })
        entries.addAll(srLists.collect { it.get() })

        entries.addAll(itemStacks.collect { it.get() })
        entries.addAll(itemStackLists.collect { it.get() })

        return entries
    }

    @BsonIgnore
    def addDefault(Collection<ConfigEntry> entries) {
        addEntries(entries, true)
    }

    @BsonIgnore
    def addEntries(Collection<ConfigEntry> entries, boolean defaultEntry = false) {
        entries.each { addEntry(it.get(), defaultEntry) }
    }

    @BsonIgnore
    def resetToDefault() {
        booleans.each {it.resetToDefault() }
        strings.each { it.resetToDefault() }
        ints.each { it.resetToDefault() }
        doubles.each { it.resetToDefault() }
        longs.each { it.resetToDefault() }
        materials.each { it.resetToDefault() }
        positions.each { it.resetToDefault() }
        cls.each { it.resetToDefault() }
        srs.each { it.resetToDefault() }
        itemTypes.each { it.resetToDefault() }
        stringLists.each { it.resetToDefault() }
        positionLists.each { it.resetToDefault() }
        materialLists.each { it.resetToDefault() }
        uuids.each { it.resetToDefault() }
        srLists.each { it.resetToDefault() }
        itemStacks.each { it.resetToDefault() }
        itemStackLists.each { it.resetToDefault() }
    }

    @BsonIgnore
    def addEntry(ConfigEntry entry, boolean defaultEntry = false) {
        def foundEntry = getEntry(entry.id, entry.type)
        if (foundEntry) {
            if (!defaultEntry) return
        }

        switch (entry.type) {
            case ConfigType.BOOLEAN:
                def booleanEntry = entry as BooleanEntry
                if (defaultEntry && foundEntry) {
                    def foundBoolean = foundEntry as BooleanEntry
                    foundBoolean.defaultValue = booleanEntry.defaultValue
                    break
                }
                booleans.add(booleanEntry)
                break
            case ConfigType.CL:
                def clEntry = entry as CLEntry
                if (defaultEntry && foundEntry) {
                    def foundCl = foundEntry as CLEntry
                    foundCl.defaultValue = clEntry.defaultValue
                    break
                }
                cls.add(clEntry)
                break
            case ConfigType.DOUBLE:
                def doubleEntry = entry as DoubleEntry
                if (defaultEntry && foundEntry) {
                    def foundDouble = foundEntry as DoubleEntry
                    foundDouble.defaultValue = doubleEntry.defaultValue
                    break
                }
                doubles.add(doubleEntry)
                break
            case ConfigType.LONG:
                def longEntry = entry as LongEntry
                if (defaultEntry && foundEntry) {
                    def foundLong = foundEntry as LongEntry
                    foundLong.defaultValue = longEntry.defaultValue
                    break
                }
                longs.add(longEntry)
                break
            case ConfigType.MATERIAL:
                def materialEntry = entry as MaterialEntry
                if (defaultEntry && foundEntry) {
                    def foundMaterial = foundEntry as MaterialEntry
                    foundMaterial.defaultValue = materialEntry.defaultValue
                    break
                }
                materials.add(materialEntry)
                break
            case ConfigType.INT:
                def intEntry = entry as IntEntry
                if (defaultEntry && foundEntry) {
                    def foundInt = foundEntry as IntEntry
                    foundInt.defaultValue = intEntry.defaultValue
                    break
                }
                ints.add(intEntry)
                break
            case ConfigType.POSITION:
                def positionEntry = entry as PositionEntry
                if (defaultEntry && foundEntry) {
                    def foundPosition = foundEntry as PositionEntry
                    foundPosition.defaultValue = positionEntry.defaultValue
                    break
                }
                positions.add(positionEntry)
                break
            case ConfigType.STRING:
                def stringEntry = entry as StringEntry
                if (defaultEntry && foundEntry) {
                    def foundString = foundEntry as StringEntry
                    foundString.defaultValue = stringEntry.defaultValue
                    break
                }
                strings.add(stringEntry)
                break
            case ConfigType.SR:
                def srEntry = entry as SREntry
                if (defaultEntry && foundEntry) {
                    def foundSr = foundEntry as SREntry
                    foundSr.defaultValue = srEntry.defaultValue
                    break
                }
                srs.add(srEntry)
                break
            case ConfigType.LIST_ITEM_TYPE:
                def itemTypeList = entry as ItemTypeListEntry
                if (defaultEntry && foundEntry) {
                    def foundItemType = foundEntry as ItemTypeListEntry
                    foundItemType.defaultValue = itemTypeList.defaultValue
                    break
                }
                itemTypes.add(itemTypeList)
                break
            case ConfigType.LIST_STRING:
                def stringList = entry as StringListEntry
                if (defaultEntry && foundEntry) {
                    def foundStringList = foundEntry as StringListEntry
                    foundStringList.defaultValue = stringList.defaultValue
                    break
                }
                stringLists.add(stringList)
                break
            case ConfigType.LIST_POSITION:
                def positionList = entry as PositionListEntry
                if (defaultEntry && foundEntry) {
                    def foundPositionList = foundEntry as PositionListEntry
                    foundPositionList.defaultValue = positionList.defaultValue
                    break
                }
                positionLists.add(positionList)
                break
            case ConfigType.LIST_MATERIAL:
                def materialList = entry as MaterialListEntry
                if (defaultEntry && foundEntry) {
                    def foundMaterialList = foundEntry as MaterialListEntry
                    foundMaterialList.defaultValue = materialList.defaultValue
                    break
                }
                materialLists.add(materialList)
                break
            case ConfigType.UUID:
                def uuidEntry = entry as UUIDEntry
                if (defaultEntry && foundEntry) {
                    def foundUuid = foundEntry as UUIDEntry
                    foundUuid.defaultValue = uuidEntry.defaultValue
                    break
                }
                uuids.add(uuidEntry)
                break
            case ConfigType.LIST_SR:
                def srList = entry as SRListEntry
                if (defaultEntry && foundEntry) {
                    def foundSrList = foundEntry as SRListEntry
                    foundSrList.defaultValue = srList.defaultValue
                    break
                }
                srLists.add(srList)
                break
            case ConfigType.ITEM_STACK:
                def itemStack = entry as ItemStackEntry
                if (defaultEntry && foundEntry) {
                    def foundItemStack = foundEntry as ItemStackEntry
                    foundItemStack.defaultValue = itemStack.defaultValue
                    break
                }
                itemStacks.add(itemStack)
                break
            case ConfigType.LIST_ITEM_STACK:
                def itemStackList = entry as ItemStackEntryList
                if (defaultEntry && foundEntry) {
                    def foundItemStackList = foundEntry as ItemStackEntryList
                    foundItemStackList.defaultValue = itemStackList.defaultValue
                    break
                }
                itemStackLists.add(itemStackList)
                break
        }
    }

    @BsonIgnore
    ConfigEntry getEntry(String id, ConfigType type) {
        switch (type) {
            case ConfigType.BOOLEAN:
                return booleans.find { it.id == id }
            case ConfigType.CL:
                return cls.find { it.id == id }
            case ConfigType.DOUBLE:
                return doubles.find { it.id == id }
            case ConfigType.LONG:
                return longs.find { it.id == id }
            case ConfigType.MATERIAL:
                return materials.find { it.id == id }
            case ConfigType.INT:
                return ints.find { it.id == id }
            case ConfigType.POSITION:
                return positions.find { it.id == id }
            case ConfigType.STRING:
                return strings.find { it.id == id }
            case ConfigType.SR:
                return srs.find { it.id == id }
            case ConfigType.LIST_ITEM_TYPE:
                return itemTypes.find { it.id == id }
            case ConfigType.LIST_STRING:
                return stringLists.find { it.id == id }
            case ConfigType.LIST_POSITION:
                return positionLists.find { it.id == id }
            case ConfigType.LIST_MATERIAL:
                return materialLists.find { it.id == id }
            case ConfigType.UUID:
                return uuids.find { it.id == id }
            case ConfigType.LIST_SR:
                return srLists.find { it.id == id }
            case ConfigType.ITEM_STACK:
                return itemStacks.find { it.id == id }
            case ConfigType.LIST_ITEM_STACK:
                return itemStackLists.find { it.id == id }
            default:
                return null
        }
    }

    @BsonIgnore
    ConfigEntry getEntryConst(ConfigEntry constEntry) {
        return getEntry(constEntry.id, constEntry.type)
    }

    @BsonIgnore
    ConfigEntry getEntryById(String id) {
        return getEntries().find { it.id == id }
    }

    @BsonIgnore
    IntEntry getIntEntry(String id) {
        return ints.find { it.id == id }
    }

    @BsonIgnore
    BooleanEntry getBooleanEntry(String id) {
        return booleans.find { it.id == id }
    }

    @BsonIgnore
    StringEntry getStringEntry(String id) {
        return strings.find { it.id == id }
    }

    @BsonIgnore
    DoubleEntry getDoubleEntry(String id) {
        return doubles.find { it.id == id }
    }

    @BsonIgnore
    LongEntry getLongEntry(String id) {
        return longs.find { it.id == id }
    }

    @BsonIgnore
    MaterialEntry getMaterialEntry(String id) {
        return materials.find { it.id == id }
    }

    @BsonIgnore
    PositionEntry getPositionEntry(String id) {
        return positions.find { it.id == id }
    }

    @BsonIgnore
    CLEntry getCLEntry(String id) {
        return cls.find { it.id == id }
    }

    @BsonIgnore
    SREntry getSREntry(String id) {
        return srs.find { it.id == id }
    }

    @BsonIgnore
    ItemTypeListEntry getItemTypeEntry(String id) {
        return itemTypes.find { it.id == id }
    }

    @BsonIgnore
    StringListEntry getStringListEntry(String id) {
        return stringLists.find { it.id == id }
    }

    @BsonIgnore
    PositionListEntry getPositionListEntry(String id) {
        return positionLists.find { it.id == id }
    }

    @BsonIgnore
    MaterialListEntry getMaterialListEntry(String id) {
        return materialLists.find { it.id == id }
    }

    @BsonIgnore
    UUIDEntry getUUIDEntry(String id) {
        return uuids.find { it.id == id }
    }

    @BsonIgnore
    SRListEntry getSRListEntry(String id) {
        return srLists.find { it.id == id }
    }

    @BsonIgnore
    ItemStackEntry getItemStackEntry(String id) {
        return itemStacks.find { it.id == id }
    }

    @BsonIgnore
    ItemStackEntryList getItemStackListEntry(String id) {
        return itemStackLists.find { it.id == id }
    }

    @BsonIgnore
    boolean hasEntry(String id) {
        return getEntryById(id) != null
    }

    @BsonIgnore
    boolean isEmpty() {
        return false
    }
}