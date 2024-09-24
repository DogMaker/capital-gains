### Captal-gains
This code implements a CLI that processes input from stdin to perform calculations on buy/sell operations, with each operation provided on independent lines from a text file. The purpose of the code is to calculate taxes for these buy/sell operations and display the results in the same order as the input, by stdout
### Installation
- To install and run the project you can use [Docker](https://www.docker.com/products/docker-desktop/) to save time with configuration environment.

### Running the application with Docker

Run the application locally using Docker:

#### Build the application

```bash
docker build -t capital-gains .
```

#### Run the application

There are some examples of operations on path /files that can be used as input. Fell free to use them.

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

We have gradle task to run testing. The following command runs the application build and tests:

```bash
./gradlew clean build test
```

### Dependencies

* **Gradle**: Dependency manager
* **Jackson**: JSON serialization/deserialization
* **kotlin-stdlib**: Kotlin standard library
* **Docker**: Handle the tedious setup