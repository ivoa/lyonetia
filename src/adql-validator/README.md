# ADQL Validation

## Preambule

### Prerequisites

* Java 8 or higher

_No need to install anything, not even Gradle._

### Compilation tool

This Java project uses [Gradle](https://gradle.org/) as compilation and dependencies management tool. There is no need to install Gradle or any dependency. This project includes 2 scripts `gradlew` (for Unix base OS) and `gradlew.bat` (for Windows) which will locally install the required version of Gradle. This operation is performed only once.

The first start of any Gradle command is always a bit slow ; the time for Gradle to initialize and refresh its cache. The subsequent Gradle commands are much faster.

### Dependencies

* [VOLLT/ADQL-Lib](http://cdsportal.u-strasbg.fr/adqltuto/)
* [JCommander](http://jcommander.org/)

### Commands

All the following commands are supposed to run from the same directory containing this `README.md` file.

If the environment variable `XML_FILE` is not set, `$XML_FILE` should be replaced in the following command strings by the list of input XML files and/or directories.



## Run the validator

* With the JAR files:

  ```bash
  # 0. If not already done, generate the JAR and its dependencies:
  ./gradlew installDist
  
  # 1. Run the validator:
  # ...on Unix based OS:
  ./build/install/adql-validation/bin/adql-validation $XML_FILE
  # ...on Windows:
  ./build/install/adql-validation/bin/adql-validation.bat $XML_FILE
  # or without the start script:
  java -jar build/install/adql-validation/lib/adql-validation-1.0-beta.jar $XML_FILE
  ```

* With Gradle:

  ```bash
  # On Unix based OS:
  ./gradlew run -q --args="$XML_FILE"
  # On Windows:
  ./gradlew.bat run -q --args="$XML_FILE"
  ```



In both case, the validator output can be saved into a file using a redirection:

```bash
./build/install/adql-validation/bin/adql-validation -f markdown $XML_FILE > result.md
# or
./gradlew run -q --args="-f markdown $XML_FILE" > result.md
```

_**Note:** Do not forget the `-q` (or `--quiet`) in the Gradle run arguments ; otherwise, the result file will be prefixed by some noisy Gradle messages._



## Validator parameters

Here is the list of all available parameters:

```bash
./gradlew run -q --args="-h"

# Usage: java -jar adql_validator.jar [OPTION]... (FILE|DIRECTORY)...
# 
# Description:
#     Validate the validation sets described in the given files. A 
#     file item can be a regular file or a directory. If a directory, all of its 
#     direct regular child files are tested ; use the recursive option to also 
#     explore its child repositories.
# 
# Options:
#     --format, -f    Report's output format. Supported values: txt (or text), 
#                     md (or markdown). (default: TEXT) (values: [TXT, TEXT, MD, 
#                     MARKDOWN]) 
#     --help, -h      Display this help.
#     --no-stats      Hide the statistics of the whole validation session. 
#                     (default: false)
#     --quiet, -q     Nothing displayed. Use the exit status code to get the 
#                     global validation result. (default: false)
#     --recursive, -r Browse directories recursively. (default: false)
#     --show-all      Display successful and failed tests. (default: false)
```



## Check the XML document

* **Unix solution:**

  ```bash
  xmllint --noout \
          --schema resources/queries.xsd \
          $XML_FILE
  ```

* **Java solution:**

  ```bash
  # On Unix based OS:
  ./gradlew run -q --args="-c $XML_FILE"
  # On Windows:
  ./gradlew.bat run -q --args="-c $XML_FILE"
  ```



## Run JUnit tests

```bash
# On Unix based OS:
./gradlew test
# On Windows:
./gradlew.bat test
```



## Build the validator

* Only the validator JAR _(no dependencies)_:

  ```bash
  # On Unix based OS:
  ./gradlew jar
  # On Windows:
  ./gradlew.bat jar
  # => result available in `build/libs/`
  ```
  
* The validator, its dependencies _(in separate JARs)_ and a start script:

  ```bash
  # On Unix based OS:
  ./gradlew installDist
  # On Windows:
  ./gradlew.bat installDist
  # => result available in `build/install/`
  ```

* A TAR/ZIP containing all the above:

  ```bash
  # On Unix based OS:
  ./gradlew assembleDist
  # On Windows:
  ./gradlew.bat assembleDist
  # => result available in `build/distributions/` ; the TAR and ZIP are the same
  ```