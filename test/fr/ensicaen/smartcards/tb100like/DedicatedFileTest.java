package fr.ensicaen.smartcards.tb100like;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DedicatedFileTest {

    private static final byte[] DF_HEADER = new byte[]{(byte) 0x3F, (byte) 0x00, (byte) 0x02, (byte) 0xA1, (byte) 0xFF,
            (byte) 0xFF, (byte) 0x9E, (byte) 0x81};
    private static final byte[] EF_HEADER = new byte[]{(byte) 0X6F, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0x83};

    @Test
    void createElementaryFile() {
        DedicatedFile df = new DedicatedFile(new FileSystem((short) 0x100, (byte) 5, (byte) 5));
        df.setup(null, (short) 0, (short) 0x100, DF_HEADER, (short) 0, (short) DF_HEADER.length);

        assertNull( df.getChild((byte) 0) );
        df.createElementaryFile((short) 17,(short) 16,EF_HEADER, (short) 0, (short) EF_HEADER.length );

        assertNotNull(df.getChild((byte) 0) );
        assertNull( df.getChild((byte) 1) );

        df.createElementaryFile((short) 0,(short) 16,EF_HEADER, (short) 0, (short) EF_HEADER.length );

        File child0 = df.getChild((byte) 0);
        File child1 = df.getChild((byte) 1);

        assertNotNull(child0);
        assertNotNull(child1);
        assertNull( df.getChild((byte) 2) );

        assertTrue( child0._inParentBodyOffset < child1._inParentBodyOffset );
    }
}
