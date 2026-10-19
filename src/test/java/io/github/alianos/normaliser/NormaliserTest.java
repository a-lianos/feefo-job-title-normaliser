package io.github.alianos.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NormaliserTest {

    private static final List<String> TARGETS = List.of("Alpha", "Beta", "Gamma");

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "Java engineer,        Software engineer",
        "C# engineer,          Software engineer",
        "Accountant,           Accountant",
        "Chief Accountant,     Accountant",
        "Software-engineer,    Software engineer",
        "'Engineer, Software', Software engineer",
        "Accountant (London),  Accountant",
        //typos
        "Acountant,            Accountant",
        "Sofware engineer,     Software engineer",
        "Architekt,            Architect",
        "Architects,           Architect",
        "Accountants,          Accountant",
        //word mapping
        "Python developer,     Software engineer",
        "Programmer,           Software engineer",
        "QS,                   Quantity surveyor",
        "ACCA,                 Accountant",
    })
    void normalisesTypicalTitles(String input, String expected) {
        assertEquals(expected, new Normaliser().normalise(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Plumber", "!!!", "Engineering", "C-suite executive"})
    void returnsEmptyWhenNoTargetMatches(String input) {
        assertEquals("", new Normaliser().normalise(input));
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void returnsEmptyForNullOrBlankInputWithoutCallingTheStrategy(String input) {
        Normaliser normaliser = new Normaliser(TARGETS, (in, target) -> {
            throw new AssertionError("strategy called with \"" + in + "\"");
        });

        assertEquals("", normaliser.normalise(input));
    }

    @Test
    void returnsTheConfiguredTargetsCasing() {
        Normaliser normaliser = new Normaliser(List.of("Software engineer"), (input, target) -> 1.0);

        assertEquals("Software engineer", normaliser.normalise("SOFTWARE ENGINEER"));
    }

    @Test
    void ignoresChangesToTheTargetListAfterConstruction() {
        List<String> targets = new ArrayList<>(List.of("Alpha"));
        Normaliser normaliser = new Normaliser(targets, (input, target) -> 1.0);

        targets.set(0, "Beta");

        assertEquals("Alpha", normaliser.normalise("anything"));
    }

    @Test
    void rejectsANullStrategy() {
        assertThrows(NullPointerException.class, () -> new Normaliser(TARGETS, null));
    }

    @Test
    void rejectsAnEmptyTargetList() {
        assertThrows(IllegalArgumentException.class, () -> new Normaliser(List.of(), (input, target) -> 1.0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsABlankTarget(String blank) {
        List<String> targets = List.of("Alpha", blank);

        assertThrows(IllegalArgumentException.class, () -> new Normaliser(targets, (input, target) -> 1.0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1, Double.NaN})
    void throwsWhenTheStrategyReturnsAScoreOutsideTheRange(double q) {
        Normaliser normaliser = new Normaliser(TARGETS, (input, target) -> q);

        assertThrows(IllegalStateException.class, () -> normaliser.normalise("anything"));
    }
}
