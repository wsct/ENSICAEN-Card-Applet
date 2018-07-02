/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.Util;

/**
 * Abstract File inspired by TB100.
 * <p>
 * A file is made of 2 parts: 1. Header Zone (fixed, defined at time of creation
 * of the file) 2. Data Zone (available to store data, EF or DF)
 */
public abstract class File {

    protected final FileSystem _fileSystem;

    protected DedicatedFile _parentDF;
    /**
     * Offset of first byte of the file in the parent (BYTES, should be a multiple
     * of 4).
     */
    protected short _inParentBodyOffset;
    /**
     * Length of the file (header + body) (WORDS).
     */
    protected short _length;
    /**
     * Length of the header (BYTES, should be a multiple of 4).
     */
    protected short _headerLength;

    /**
     * Initializes a new file.
     *
     * @param fileSystem FileSystem instance in which the file lives.
     */
    public File(FileSystem fileSystem) {
        _fileSystem = fileSystem;
    }

    //
    // >> Public Methods
    //

    /**
     * Reads the first 2 bytes of the header of the file.
     *
     * @return FID of the file.
     */
    public final short getFileId() {
        short inMemoryOffset = getInMemoryOffset((short) (-_headerLength));
        return Util.getShort(_fileSystem.memory, inMemoryOffset);
    }

    /**
     * @return Length of the file (header + body) (WORDS).
     */
    public final short getLength() {
        return _length;
    }

    /**
     * @return Offset the file in memory (start of header).
     */
    public final short getOffset() {
        return _inParentBodyOffset;
    }

    /**
     * @param localWordOffset Offset of the first word to be tested (WORDS)
     * @param length          Number of words to be tested (WORDS)
     * @return true if all the words in the file between localWordOffset and
     * localWordOffset+length are not yet written.
     */
    public abstract boolean isAvailable(short localWordOffset, short length);

    /**
     * @return true if the file is an Elementary File.
     */
    public abstract boolean isDF();

    /**
     * @return true if the file is a Dedicated File.
     */
    public abstract boolean isEF();

    /**
     * Copies the header in the output buffer.
     *
     * @param output Output buffer.
     * @param offset Offset in previous buffer where to write the header.
     */
    public final void getHeader(byte[] output, short offset) {
        _fileSystem.read(getInMemoryOffset((short) (-_headerLength)), output, offset, _headerLength, false);
    }

    /**
     * @return Size of the header (WORDS).
     */
    public final short getHeaderSize() {
        return (short) (_headerLength >> 2);
    }

    /**
     * Marks the file as released.
     * <p>
     * Automatically calls {@link #clearInternals()} to release file specific data.
     */
    public final void release() {
        clearInternals();

        // Erase all memory related reserved by the file (header + body)
        _fileSystem.erase(getInMemoryOffset((short) -_headerLength), _length);

        _headerLength = 0;
        _length = 0;
    }

    /**
     * Sets up the file for use.
     *
     * @param parentDF     Parent DF.
     * @param offset       Offset of the file in parent DF body (WORDS).
     * @param size         Size used by the file (WORDS).
     * @param header       Buffer containing the header of the file (header should
     *                     be 32 bits aligned).
     * @param headerOffset Offset of the header in previous buffer (BYTES).
     * @param headerLength Length of the header (BYTES, should be a multiple of 4).
     */
    public final void setup(DedicatedFile parentDF, short offset, short size, byte[] header, short headerOffset,
                            short headerLength) {
        _parentDF = parentDF;
        _inParentBodyOffset = (short) (offset << 2);
        _length = size;
        _headerLength = headerLength;

        // Write the header
        _fileSystem.write(header, headerOffset, getInMemoryOffset((short) (-_headerLength)), _headerLength);
    }

    //
    // >> Protected Methods
    //

    /**
     * Must be overriden to clear file specific data before file release
     */
    protected abstract void clearInternals();

    /**
     * Converts local file body offset to file system memory offset.
     *
     * @param dataOffset Offset in file body.
     */
    protected final short getInMemoryOffset(short dataOffset) {
        File ancestor = _parentDF;
        short offset = (short) (_inParentBodyOffset + _headerLength + dataOffset);
        while (ancestor != null) {
            offset += (short) (ancestor._inParentBodyOffset + ancestor._headerLength);
            ancestor = ancestor._parentDF;
        }

        return offset;
    }
}
