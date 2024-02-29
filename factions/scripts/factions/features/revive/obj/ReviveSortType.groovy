package scripts.factions.features.revive.obj

import org.bson.codecs.pojo.annotations.BsonIgnore

enum ReviveSortType
{
    DEATH_TIME_OLDER_TO_NEW("Death Time (Older to New)"),
    DEATH_TIME_NEWER_TO_OLD("Death Time (Newer to Old)"),
    TIMES_REVIVED_ASCENDING("Times Revived (Ascending)"),
    TIMES_REVIVED_DESCENDING("Times Revived (Descending)"),
    DEATH_CAUSE("Death Cause"),
    KILLED_BY("Killer")

    String name

    ReviveSortType(String name)
    {
        this.name = name
    }

    @BsonIgnore
    ReviveSortType getNext()
    {
        switch (this)
        {
            case DEATH_TIME_OLDER_TO_NEW:
                return DEATH_TIME_NEWER_TO_OLD
            case DEATH_TIME_NEWER_TO_OLD:
                return TIMES_REVIVED_ASCENDING
            case TIMES_REVIVED_ASCENDING:
                return TIMES_REVIVED_DESCENDING
            case TIMES_REVIVED_DESCENDING:
                return DEATH_CAUSE
            case DEATH_CAUSE:
                return KILLED_BY
            case KILLED_BY:
                return DEATH_TIME_OLDER_TO_NEW
        }
    }
}