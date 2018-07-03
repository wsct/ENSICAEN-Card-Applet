package fr.ensicaen.smartcards.tb100like;

import fr.ensicaen.smartcards.helpers.ApduBuffer;
import javacard.framework.APDU;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TB100LikeTest {
    @Test
    void processAppletSelection() {
        ApduBuffer buffer = new ApduBuffer("00A404000B");
        APDU apdu = mock(APDU.class);
        when(apdu.getBuffer()).thenReturn(buffer.get());

        TB100Like applet = new TB100Like();
        applet.processAppletSelection(apdu);

        // Intercept and verify outgoing buffer parameters
        ArgumentCaptor<Short> offset = ArgumentCaptor.forClass(Short.class);
        ArgumentCaptor<Short> length = ArgumentCaptor.forClass(Short.class);
        verify(apdu).setOutgoingAndSend(offset.capture(), length.capture());
        assertEquals((short) 0, (short) offset.getValue());
        assertEquals((short) Constants.MF_HEADER.length, (short) length.getValue());

        // Verify APDU response content
        assertArrayEquals(Constants.MF_HEADER, buffer.getRange(length.getValue()));
    }
}
