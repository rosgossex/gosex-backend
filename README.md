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


## Deployment

```shell
docker compose --env-file .env.dev up -d --wait --build
```
