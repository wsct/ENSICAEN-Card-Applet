/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 * Elementary File implementation inspired by TB100.
 */
public class ElementaryFile extends File {

    /**
     * Creates a new empty EF.
     *
     * @param fileSystem The file system instance this DF belongs.
     */
    public ElementaryFile(FileSystem fileSystem) {
        super(fileSystem);
    }

    //
    // >> File
    //

    /**
     * {@inheritDoc}
     */
    protected final void clearInternals() {
        // TODO Clear used space.
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAvailable(short localWordOffset, short length) {
        short offset = (short) (getInMemoryOffset((short) (localWordOffset << 2)) >> 2);

        return _fileSystem.getFreeLength(offset, (short) (offset + length)) == length;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isDF() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isEF() {
        return true;
    }

    //
    // Public Methods
    //

    /**
     * Erases {@code length} words of data starting from {@code offset}.
     *
     * @param offset Offset of the first word of data to erase (WORDS).
     * @param length Number of words to erase (WORDS).
     * @return offset + length
     */
    public short erase(short offset, short length) {
        _fileSystem.erase(getInMemoryOffset((short) (offset << 2)), (short) (length << 2));

        return (short) (offset + length);
    }

    /**
     * Reads a sequence of words from the file.
     *
     * @param offset       Offset in the body where to start the reading (WORD).
     * @param output       Output buffer.
     * @param outputOffset Offset in the output buffer where to write the data
     *                     (BYTES).
     * @param length       Length of the data to read (WORDS).
     * @return offset + length;
     */
    public short read(short offset, byte[] output, short outputOffset, short length, boolean secureRead) {
        _fileSystem.read(getInMemoryOffset((short) (offset << 2)), output, outputOffset, (short) (length << 2), secureRead);

        return (short) (offset + length);
    }

    /**
     * Search consecutive bytes in file body.
     *
     * @param offset      Offset of the first word to test in file body (WORDS).
     * @param value       Value to search in memory range.
     * @param valueOffset Offset of the first byte to test in value (BYTES).
     * @param valueLength Length of the searched value (BYTES).
     * @return Offset of the first occurrence of value in file body (WORDS).
     */
    public final short search(short offset, byte[] value, short valueOffset, short valueLength) {
        if (getHeaderSize() + offset > _length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        short searchResult = _fileSystem.search(
                (short) (getInMemoryOffset((short) (offset << 2)) >> 2),
                (short) (_length - offset),
                value, valueOffset, valueLength);

        if (searchResult == -1) {
            return searchResult;
        }

        return (short) (searchResult - (getInMemoryOffset((short) 0) >> 2));
    }

    /**
     * Writes a sequence of words to the file.
     *
     * @param source       Buffer containing the data to write.
     * @param sourceOffset Offset of the data in previous buffer (BYTES).
     * @param offset       Offset in the body where to write the data (WORDS).
     * @param length       Length of the data to write (WORDS).
     * @return offset + length;
     */
    public short write(byte[] source, short sourceOffset, short offset, short length) {
        _fileSystem.write(source, sourceOffset, getInMemoryOffset((short) (offset << 2)), (short) (length << 2));

        return (short) (offset + length);
    }
}
