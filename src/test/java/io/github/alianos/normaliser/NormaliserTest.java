package io.github.alianos.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class NormaliserTest {

    private static final List<String> TARGETS = List.of("Alpha", "Beta", "Gamma");

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
    void highestScoreWins() {
        Normaliser normaliser = new Normaliser(TARGETS, (input, target) -> target.equals("Beta") ? 0.9 : 0.1);

        assertEquals("Beta", normaliser.normalise("anything"));
    }

    @Test
    void tiesGoToTheFirstTargetInListOrder() {
        Normaliser normaliser = new Normaliser(TARGETS, (input, target) -> target.equals("Alpha") ? 0.1 : 0.5);

        assertEquals("Beta", normaliser.normalise("anything"));
    }

    @Test
    void returnsEmptyWhenTheBestScoreIsZero() {
        Normaliser normaliser = new Normaliser(TARGETS, (input, target) -> 0.0);

        assertEquals("", normaliser.normalise("anything"));
    }

    @Test
    void returnsTheConfiguredTargetsCasing() {
        Normaliser normaliser = new Normaliser(List.of("Software engineer"), (input, target) -> 1.0);

        assertEquals("Software engineer", normaliser.normalise("SOFTWARE ENGINEER"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1, Double.NaN})
    void throwsWhenTheStrategyReturnsAScoreOutsideTheRange(double q) {
        Normaliser normaliser = new Normaliser(TARGETS, (input, target) -> q);

        assertThrows(IllegalStateException.class, () -> normaliser.normalise("anything"));
    }
}
