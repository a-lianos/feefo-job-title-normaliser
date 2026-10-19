package io.github.alianos.normaliser;

import java.util.List;

/** Prints the normalised title for each input title given as an argument, or for a few examples. */
public class Main {

    private static final List<String> EXAMPLE_INPUT_TITLES = List.of(
            "Java engineer",
            "C# engineer",
            "Chief Accountant",
            "Acountant",
            "Engineer, Software",
            "Architects",
            "quantity surveyor",
            "Gardener",
            "Civil engineer",
            "Software Architect",
            "");

    public static void main(String[] args) {
        Normaliser normaliser = new Normaliser();
        List<String> inputTitles = args.length > 0 ? List.of(args) : EXAMPLE_INPUT_TITLES;
        for (String inputTitle : inputTitles) {
            String normalisedTitle = normaliser.normalise(inputTitle);
            System.out.printf("%-22s -> %s%n", "\"" + inputTitle + "\"",
                    normalisedTitle.isEmpty() ? "(no match)" : normalisedTitle);
        }
    }
}
