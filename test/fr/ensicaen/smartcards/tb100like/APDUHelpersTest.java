package fr.ensicaen.smartcards.tb100like;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class APDUHelpersTest {
    private final byte[] shortLcCommand = {(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x3F, (byte) 0x00, (byte) 0x00};
    private final byte[] extendedLcCommand = {(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x3F, (byte) 0x00, (byte) 0x00};
    private APDU apdu;

    void APDUHelpersTest() {
    }

    @Test
    void getIncomingShortLength() {
        apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(shortLcCommand);

        short lc = APDUHelpers.getIncomingLength(apdu);

        assertEquals((short) 2, lc);
    }

    @Test
    void getIncomingExtendedLength() {
        apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(extendedLcCommand);

        short lc = APDUHelpers.getIncomingLength(apdu);

        assertEquals((short) 0x0102, lc);
    }

    @Test
    void getOffsetCdataWithShortLc() {
        apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(shortLcCommand);

        short offset = APDUHelpers.getOffsetCdata(apdu);

        assertEquals(ISO7816.OFFSET_CDATA, offset);
    }

    @Test
    void getOffsetCdataWithExtendedLc() {
        apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(extendedLcCommand);

        short offset = APDUHelpers.getOffsetCdata(apdu);

        assertEquals((short) (ISO7816.OFFSET_CDATA + 2), offset);
    }
}