/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

import java.lang.Override;

import javacard.framework.Util;

/**
 * Elementary File inspired by TB100.
 */
public class ElementaryFile extends File {

	/**
	 * @param parentDF Parent DF.
	 * @param offset   Start byte index of the file in parent DF data zone.
	 * @param size     Number of used bytes by the DF (body + header).
	 * @param header   File header.
	 */
	public ElementaryFile(DedicatedFile parentDF, short offset, short size, byte[] header, short headerOffset,
			short headerLength) {
		super(parentDF, offset, size, header, headerOffset, headerOffset);
	}

	//
	// >> File
	//

	/**
	 *
	 */
	protected final void clearInternals() {
		// TODO Clear used space.
	}

	/**
	 *
	 */
	public boolean isAvailable(short offset, short length) {
		// TODO Implement availability check
		return true;
	}

	/**
	 * 
	 */
	public final boolean isDF() {
		return false;
	}

	/**
	 * 
	 */
	public final boolean isEF() {
		return true;
	}

	//
	// Public Methods
	//

	/**
	 * Write bytes to the file.
	 */
	public short write(byte[] source, short sourceOffset, short offset, short length) {
		return Util.arrayCopy(source, sourceOffset, getMemory(), getInMemoryOffset(offset), length);
	}

	/**
	 * Read bytes from the file.
	 */
	public short read(short offset, byte[] output, short outputOffset, short length) {
		return Util.arrayCopy(getMemory(), getInMemoryOffset(offset), output, outputOffset, length);
	}
}
