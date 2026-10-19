package io.github.alianos.normaliser.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WordMatchStrategyTest {

    private final WordMatchStrategy strategy = new WordMatchStrategy();

    @ParameterizedTest(name = "{0} vs {1} -> {2}")
    @CsvSource({
        "Java engineer,        Software engineer, 0.5",
        "Chief Accountant,     Accountant,        1.0",
        "chief ACCOUNTANT,     Accountant,        1.0",
        "Plumber,              Accountant,        0.0",
        "engineer engineer,    Software engineer, 0.5",
        "Software-engineer,    Software engineer, 1.0",
        "'Engineer, Software', Software engineer, 1.0",
        "Accountant(London),   Accountant,        1.0",
        "(London) Accountant,  Accountant,        1.0",
    })
    void scoresTheFractionOfTargetWordsFoundInTheInput(String input, String target, double expected) {
        assertEquals(expected, strategy.score(input, target));
    }

    @Test
    void scoresZeroForATargetWithNoWords() {
        assertEquals(0.0, strategy.score("Accountant", "-"));
    }
}
