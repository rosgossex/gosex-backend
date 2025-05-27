# GoSex backend

## Development

**Build and run:**

```shell
./gradlew clean run
```

**Format:**

```shell
./gradlew ktfmtFormat     
```


## Deployment

```shell
docker compose --env-file .env.dev up -d --wait --build
```
