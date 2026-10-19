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

## Decisions

- The input is split into words once per target, not once per `normalise` call. An earlier commit
  split it once per call, which was a single cheap line while scoring lived in `Normaliser`.
  Extracting `ScoringStrategy` undid that on purpose. Keeping it would have meant either passing
  pre-split words through the interface, which ties every strategy to word matching, or adding a
  per-input preparation step to the interface. With four short targets the saving isn't worth any
  interface complexity at this stage.

## Limits and next steps

- The no-match threshold is fixed in `Normaliser` (q > 0.0), but different strategies may need
  different thresholds. For example, a fuzzy strategy that rarely returns exactly 0.0 would almost
  never report no match.
- Locale and Unicode handling is out of scope for now. Lowercasing uses `Locale.ROOT`, so the JVM's
  default locale doesn't change results, but there is nothing beyond that: non-ASCII whitespace
  such as a non-breaking space doesn't separate words, accents aren't folded ("Ingénieur" doesn't
  match "ingenieur"), composed and decomposed forms of the same character aren't unified, case
  folding is plain lowercasing (so "STRASSE" doesn't match "Straße"), and non-English titles aren't
  translated.
