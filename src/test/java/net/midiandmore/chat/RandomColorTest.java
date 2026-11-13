package net.midiandmore.chat;

import java.util.HashSet;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RandomColorTest {

    @Test
    public void generatedColorsAreVariedAndReadableOnDarkSurfaces() {
        var colors = new HashSet<String>();
        var random = new Random(7);
        String[] blocked = {"?", "?", "?", "?", "?", "?"};
        for (int i = 0; i < 1000; i++) {
            String color = Util.createRandomColor(random, blocked);
            assertTrue(color.matches("[0-9A-F]{6}"));
            for (String background : new String[] {"111827", "1F2937", "273449", "374151"}) {
                assertTrue(color + " on " + background, contrast(color, background) >= 4.5);
            }
            colors.add(color);
        }
        assertTrue("Generated colors should be varied", colors.size() > 900);
    }

    @Test
    public void respectsDigitRestrictionsEvenForSinglePossibleColor() {
        String[] blocked = {"0123456789ABCDE", "0123456789ABCDE",
                "0123456789ABCDE", "0123456789ABCDE",
                "0123456789ABCDE", "0123456789ABCDE"};
        assertEquals("FFFFFF", Util.createRandomColor(new Random(1), blocked));
    }

    @Test
    public void restrictedColorsRemainReadable() {
        String[] blocked = {"ABCDEF", "0123456789", "01234567", "?", "89ABCDEF", "?"};
        var random = new Random(2);
        for (int i = 0; i < 100; i++) {
            String color = Util.createRandomColor(random, blocked);
            for (int digit = 0; digit < color.length(); digit++) {
                assertTrue(blocked[digit].indexOf(color.charAt(digit)) < 0);
            }
            assertTrue(color, contrast(color, "374151") >= 4.5);
        }
    }

    @Test
    public void rejectsRestrictionsThatMakeReadableColorsImpossible() {
        String[] blocked = {"123456789ABCDEF", "123456789ABCDEF",
                "123456789ABCDEF", "123456789ABCDEF",
                "123456789ABCDEF", "123456789ABCDEF"};
        var error = assertThrows(IllegalStateException.class,
                () -> Util.createRandomColor(new Random(1), blocked));
        assertTrue(error.getMessage().contains("bright_0"));

        blocked[0] = "0123456789ABCDEF";
        assertThrows(IllegalStateException.class, () -> Util.createRandomColor(new Random(1), blocked));
    }

    private static double contrast(String foreground, String background) {
        return (luminance(foreground) + 0.05) / (luminance(background) + 0.05);
    }

    private static double luminance(String color) {
        return 0.2126 * channel(color.substring(0, 2))
                + 0.7152 * channel(color.substring(2, 4))
                + 0.0722 * channel(color.substring(4, 6));
    }

    private static double channel(String hex) {
        double value = Integer.parseInt(hex, 16) / 255.0;
        return value <= 0.04045 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }
}
