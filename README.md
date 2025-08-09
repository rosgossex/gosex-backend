# GoSex backend

## Development

**Build and run with hot reload:**

In first shell run gradle to continuous build your project

```shell
./gradlew -t build -x test -x check -i
```

In another terminal run the application

```shell
./gradlew run -Dio.ktor.development=true
```

**Format:**

```shell
./gradlew ktfmtFormat
```

**Pre-commit Hooks:**

Install pre-commit to automatically format code before commits:

```shell
pip install pre-commit
pre-commit install
```

The hooks will automatically run `ktfmtFormat` before each commit. To run manually:

```shell
pre-commit run --all-files
```

To bypass hooks (not recommended):

```shell
git commit --no-verify
```

## Testing

**Run all tests:**

```shell
./gradlew test
```

**Generate coverage report:**

```shell
./gradlew test jacocoTestReport
```

**View coverage report:**

Open `build/reports/jacoco/test/html/index.html` in your browser.

**Coverage verification (50% minimum):**

```shell
./gradlew jacocoTestCoverageVerification
```

## Deployment

```shell
docker compose --env-file .env.dev up -d --wait --build
```
