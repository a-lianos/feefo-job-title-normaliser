package io.github.alianos.normaliser.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WordMappingStrategyTest {

    private static final Map<String, String> MAPPING = Map.of("dev", "software engineer", "QS", "quantity surveyor");

    private String delegateInput;

    private final ScoringStrategy recordingDelegate = (input, target) -> {
        delegateInput = input;
        return 0.5;
    };

    @Test
    void passesTheInputWithMappedWordsAddedToTheDelegate() {
        new WordMappingStrategy(MAPPING, recordingDelegate).score("Senior Dev", "Software engineer");

        assertEquals("Senior Dev software engineer", delegateInput);
    }

    @Test
    void passesTheInputUnchangedWhenNoWordIsMapped() {
        new WordMappingStrategy(MAPPING, recordingDelegate).score("Gardener", "Software engineer");

        assertEquals("Gardener", delegateInput);
    }

    @Test
    void matchesMappingKeysAsWholeWordsIgnoringCase() {
        new WordMappingStrategy(MAPPING, recordingDelegate).score("qs, developer", "Quantity surveyor");

        assertEquals("qs, developer quantity surveyor", delegateInput);
    }

    @Test
    void returnsTheDelegatesScore() {
        assertEquals(0.5, new WordMappingStrategy(MAPPING, recordingDelegate).score("dev", "Software engineer"));
    }

    @Test
    void ignoresChangesToTheMappingAfterConstruction() {
        Map<String, String> mapping = new HashMap<>(MAPPING);
        WordMappingStrategy strategy = new WordMappingStrategy(mapping, recordingDelegate);

        mapping.put("gardener", "architect");
        strategy.score("Gardener", "Architect");

        assertEquals("Gardener", delegateInput);
    }

    @Test
    void rejectsANullMapping() {
        assertThrows(NullPointerException.class, () -> new WordMappingStrategy(null, recordingDelegate));
    }

    @Test
    void rejectsANullDelegate() {
        assertThrows(NullPointerException.class, () -> new WordMappingStrategy(MAPPING, null));
        assertThrows(NullPointerException.class, () -> new WordMappingStrategy(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "!!!", "two words"})
    void rejectsAKeyThatIsNotOneWord(String key) {
        Map<String, String> mapping = Map.of(key, "software");

        assertThrows(IllegalArgumentException.class, () -> new WordMappingStrategy(mapping, recordingDelegate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "!!!"})
    void rejectsABlankValue(String value) {
        Map<String, String> mapping = Map.of("dev", value);

        assertThrows(IllegalArgumentException.class, () -> new WordMappingStrategy(mapping, recordingDelegate));
    }

    @ParameterizedTest(name = "{0} vs {1} -> {2}")
    @CsvSource({
        // specialisms
        "Java engineer,        Software engineer, 1.0",
        "C# engineer,          Software engineer, 1.0",
        "C++ engineer,         Software engineer, 1.0",
        "Python developer,     Software engineer, 1.0",
        "Frontend engineer,    Software engineer, 1.0",
        "Fullstack developer,  Software engineer, 1.0",
        // synonyms
        "Programmer,           Software engineer, 1.0",
        "Coder,                Software engineer, 1.0",
        "Developer,            Software engineer, 0.5",
        // abbreviations
        "QS,                   Quantity surveyor, 1.0",
        "SWE,                  Software engineer, 1.0",
        "ACA,                  Accountant,        1.0",
        "ACCA,                 Accountant,        1.0",
    })
    void defaultMappingCoversSpecialismsSynonymsAndAbbreviations(String input, String target, double expected) {
        WordMappingStrategy strategy = new WordMappingStrategy(new LevenshteinWordStrategy());

        assertEquals(expected, strategy.score(input, target), 1e-9);
    }
}
