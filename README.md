### Running the application

Run the application locally using Docker:

#### Build the application

```bash
docker build -t capital-gains .
```

#### Run the application

```bash
docker run -i --rm capital-gains < files/operations.txt
```
##### Windows
```bash
Get-Content files/operations.txt | docker run -i --rm capital-gains
```

or

```bash
docker run -i --rm capital-gains
```


### Testing

We have some gradle tasks to run testing. The following command runs the application build and tests:

```bash
./gradlew clean build test
```

### Dependencies

* **Gradle**: Dependency manager
* **Jackson**: JSON serialization/deserialization
* **kotlin-stdlib**: Kotlin standard library