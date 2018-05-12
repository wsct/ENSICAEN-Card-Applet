/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Abstract File inspired by TB100.
 * 
 * A file is made of 2 parts: 1. Header Zone (fixed, defined at time of creation
 * of the file) 2. Data Zone (available to store data, EF or DF)
 */
public abstract class File {

	protected final FileSystem _fileSystem;

	protected DedicatedFile _parentDF;
	protected short _inParentBodyOffset;
	protected short _length;

	private short _headerLength;

	/**
	 * Initializes a new file.
	 * 
	 * @param parentDF Parent DF.
	 * @param offset   Start byte index of the file in parent DF data zone.
	 * @param size     Size used by the file (header+body).
	 * @param header   File header.
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
		return Util.getShort(_fileSystem.getMemory(), inMemoryOffset);
	}

	/**
	 * @return Length of the file (header + body).
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
	 * @return true if all the bytes between offset and offset+length are not yet
	 *         written.
	 */
	public abstract boolean isAvailable(short offset, short length);

	/**
	 * @return true if the file is an Elementary File.
	 */
	public abstract boolean isDF();

	/**
	 * @param true if the file is a Dedicated File.
	 */
	public abstract boolean isEF();

	/**
	 * Copies the header in the output buffer.
	 * 
	 * @param output Output buffer.
	 * @param offset Offset in previous buffer where to write the header.
	 */
	public final void getHeader(byte[] output, short offset) {
		_fileSystem.read(getInMemoryOffset((short) (-_headerLength)), output, offset, _headerLength);
	}

	/**
	 * @return Size of the header.
	 */
	public final short getHeaderSize() {
		return _headerLength;
	}

	/**
	 * Marks the file as released.
	 * 
	 * Automatically calls {@link #clearInternals()} to release file specific data.
	 */
	public final void release() {
		clearInternals();

		_length = 0;
	}

	/**
	 * Sets up the file for use.
	 * 
	 * @param parentDF     Parent DF.
	 * @param offset       Offset of the file in parent DF body.
	 * @param size         Size used by the file.
	 * @param header       Buffer containing the header of the file.
	 * @param headerOffset Offset of the header in previous buffer.
	 * @param headerLength Length of the header.
	 */
	public final void setup(DedicatedFile parentDF, short offset, short size, byte[] header, short headerOffset,
			short headerLength) {
		_parentDF = parentDF;
		_inParentBodyOffset = offset;
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
