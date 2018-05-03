/**
 * @author ENSICAEN
 */

package fr.ensicaen.smartcards.tb100like;

/**
 * Dedicated File inspired by TB100.
 */
public class DedicatedFile extends File {

	private static final byte MAX_CHILDREN = 10;

	private final File[] _children = new File[MAX_CHILDREN];

	private byte _childrenCount = 0;

	/**
	 * @param parentDF Parent DF.
	 * @param offset   Start byte index of the file in parent DF data zone.
	 * @param size     Number of used bytes by the DF (body + header).
	 * @param header   File header.
	 */
	public DedicatedFile(DedicatedFile parentDF, short offset, short size, byte[] header) {
		super(parentDF, offset, size, header);
	}

	//
	// >> File
	//

	/**
	 *
	 */
	public final boolean isAvailable(short offset, short length) {
		for (byte i = 0; i < _childrenCount; i++) {
			if (offset < (short) (_children[i].getOffset() + _children[i].getLength())
					|| (short) (offset + length) > _children[i].getOffset()) {
				return false;
			}
		}

		return true;
	}

	/**
	 *
	 */
	public final boolean isDF() {
		return true;
	}

	/**
	 *
	 */
	public final boolean isEF() {
		return false;
	}

	//
	// > Public Methods
	//

	/**
	 * Creates a new DF starting at offset and using size bytes.
	 */
	public byte createDedicatedFile(short offset, short size, byte[] header) {
		// TODO Check space availability
		_children[_childrenCount] = new DedicatedFile(this, offset, size, header);
		_childrenCount++;
		return (byte) (_childrenCount - 1);
	}

	/**
	 * Creates a new EF starting at offset and using size bytes.
	 */
	public byte createElementaryFile(short offset, short size, byte[] header) {
		// TODO Check space availability
		_children[_childrenCount] = new ElementaryFile(this, offset, size, header);
		_childrenCount++;
		return (byte) (_childrenCount - 1);
	}

	/**
	 * Returns the nth child file. A call to hasChild should be done to verify the
	 * existence.
	 * 
	 * @param nth Index of the child (starts at 0)
	 */
	public File getChild(byte nth) {
		return _children[nth];
	}

	/**
	 * Returns true if the nth child file exists.
	 * 
	 * @param nth Index of the child (starts at 0)
	 */
	public boolean hasChild(byte nth) {
		return nth >= (short) 0 && nth < _childrenCount;
	}
}
