/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.Util;
import javacard.framework.ISOException;

/**
 * Generic File inspired by TB100.
 */
public abstract class File {

	protected final DedicatedFile _parentDF;
	protected final short _inParentDataOffset;
	protected final short _size;

	private short _headerLength;

	/**
	 * @param parentDF Parent DF.
	 * @param offset   Start byte index of the file in parent DF data zone.
	 * @param size     Size used by the file (header+body).
	 */
	public File(DedicatedFile parentDF, short offset, short size, byte[] header) {
		_parentDF = parentDF;
		_inParentDataOffset = offset;
		_size = size;

		if (parentDF != null) {
			setHeader(header);
		}
	}

	/**
	 * Copy the header in the output buffer.
	 */
	public final void getHeader(byte[] output, short offset) {
		short inMemoryOffset = (short) (getInMemoryOffset(_inParentDataOffset) - _headerLength);
		Util.arrayCopy(getMemory(), inMemoryOffset, output, offset, _headerLength);
	}

	/**
	 * Return the size of the header.
	 */
	public final short getHeaderSize() {
		return _headerLength;
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
	protected final short getInMemoryOffset(short dataOffset) {
		return _parentDF == null ? (short) (_headerLength + dataOffset)
				: _parentDF.getInMemoryOffset((short) (_inParentDataOffset + _headerLength + dataOffset));
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
