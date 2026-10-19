package io.github.alianos.normaliser.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevenshteinWordStrategyTest {

    private final LevenshteinWordStrategy strategy = new LevenshteinWordStrategy();

    @ParameterizedTest(name = "{0} vs {1} -> {2}")
    @CsvSource({
        "Accountant,           Accountant,        1.0",
        "chief ACCOUNTANT,     Accountant,        1.0",
        "'Engineer, Software', Software engineer, 1.0",
        "Acountant,            Accountant,        0.9",
        "Accountants,          Accountant,        0.9090909090909091",
        "Architekt,            Architect,         0.8888888888888888",
        "Sofware engineer,     Software engineer, 0.9375",
        "Acountent,            Accountant,        0.8",
        "Architecture,         Architect,         0.75",
        "Java engineer,        Software engineer, 0.5",
        "Engineering,          Software engineer, 0.0",
        "Plumber,              Accountant,        0.0",
    })
    void scoresTheAverageBestWordSimilarityAboveTheFloor(String input, String target, double expected) {
        assertEquals(expected, strategy.score(input, target), 1e-9);
    }

    @Test
    void scoresZeroForATargetWithNoWords() {
        assertEquals(0.0, strategy.score("Accountant", "-"));
    }

    @Test
    void scoresZeroForAnInputWithNoWords() {
        assertEquals(0.0, strategy.score("!!!", "Accountant"));
    }
}
