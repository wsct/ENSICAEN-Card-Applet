/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Generic File inspired by TB100. A file is made of 2 parts: 1. Header Zone
 * (fixed, defined at time of creation of the file) 2. Data Zone (available to
 * store data, EF or DF)
 */
public abstract class File {

	protected final DedicatedFile _parentDF;
	protected final short _inParentDataOffset;
	protected final short _length;

	private short _headerLength;

	/**
	 * @param parentDF Parent DF.
	 * @param offset   Start byte index of the file in parent DF data zone.
	 * @param size     Size used by the file (header+body).
	 * @param header   File header.
	 */
	public File(DedicatedFile parentDF, short offset, short size, byte[] header) {
		_parentDF = parentDF;
		_inParentDataOffset = offset;
		_length = size;

		if (parentDF != null) {
			setHeader(header);
		}
	}

	//
	// >> Public Methods
	//

	/**
	 * 
	 */
	public final short getOffset() {
		return _inParentDataOffset;
	}

	/**
	 * 
	 */
	public final short getLength() {
		return _length;
	}

	/**
	 * Returns true if all the bytes between offset and offset+length are not yet
	 * written.
	 */
	public abstract boolean isAvailable(short offset, short length);

	/**
	 * Returns true if the file is an Elementary File.
	 */
	public abstract boolean isDF();

	/**
	 * Returns true if the file is a Dedicated File.
	 */
	public abstract boolean isEF();

	/**
	 * Copies the header in the output buffer.
	 */
	public final void getHeader(byte[] output, short offset) {
		short inMemoryOffset = (short) (getInMemoryOffset(_inParentDataOffset) - _headerLength);
		Util.arrayCopy(getMemory(), inMemoryOffset, output, offset, _headerLength);
	}

	/**
	 * Returns the size of the header.
	 */
	public final short getHeaderSize() {
		return _headerLength;
	}

	//
	// >> Protected Methods
	//

	/**
	 *
	 */
	protected final short getInMemoryOffset(short dataOffset) {
		return _parentDF == null ? (short) (_headerLength + dataOffset)
				: _parentDF.getInMemoryOffset((short) (_inParentDataOffset + _headerLength + dataOffset));
	}

	/**
	 *
	 */
	protected byte[] getMemory() {
		return _parentDF.getMemory();
	}

	/**
	 *
	 */
	protected final void setHeader(byte[] header) {
		_headerLength = (short) header.length;

		short inMemoryOffset = (short) (getInMemoryOffset(_inParentDataOffset) - _headerLength);
		Util.arrayCopy(header, (short) 0, getMemory(), inMemoryOffset, _headerLength);
	}
}
