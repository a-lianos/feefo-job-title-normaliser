# Job Title Normaliser

A small Java library that maps free-text job titles to the closest of a fixed set of target titles
("Architect", "Software engineer", "Quantity surveyor", "Accountant"). For example, "Java engineer"
becomes "Software engineer" and "Chief Accountant" becomes "Accountant". Internally, each target is
given a quality score q between 0.0 and 1.0, and the best-scoring target wins.

## Build and test

Requires JDK 25. The Maven wrapper downloads Maven itself.

```sh
./mvnw test           # run the unit tests
./mvnw clean package  # build the jar into target/
./mvnw -q compile exec:java  # print the normalised title for a few example inputs
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Usage

```java
Normaliser n = new Normaliser();
String input = "Java engineer";
String normalisedTitle = n.normalise(input);
// output normalisedTitle
```

## Design

`Normaliser` owns selection: it asks a `ScoringStrategy` for q against each target title, picks the
highest, and applies the tie and no-match rules. The strategy owns scoring and nothing else. A new
matching algorithm is a new `ScoringStrategy` implementation passed to
`Normaliser(List<String> targetTitles, ScoringStrategy strategy)`. This split has two benefits.
First, it makes the code testable: `Normaliser`'s selection rules are pinned with stub strategies,
and each strategy's scoring is tested on its own. Second, we can benchmark different strategies
against each other while `Normaliser` itself doesn't change.

## Assumptions

Without a large database of classified example titles to test against, the matching rules below
are educated guesses rather than findings, and real data could overturn any of them.

- "Best match" means the target with the highest q. If no target scores above 0.0, `normalise`
  returns "" rather than null or an exception, so the brief's `String` signature and sample code
  work unchanged.
- Target list order sets the priority on ties: when several targets share the highest q, the first
  in the list wins, so the result is deterministic.
- Null or blank input returns "", the same as no match, rather than throwing: free-text fields are
  often empty, and normalising one shouldn't crash. `Normaliser` checks this before scoring, so
  strategies never see null or blank input.
- An empty target list or a blank target title is a configuration error, so the `Normaliser`
  constructor rejects it straight away rather than returning "" for every input later.
- Anything other than a letter or digit separates words, so "Software-engineer" and "Engineer,
  Software" both match "Software engineer"; word order doesn't matter. Symbols are dropped, so C#
  and C++ are read as C; no target title depends on them.
- A word within roughly one or two typos of a target word counts as that word, so "Acountant" and
  "Architects" both match. Precisely, word similarity is 1 - edit distance / longer word length,
  and anything below 0.75 counts as no match. This catches some related words but not others:
  "Architecture" matches Architect and "Accounting" matches Accountant, but "Engineering" doesn't
  match engineer.

## Decisions

- [ADR 0001](docs/adr/0001-use-commons-text-for-fuzzy-matching.md): use Apache Commons Text's
  Levenshtein distance for fuzzy matching.
- The input is split into words once per target, not once per `normalise` call. An earlier commit
  split it once per call, which was a single cheap line while scoring lived in `Normaliser`.
  Extracting `ScoringStrategy` undid that on purpose. Keeping it would have meant either passing
  pre-split words through the interface, which ties every strategy to word matching, or adding a
  per-input preparation step to the interface. With four short targets the saving isn't worth any
  interface complexity at this stage.

## Limits and next steps

- The no-match threshold is fixed in `Normaliser` (q > 0.0), so each strategy must return exactly
  0.0 for no match. `LevenshteinWordStrategy` does this with its own word similarity floor;
  otherwise every unrelated word would score a little and nothing would be "no match". A shared,
  configurable threshold may be worth adding if more strategies need one.
- Locale and Unicode handling is out of scope for now. Lowercasing uses `Locale.ROOT`, so the JVM's
  default locale doesn't change results, but there is nothing beyond that: accents aren't folded
  ("Ingénieur" doesn't match "ingenieur"), composed and decomposed forms of the same character
  aren't unified, case folding is plain lowercasing (so "STRASSE" doesn't match "Straße"), and
  non-English titles aren't translated.
