/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 * Dedicated File inspired by TB100.
 */
public class DedicatedFile extends File {

	private static final byte MAX_INNER_FILES = 10;

	private final File[] _innerFiles = new File[MAX_INNER_FILES];

	private byte _innerFilesCount = 0;

	/**
	 * @param header   File header.
	 * @param bodySize Number of usable bytes in the DF (body only, without header).
	 */
	public DedicatedFile(DedicatedFile parentDF, short offset, short size, byte[] header) {
		super(parentDF, offset, size, header);
	}
}
