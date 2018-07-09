/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.Util;

/**
 * Defines a virtual file system inspired by TB100.
 */
public class FileSystem {

    final static byte ATTRIBUTE_WRITTEN = (byte) 0x80;
    final static byte ATTRIBUTE_FREE = (byte) 0x00;

    public final static byte FREE_BYTE = (byte) 0xFF;
    public final static byte WRITTEN_BYTE = (byte) 0x00;

    private byte _dfMax;
    private byte _efMax;
    final ElementaryFile[] elementaryFiles;
    final DedicatedFile[] dedicatedFiles;

    /**
     * Raw data storage.
     */
    final byte[] memory;

    /**
     * Storage for attributes of data words in {@link #memory}.
     * <p>
     * 1 attribute byte per data word.
     */
    final byte[] attributes;

    /**
     * Creates a new virtual file system in memory.
     *
     * @param size  Size of the dedicated memory (WORDS).
     * @param dfMax Max number of DF this instance can handle.
     * @param efMax Max number of EF this instance can handle.
     */
    public FileSystem(short size, byte dfMax, byte efMax) {
        _dfMax = dfMax;
        _efMax = efMax;

        // Allocation of data memory
        memory = new byte[(short) (size << 2)];
        attributes = new byte[size];
        erase((short) 0, (short) (size << 2));

        // Allocation of DF files structures
        dedicatedFiles = new DedicatedFile[_dfMax];
        byte i;
        for (i = 0; i < _dfMax; i++) {
            dedicatedFiles[i] = new DedicatedFile(this);
        }

        // Allocation of EF files structures
        elementaryFiles = new ElementaryFile[_efMax];
        for (i = 0; i < _efMax; i++) {
            elementaryFiles[i] = new ElementaryFile(this);
        }
    }

    //
    // >> Public Methods
    //

    /**
     * Erases data from memory.
     *
     * @param offset Offset from where to start erasing (<em>should be a multiple of
     *               4</em>) (BYTES).
     * @param length Number of bytes to erase (<em>should be a multiple of 4</em>)
     *               (BYTES).
     * @return offset + length
     */
    public final short erase(short offset, short length) {
        // Erase data
        Util.arrayFillNonAtomic(memory, offset, length, FREE_BYTE);

        // Update "written" attributes of erased words
        Util.arrayFillNonAtomic(attributes, (short) (offset / 4), (short) (length / 4), ATTRIBUTE_FREE);

        return (short) (offset + length);
    }

    /**
     * Returns a "not yet used" DF.
     * <p>
     * Warning: 2 successive calls to this method will return the same instance.
     */
    public final DedicatedFile getFreeDF() {
        byte index = 0;
        while (index < _dfMax && dedicatedFiles[index].getLength() != 0) {
            index++;
        }
        return (index == _dfMax ? null : dedicatedFiles[index]);
    }

    /**
     * Returns a "not yet used" EF.
     * <p>
     * Warning: 2 successive calls to this method will return the same instance.
     */
    public final ElementaryFile getFreeEF() {
        byte index = 0;
        while (index < _efMax && elementaryFiles[index].getLength() != 0) {
            index++;
        }
        return (index == _efMax ? null : elementaryFiles[index]);
    }

    /**
     * @param from Offset in the body from where to look for free words (WORDS).
     * @param to   Maximum offset to look for (WORDS).
     * @return Number of consecutive free words (WORDS).
     */
    public short getFreeLength(short from, short to) {
        short i = from;

        while (i <= to && attributes[i] != ATTRIBUTE_WRITTEN) {
            i++;
        }

        if (i == (short) (to + 1)) {
            i--;
        }

        return (short) (i - from);
    }

    /**
     * @param from Offset in the body from where to look for written words (WORDS).
     * @param to   Maximum offset to look for (WORDS).
     * @return Number of consecutive written words.
     */
    public short getWrittenLength(short from, short to) {
        short i = from;

        while (i <= to && attributes[i] == ATTRIBUTE_WRITTEN) {
            i++;
        }

        return (short) (i - from);
    }

    /**
     * Reads a section of data in memory.
     *
     * @param offset       Offset in the body where to start the reading (should be
     *                     a multiple of 4) (BYTES).
     * @param output       Output buffer.
     * @param outputOffset Offset in the output buffer where to write the data
     *                     (BYTES).
     * @param length       Length of the data to read (BYTES).
     * @return new in memory offset
     */
    public final short read(short offset, byte[] output, short outputOffset, short length, boolean secureRead) {

        short iMax = (short) (offset + length);
        short i = offset;
        short writtenLength;
        short freeLength;
        while (i < iMax) {
            writtenLength = (short) (getWrittenLength((short) (i >> 2), (short) ((short) (iMax >> 2) - 1)) << 2);
            if (secureRead) {
                Util.arrayFillNonAtomic(output, outputOffset, writtenLength, WRITTEN_BYTE);
            } else {
                Util.arrayCopyNonAtomic(memory, i, output, outputOffset, writtenLength);
            }
            i += writtenLength;
            outputOffset += writtenLength;
            freeLength = (short) (getFreeLength((short) (i >> 2), (short) (iMax >> 2)) << 2);
            Util.arrayFillNonAtomic(output, outputOffset, freeLength, FREE_BYTE);
            i += freeLength;
            outputOffset += freeLength;
        }

        return (short) (outputOffset + length);
    }

    /**
     * Search for up to 4 consecutive bytes in memory.
     *
     * @param offset      Offset of the first word to test in memory (WORDS).
     * @param length      Maximum number of consecutive words to test (WORDS).
     * @param value       Value to search in memory range.
     * @param valueOffset Offset of the first byte to test in value (BYTES).
     * @param valueLength Length of the searched value (BYTES).
     * @return Offset of the first occurrence of value in memory (WORDS).
     */
    public final short search(short offset, short length, byte[] value, short valueOffset, short valueLength) {
        short lengthInBytes = (short) (length << 2);
        if (valueLength > lengthInBytes || (short) (offset + length) > memory.length || (short) (valueOffset + valueLength) > value.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        short valueLengthInWords = (short) ((short) (valueLength + 3) / 4);

        short index = (short) (offset << 2);
        byte compareResult = -1;
        do {
            short x = getWrittenLength((short) (index / 4), (short) ((short) (index + valueLength - 1) / 4));
            if (x == valueLengthInWords) {
                compareResult = Util.arrayCompare(value, valueOffset, memory, (short) index, valueLength);
            }
            index += 4;
        } while (index < lengthInBytes && compareResult != 0);

        if (compareResult == 0) {
            return (short) ((short) (index - 4) >> 2);
        } else {
            return (short) -1;
        }
    }

    /**
     * Writes data in memory.
     *
     * @param source       Buffer containing the data to write.
     * @param sourceOffset Offset of the data in previous buffer (BYTES).
     * @param offset       Offset in memory where to write the data (should be a
     *                     multiple of 4) (BYTES).
     * @param length       Length of the data to write (BYTES).
     * @return new in memory offset
     */
    public short write(byte[] source, short sourceOffset, short offset, short length) {
        // Store data
        Util.arrayCopyNonAtomic(source, sourceOffset, memory, offset, length);

        // Update "written" attributes of used words
        Util.arrayFillNonAtomic(attributes, (short) (offset / 4), (short) (length / 4), ATTRIBUTE_WRITTEN);

        return (short) (offset + length);
    }

    //
    // Private Methods
    //
}
