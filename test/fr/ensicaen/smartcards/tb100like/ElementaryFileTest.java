package fr.ensicaen.smartcards.tb100like;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElementaryFileTest {

    private static final byte[] DF_HEADER = new byte[]{(byte) 0x3F, (byte) 0x00, (byte) 0x02, (byte) 0xA1, (byte) 0xFF,
            (byte) 0xFF, (byte) 0x9E, (byte) 0x81};
    private static final byte[] EF_HEADER = new byte[]{(byte) 0X6F, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0x83};

    @Test
    void search2BytesInFile() {
        DedicatedFile df = new DedicatedFile(new FileSystem((short) 0x100, (byte) 2, (byte) 3));
        df.setup(null, (short) 0, (short) 0x100, DF_HEADER, (short) 0, (short) DF_HEADER.length);
        ElementaryFile ef = df.createElementaryFile((short) 0x10, (short) 0x20, EF_HEADER, (short) 0, (short) EF_HEADER.length);

        byte[] source = new byte[]{
                (byte) '1', (byte) '2', (byte) '3', (byte) '4'
        };
        short sourceOffset = (short) 0x00;
        short length = (short) ((source.length - sourceOffset) >> 2);
        short offset = (short) 0x02;
        ef.write(source, sourceOffset, offset, length);

        byte[] aValue = new byte[]{
                (byte) 'X', (byte) 'Y', (byte) '1', (byte) '2',
                (byte) '3', (byte) '4', (byte) '5', (byte) '6'
        };
        assertEquals((short) 0x02, ef.search((short) 0x00, aValue, (short) 2, (short) 2));

        assertEquals((short) -1, ef.search((short) 0x03, aValue, (short) 2, (short) 2));

        byte[] otherValue = new byte[]{
                (byte) '2', (byte) '1'
        };
        assertEquals((short) -1, ef.search((short) 0, otherValue, (short) 0, (short) 2));
    }
}
