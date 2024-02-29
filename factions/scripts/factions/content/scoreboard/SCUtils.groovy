package scripts.factions.content.scoreboard

import com.google.common.collect.Sets
import groovy.transform.CompileStatic

@CompileStatic
class SCUtils {
    static List<String> makeLinesUnique(List<String> list) {
        Set<String> usedStrings = Sets.newConcurrentHashSet()
        List<String> newList = new ArrayList<>()
        for (String string : list) {
            while(usedStrings.contains(string)) {
                string = string + "§r"
            }
            usedStrings.add(string)
            newList.add(string)
        }
        return newList
    }
}
