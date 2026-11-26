package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class VowelConsonantCountTest {

    public int[] countVowelsConsonants(String s) {
        int vowels = 0, consonants = 0;
        s = s.toLowerCase();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= 'a' && ch <= 'z') {
                if ("aeiou".indexOf(ch) >= 0) vowels++;
                else consonants++;
            }
        }
        return new int[] { vowels, consonants };
    }

    @Test
    public void testVowelConsonantCount() {
        int[] r = countVowelsConsonants("hello");
        assertEquals(2, r[0]);
        assertEquals(3, r[1]);
    }

    @Test
    public void testOnlyVowels() {
        int[] r = countVowelsConsonants("aeiou");
        assertEquals(5, r[0]);
        assertEquals(0, r[1]);
    }

    @Test
    public void testOnlyConsonants() {
        int[] r = countVowelsConsonants("bcdf");
        assertEquals(0, r[0]);
        assertEquals(4, r[1]);
    }
}
