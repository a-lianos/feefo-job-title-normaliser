package io.github.alianos.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NormaliserTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "Java engineer,    Software engineer",
        "C# engineer,      Software engineer",
        "Accountant,       Accountant",
        "Chief Accountant, Accountant",
    })
    void normalisesTheBriefsExamples(String input, String expected) {
        assertEquals(expected, new Normaliser().normalise(input));
    }

    @Test
    void returnsEmptyWhenNoTargetMatches() {
        assertEquals("", new Normaliser().normalise("Plumber"));
    }
}
