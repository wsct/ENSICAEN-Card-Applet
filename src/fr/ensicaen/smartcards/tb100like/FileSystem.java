/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.Util;

/**
 * Defines a virtual file system inspired by TB100.
 */
public class FileSystem {

	private final static byte ATTRIBUTE_WRITTEN = (byte) 0x80;
	private final static byte ATTRIBUTE_FREE = (byte) 0x00;

	public final static byte FREE_BYTE = (byte) 0xFF;

	private byte _dfMax;
	private byte _efMax;
	private final ElementaryFile[] _elementaryFiles;
	private final DedicatedFile[] _dedicatedFiles;

	/**
	 * Raw data storage.
	 */
	private final byte[] _memory;

	/**
	 * Storage for attributes of data words in {@link #_memory}.
	 * 
	 * 1 attribute byte per data word.
	 */
	private final byte[] _attributes;

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
		_memory = new byte[(short) (size << 2)];
		_attributes = new byte[size];

		// Allocation of DF files structures
		_dedicatedFiles = new DedicatedFile[_dfMax];
		byte i;
		for (i = 0; i < _dfMax; i++) {
			_dedicatedFiles[i] = new DedicatedFile(this);
		}

		// Allocation of EF files structures
		_elementaryFiles = new ElementaryFile[_efMax];
		for (i = 0; i < _efMax; i++) {
			_elementaryFiles[i] = new ElementaryFile(this);
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
	 * 
	 * @return offset + length
	 */
	public final short erase(short offset, short length) {
		// Erase data
		Util.arrayFillNonAtomic(_memory, offset, length, (byte) 0x00);

		// Update "written" attributes of erased words
		Util.arrayFillNonAtomic(_attributes, (short) (offset / 4), (short) (length / 4), ATTRIBUTE_FREE);

		return (short) (offset + length);
	}

	/**
	 * Returns a "not yet used" DF.
	 * 
	 * Warning: 2 successive calls to this method will return the same instance.
	 */
	public final DedicatedFile getFreeDF() {
		byte index = 0;
		while (index < _dfMax && _dedicatedFiles[index].getLength() != 0) {
			index++;
		}
		return (index == _dfMax ? null : _dedicatedFiles[index]);
	}

	/**
	 * Returns a "not yet used" EF.
	 * 
	 * Warning: 2 successive calls to this method will return the same instance.
	 */
	public final ElementaryFile getFreeEF() {
		byte index = 0;
		while (index < _efMax && _elementaryFiles[index].getLength() != 0) {
			index++;
		}
		return (index == _efMax ? null : _elementaryFiles[index]);
	}

	/**
	 * @param from Offset in the body from where to look for free words (WORDS).
	 * @param to   Maximum offset to look for (WORDS).
	 * 
	 * @return Number of consecutive free words.
	 */
	public short getFreeLength(short from, short to) {
		short i = from;
		// TODO
		while (i <= to && _attributes[i] != ATTRIBUTE_WRITTEN) {
			i++;
		}

		if (i == (short) (to + 1)) {
			i--;
		}

		return (short) (i - from);
	}

	/**
	 * Returns the memory buffer.
	 */
	public final byte[] getMemory() {
		return _memory;
	}

	/**
	 * @param from Offset in the body from where to look for written words (WORDS).
	 * @param to   Maximum offset to look for (WORDS).
	 * 
	 * @return Number of consecutive written words.
	 */
	public short getWrittenLength(short from, short to) {
		short i = from;
		// TODO
		while (i <= to && _attributes[i] == ATTRIBUTE_WRITTEN) {
			i++;
		}

		if (i == (short) (to + 1)) {
			i--;
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
	 * 
	 * @return new in memory offset
	 */
	public final short read(short offset, byte[] output, short outputOffset, short length) {

		short iMax = (short) (offset + length);
		short i = offset;
		short writtenLength;
		short freeLength;
		while (i < iMax) {
			writtenLength = (short) (getWrittenLength((short) (i / 4), (short) (iMax / 4)) * 4);
			Util.arrayCopyNonAtomic(_memory, i, output, outputOffset, writtenLength);
			i += writtenLength;
			outputOffset += writtenLength;
			freeLength = (short) (getFreeLength((short) (i / 4), (short) (iMax / 4)) * 4);
			Util.arrayFillNonAtomic(output, outputOffset, (short) (freeLength / 4), FREE_BYTE);
			i += freeLength;
			outputOffset += freeLength;
		}

		return (short) (outputOffset + length);
	}

	/**
	 * Writes data in memory.
	 * 
	 * @param source       Buffer containing the data to write.
	 * @param sourceOffset Offset of the data in previous buffer (BYTES).
	 * @param offset       Offset in the body where to write the data (should be a
	 *                     multiple of 4) (BYTES).
	 * @param length       Length of the data to write (BYTES).
	 * 
	 * @return new in memory offset
	 */
	public short write(byte[] source, short sourceOffset, short offset, short length) {
		// Store data
		Util.arrayCopyNonAtomic(source, sourceOffset, _memory, offset, length);

		// Update "written" attributes of used words
		Util.arrayFillNonAtomic(_attributes, (short) (offset / 4), (short) (length / 4), ATTRIBUTE_WRITTEN);

		return (short) (offset + length);
	}

	//
	// Private Methods
	//
}
