# Job Title Normaliser

Java library for a technical assessment: `Normaliser.normalise(String)` maps a free-text job title
to the best-matching target title ("Architect", "Software engineer", "Quantity surveyor",
"Accountant"), using an internal quality score q in [0.0, 1.0].

Maven project, JDK 25, JUnit 5. Base package `io.github.alianos.normaliser`.

## Glossary

- **Input title**: the free-text job title passed to `normalise` (`inputTitle` in code).
- **Target title**: one of the configured titles the input can map to (`targetTitle` in code).
- **Quality score (q)**: how well an input title matches one target title, from 0.0 to 1.0,
  computed by a `ScoringStrategy`.
- **No match**: no target title scores above 0.0, or the input is null or blank. `normalise`
  returns "".

## Architecture

- `io.github.alianos.normaliser`: `Normaliser`, which selects the best target title by q.
- `io.github.alianos.normaliser.scoring`: `ScoringStrategy` (computes q) and its implementations,
  e.g. `WordMatchStrategy`.

## Commands

- `./mvnw test`: run the unit tests
- `./mvnw clean package`: full build

## Conventions

- Test first (red, green, refactor). Keep every existing test green.
- Small focused changes; one commit per step. Stage the files, but don't commit until asked.
- British spelling in names and docs (normaliser, tokeniser).
- Comments only on public interfaces or to explain a non-obvious why.
- When a step makes an assumption about behaviour, add one bullet to the README's "Assumptions"
  section in the same commit.
- A new external library needs an ADR in docs/adr/ in the same commit.
- Pragmatism over cleverness: no speculative abstractions, no features the step didn't ask for.
- Use `tmp/` (git-ignored) for scratch files.
