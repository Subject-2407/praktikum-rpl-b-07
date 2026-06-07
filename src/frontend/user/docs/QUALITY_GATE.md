# Quality Gate

These checks should pass before merging feature work:

```bash
./gradlew spotlessCheck
./gradlew detekt
./gradlew allTests
./gradlew check
```

Coverage targets from `AGENTS.md`:

- `domain`: 80 percent line coverage.
- `data/repository`: 70 percent line coverage.
- `data/remote`: 60 percent line coverage.
- `presentation/viewmodel`: 70 percent line coverage.
- Unit-testable platform logic: 60 percent line coverage.

Kover is wired but threshold verification should be enabled after real packages and tests exist,
so the first feature PR can set stable filters instead of fighting empty-package noise.
