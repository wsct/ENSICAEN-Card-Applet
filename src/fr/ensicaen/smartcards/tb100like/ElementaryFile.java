/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import javacard.framework.Util;

/**
 * Elementary File inspired by TB100.
 */
public class ElementaryFile extends File {

	/**
	 * @param header   File header.
	 * @param bodySize Number of usable bytes in the EF (body only, without header).
	 */
	public ElementaryFile(DedicatedFile parentDF, short offset, short size, byte[] header) {
		super(parentDF, offset, size, header);
	}

	/**
	 * Write bytes to the file.
	 */
	public short write(byte[] source, short sourceOffset, short bodyOffset, short length) {
		return Util.arrayCopy(source, sourceOffset, getMemory(), getInMemoryOffset(bodyOffset), length);
	}

	/**
	 * Read bytes from the file.
	 */
	public short read(short bodyOffset, byte[] output, short outputOffset, short length) {
		return Util.arrayCopy(getMemory(), getInMemoryOffset(bodyOffset), output, outputOffset, length);
	}
}
