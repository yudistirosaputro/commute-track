# CI/CD Implementation Specification

## Requirements Summary

### Functional Requirements
1. Build validation - Verify code compiles across all 11 modules
2. Unit test execution - Run all unit tests and report results
3. Static analysis - Run Android lint checks
4. Artifact generation - Upload APK, test results, and lint reports

### Non-Functional Requirements
- Performance: Complete build + tests in <10 minutes (cached)
- Reliability: <5% flaky test rate
- Security: Validate Gradle wrapper, no secrets in logs
- Cost efficiency: Use caching to minimize GitHub Actions minutes

### Implicit Requirements
- Gradle dependency caching for speed
- Parallel job execution where possible
- Read-only cache for PRs (cache poisoning prevention)
- Automatic artifact retention (7 days)

### Out of Scope
- Release signing (deferred to Phase 2)
- Deployment to app stores (deferred to Phase 2)
- Instrumentation tests (can add later)
- Code coverage reporting (can add later)

## Technical Specification

### Tech Stack
- **Runner**: ubuntu-latest
- **JDK**: 17 (Eclipse Temurin)
- **Build Tool**: Gradle with Kotlin DSL
- **Caching**: gradle/gradle-build-action@v3

### Workflow Structure

```
.github/workflows/
└── ci.yml
```

**Triggers**:
- Push to `main` branch
- Pull requests to `main` branch

**Jobs**:
1. `validate-gradle-wrapper` - Security validation
2. `build` - Assemble debug APK (parallel)
3. `test` - Unit tests (parallel)
4. `lint` - Static analysis (parallel)

### Performance Optimizations
- Gradle dependency caching
- Build cache configuration
- Parallel job execution
- Module-level parallelism (optional)

## Implementation Plan

### File to Create
`.github/workflows/ci.yml`

### Jobs Configuration

#### 1. validate-gradle-wrapper
```yaml
validate-gradle-wrapper:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: gradle/wrapper-validation-action@v3
```

#### 2. build
```yaml
build:
  runs-on: ubuntu-latest
  needs: validate-gradle-wrapper
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: '17'
    - run: chmod +x gradlew
    - uses: gradle/gradle-build-action@v3
    - run: ./gradlew assembleDebug --stacktrace
    - uses: actions/upload-artifact@v4
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/*.apk
        retention-days: 7
```

#### 3. test
```yaml
test:
  runs-on: ubuntu-latest
  needs: validate-gradle-wrapper
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: '17'
    - run: chmod +x gradlew
    - uses: gradle/gradle-build-action@v3
    - run: ./gradlew test testDebugUnitTest --stacktrace
    - uses: actions/upload-artifact@v4
      if: always()
      with:
        name: test-results
        path: |
          **/build/test-results/test*/**/*.xml
          **/build/reports/tests/test*/
    - uses: dorny/test-reporter@v1
      if: always()
      with:
        name: Unit Tests
        path: '**/build/test-results/test*/**/*.xml'
        reporter: java-junit
        fail-on-error: true
```

#### 4. lint
```yaml
lint:
  runs-on: ubuntu-latest
  needs: validate-gradle-wrapper
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: '17'
    - run: chmod +x gradlew
    - uses: gradle/gradle-build-action@v3
    - run: ./gradlew lintDebug --stacktrace
    - uses: actions/upload-artifact@v4
      if: always()
      with:
        name: lint-reports
        path: |
          app/build/reports/lint-results-*.html
          app/build/reports/lint-results-*.xml
```

### Artifacts
- Debug APK (7-day retention)
- Test results (JUnit XML + HTML)
- Lint reports (HTML + XML)

## Success Criteria
- [ ] Workflow file created at `.github/workflows/ci.yml`
- [ ] All 4 jobs pass successfully
- [ ] Artifacts uploaded correctly
- [ ] Build completes in <10 minutes (cached)

## Future Enhancements (Phase 2)
- Instrumentation tests with emulator
- Code coverage with JaCoCo
- Deployment to Firebase App Distribution
- Signed release builds
