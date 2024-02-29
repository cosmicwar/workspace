package scripts.utils

import groovy.transform.CompileStatic

import java.util.function.Function

@CompileStatic
class StringUtils {
    static <T> String asString(List<T> elements, Function<T, String> toString, String between = ",", int offset = 0, String ending = null) {
        for (int i = 0; i < offset; ++i) {
            elements.remove(0)
        }
        StringBuilder builder = new StringBuilder()

        boolean first = true

        for (int i = 0; i < elements.size(); ++i) {
            if (!first) {
                if (ending == null || i != elements.size() - 1) {
                    builder.append(between)
                } else {
                    builder.append(" ")
                }
            } else {
                first = false
            }
            if (i != 0 && i == elements.size() - 1 && ending != null) {
                builder.append(ending)
            }
            builder.append(toString.apply(elements.get(i)))
        }
        return builder.toString()
    }

    static String asString(List<String> elements, String between, int offset) {
        for (int i = 0; i < offset; ++i) {
            elements.remove(0)
        }
        StringBuilder builder = new StringBuilder()

        boolean first = true

        for (String element : elements) {
            if (!first) {
                builder.append(between)
            } else {
                first = false
            }
            builder.append(element)
        }
        return builder.toString()
    }

    static String asString(List<String> elements, String between) {
        return asString(elements, between, 0)
    }

    static String asString(List<String> elements, int offset) {
        return asString(elements, " ", offset)
    }

    static String asString(List<String> elements) {
        return asString(elements, 0)
    }

    static String getProgressBar(long current, long needed, int bars, char character = '|' as char) {
        StringBuilder builder = new StringBuilder(3 * bars)
        if (current >= needed || needed == 0) {
            bars.times { builder.append("§a").append(character) }
            return builder.toString()
        }

        double unit = (needed / bars) as double
        for (double progress = unit; progress <= needed; progress += unit) {
            builder.append(current >= progress ? "§a" : "§7").append(character)
        }
        return builder.toString()
    }

    static String capitalize(String string) {
        if (string == null) string = ""
        string = string.replaceAll("_", " ")
        final int length = string.length()
        final char[] capitalized = new char[length]

        char character, previous = (char) ' '

        for (int i = 0; i < length; ++i) {
            character = string.charAt(i)

            if (character >= 65 && character <= 90) {
                capitalized[i] = previous == (char) ' ' ? character : (character + 32) as char
            } else if (character >= 97 && character <= 122) {
                capitalized[i] = previous == (char) ' ' ? (character - 32) as char : character
            } else {
                capitalized[i] = character
            }
            previous = character
        }
        return new String(capitalized)
    }

    static String[] toLower(String[] arr) {
        if (arr == null) {
            return null
        }
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = arr[i].toLowerCase()
        }
        return arr
    }

    static String parseValue(String str) {
        String raw = str.toLowerCase().replace(",", "")

        int decimalPlaces = 0
        if (raw.contains(".")) {
            int integerPlaces = raw.indexOf('.')
            decimalPlaces = raw.length() - integerPlaces - 2
        }

        int count = 0
        if (raw.contains("k")) {
            count += raw.count("k")
            raw = raw.replace("k", "000")
        }
        if (raw.contains("m")) {
            count += raw.count("m")
            raw = raw.replace("m", "000000")
        }
        if (raw.contains("b")) {
            count += raw.count("b")
            raw = raw.replace("b", "000000000")
        }
        if (raw.contains("t")) {
            count += raw.count("t")
            raw = raw.replace("t", "000000000000")
        }
        if (raw.contains("q")) {
            count += raw.count("q")
            raw = raw.replace("q", "0000000000000000")
        }

        if (count == 1 && decimalPlaces > 0) {
            raw = raw.substring(0, raw.length() - decimalPlaces)
            raw = raw.replace(".", "")
            return raw
        }

        return str
    }

    static String getOrdinal(int num) {
        int lastTenth = num % 100;
        if (lastTenth >= 11 && lastTenth <= 13) {
            return num + "th";
        }

        int lastDigit = num % 10;
        switch (lastDigit) {
            case 1:
                return num + "st";
            case 2:
                return num + "nd";
            case 3:
                return num + "rd";
            default:
                return num + "th";
        }
    }

    static String an(char character) {
        "aAoOiIeEuU".containsIgnoreCase(character.toString()) ? "an" : "a"
    }

    static String an(String next) {
        return an(next.charAt(0))
    }
}