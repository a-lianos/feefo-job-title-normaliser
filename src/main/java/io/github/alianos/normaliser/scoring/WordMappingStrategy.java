package io.github.alianos.normaliser.scoring;

import static io.github.alianos.normaliser.scoring.Tokeniser.words;
import static java.util.Map.entry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Adds the target words that input words stand for to the input, then lets another strategy score
 * it. For example, with "python" mapped to "software", "Python engineer" is scored as
 * "Python engineer software". Input words must match a mapping key exactly, ignoring case.
 * Unlike title words, mapping words keep '#' and '+', so "C#" and "C++" can be mapped without
 * also mapping a lone "C".
 */
public class WordMappingStrategy implements ScoringStrategy {

    private static final Map<String, String> DEFAULT_MAPPING = Map.ofEntries(
            // specialisms
            entry("java", "software"),
            entry("c#", "software"),
            entry("c++", "software"),
            entry("python", "software"),
            entry("javascript", "software"),
            entry("typescript", "software"),
            entry("frontend", "software"),
            entry("backend", "software"),
            entry("fullstack", "software"),
            // synonyms; "developer" alone could be a property developer, so it doesn't add "software"
            entry("programmer", "software engineer"),
            entry("coder", "software engineer"),
            entry("developer", "engineer"),
            // abbreviations
            entry("qs", "quantity surveyor"),
            entry("swe", "software engineer"),
            entry("aca", "accountant"),
            entry("acca", "accountant"));

    private final Map<String, String> mapping;
    private final ScoringStrategy delegate;

    /** Creates a strategy with the default mapping for the default target titles. */
    public WordMappingStrategy(ScoringStrategy delegate) {
        this(DEFAULT_MAPPING, delegate);
    }

    /**
     * Creates a strategy with the given mapping. Each key is an input word, and its value is the
     * space-separated target words it also counts as.
     *
     * @throws NullPointerException if the mapping or the delegate is null
     * @throws IllegalArgumentException if a key isn't exactly one word or a value has no words
     */
    public WordMappingStrategy(Map<String, String> mapping, ScoringStrategy delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        Objects.requireNonNull(mapping, "mapping");
        Map<String, String> wordMapping = new HashMap<>();
        mapping.forEach((key, value) -> {
            List<String> keyWords = mappingWords(key);
            List<String> valueWords = words(value);
            if (keyWords.size() != 1) {
                throw new IllegalArgumentException("mapping key must be one word: \"" + key + "\"");
            }
            if (valueWords.isEmpty()) {
                throw new IllegalArgumentException("mapping value must not be blank for key \"" + key + "\"");
            }
            wordMapping.merge(keyWords.getFirst(), String.join(" ", valueWords),
                    (existing, added) -> existing.equals(added) ? existing : existing + " " + added);
        });
        this.mapping = Map.copyOf(wordMapping);
    }

    @Override
    public double score(String inputTitle, String targetTitle) {
        StringBuilder mappedTitle = new StringBuilder(inputTitle);
        for (String inputWord : mappingWords(inputTitle)) {
            String mappedWords = mapping.get(inputWord);
            if (mappedWords != null) {
                mappedTitle.append(' ').append(mappedWords);
            }
        }
        return delegate.score(mappedTitle.toString(), targetTitle);
    }

    private static List<String> mappingWords(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}#+]+"))
                .filter(word -> !word.isEmpty())
                .toList();
    }
}
