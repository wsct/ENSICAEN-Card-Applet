/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.Util;

/**
 * Defines a virtual file system inspired by TB100.
 */
public class FileSystem {

	private byte _dfMax;
	private byte _efMax;
	private final ElementaryFile[] _elementaryFiles;
	private final DedicatedFile[] _dedicatedFiles;

	/**
	 * Raw data storage.
	 */
	private final byte[] _memory;

	/**
	 * Creates a new virtual file system in memory.
	 * 
	 * @param size  Size of the dedicated memory (in words).
	 * @param dfMax Max number of DF this instance can handle.
	 * @param efMax Max number of EF this instance can handle.
	 */
	public FileSystem(short size, byte dfMax, byte efMax) {
		_dfMax = dfMax;
		_efMax = efMax;

		// Allocation of data memory
		_memory = new byte[(short) (size << 2)];

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
	 * Returns the memory buffer.
	 */
	public final byte[] getMemory() {
		return _memory;
	}

	/**
	 * Reads a section of data in memory.
	 * 
	 * @param offset       Offset in the body where to start the reading.
	 * @param output       Output buffer.
	 * @param outputOffset Offset in the output buffer where to write the data.
	 * @param length       Length of the data to read.
	 * 
	 * @return new in memory offset
	 */
	public final short read(short offset, byte[] output, short outputOffset, short length) {
		Util.arrayCopyNonAtomic(_memory, offset, output, outputOffset, length);

		return (short) (offset + length);
	}

	/**
	 * Writes data in memory.
	 * 
	 * @param source       Buffer containing the data to write.
	 * @param sourceOffset Offset of the data in previous buffer.
	 * @param offset       Offset in the body where to write the data.
	 * @param length       Length of the data to write.
	 * 
	 * @return new in memory offset
	 */
	public short write(byte[] source, short sourceOffset, short offset, short length) {
		Util.arrayCopyNonAtomic(source, sourceOffset, _memory, offset, length);

		return (short) (offset + length);
	}
}
