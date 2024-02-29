package scripts.utils

import java.util.Map.Entry

class FileUtils {
    static File create(final String name, final String extension) {
        File file = new File(name + extension)

        for (int i = 1; file.exists(); ++i) {
            file = new File(name + " (" + i + ')' + extension)
        }
        try {
            file.createNewFile()
        } catch (IOException e) {
            e.printStackTrace()
        }
        return file
    }

    static File create(final String path, final String name, final String extension) {
        return create(path + File.separatorChar + name, extension)
    }

    static String read(final File file) {
        String string = null

        try {
            final BufferedReader reader = new BufferedReader(new FileReader(file))

            final char[] buffer = new char[(int) file.length()]

            reader.read(buffer)
            reader.close()

            string = new String(buffer)
        } catch (Exception e) {
            e.printStackTrace()
        }
        return string
    }

    static List<String> getLines(final File file) {
        final List<String> lines = new ArrayList<>()

        try {
            final BufferedReader reader = new BufferedReader(new FileReader(file))

            final char[] buffer = new char[(int) file.length()]
            final int size = reader.read(buffer)

            reader.close()

            if (size == 0) {
                return lines
            }
            StringBuilder line = new StringBuilder()
            char character = 0

            for (int i = 0; i < size; ++i) {
                character = buffer[i]

                if (character == (char) '\n') {
                    lines.add(line.toString())
                    line = new StringBuilder()
                } else {
                    line.append(character)
                }
            }
            if (character != (char) '\n') {
                lines.add(line.toString())
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
        return lines
    }

    static void write(final File file, final String text) {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            final BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))
            writer.write(text)

            writer.flush()
            writer.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    static void write(final File file, final Object object) {
        write(file, object.toString())
    }

    static void write(final File file, final boolean value) {
        write(file, Boolean.toString(value))
    }

    static void write(final File file, final byte value) {
        write(file, Byte.toString(value))
    }

    static void write(final File file, final char value) {
        write(file, Character.toString(value))
    }

    static void write(final File file, final short value) {
        write(file, Short.toString(value))
    }

    static void write(final File file, final int value) {
        write(file, Integer.toString(value))
    }

    static void write(final File file, final float value) {
        write(file, Float.toString(value))
    }

    static void write(final File file, final long value) {
        write(file, Long.toString(value))
    }

    static void write(final File file, final double value) {
        write(file, Double.toString(value))
    }

    static void write(final File file, final Object[] array) {
        final StringBuilder text = new StringBuilder()

        for (final Object element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final boolean[] array) {
        final StringBuilder text = new StringBuilder()

        for (final boolean element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final byte[] array) {
        final StringBuilder text = new StringBuilder()

        for (final byte element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final char[] array) {
        final StringBuilder text = new StringBuilder()

        for (final char element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final short[] array) {
        final StringBuilder text = new StringBuilder()

        for (final short element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final int[] array) {
        final StringBuilder text = new StringBuilder()

        for (final int element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final float[] array) {
        final StringBuilder text = new StringBuilder()

        for (final float element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final long[] array) {
        final StringBuilder text = new StringBuilder()

        for (final long element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static void write(final File file, final double[] array) {
        final StringBuilder text = new StringBuilder()

        for (final double element : array) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static <E> void write(final File file, final Collection<E> collection) {
        final StringBuilder text = new StringBuilder()

        for (final E element : collection) {
            text.append(element).append('\n')
        }
        write(file, text.toString())
    }

    static <K, V> void write(final File file, final Collection<Entry<K, V>> entries, final char seperator) {
        final StringBuilder text = new StringBuilder()

        for (final Entry<K, V> entry : entries) {
            text.append(entry.getKey()).append(seperator).append(entry.getValue()).append('\n')
        }
        write(file, text.toString())
    }

    static <K, V> void write(final File file, final Map<K, V> map) {
        write(file, map.entrySet(), ' ')
    }
}