package scripts.factions.content.dbconfig.data

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic(TypeCheckingMode.SKIP)
enum ConfigType
{
    STRING,
    INT,
    LONG,
    DOUBLE,
    BOOLEAN,
    UUID,
    CL,
    SR,
    POSITION,
    ITEM_TYPE,
    ITEM_STACK,
    MATERIAL,
    // lists
    LIST_ITEM_TYPE,
    LIST_STRING,
    LIST_POSITION,
    LIST_MATERIAL,
    LIST_SR,
    LIST_ITEM_STACK,
}

