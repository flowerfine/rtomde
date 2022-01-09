# RTOM Data Engine
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Total Lines](https://tokei.rs/b1/github/flowerfine/rtomde?category=lines)](https://github.com/flowerfine/rtomde)
[![codecov](https://codecov.io/gh/flowerfine/rtomde/branch/master/graph/badge.svg)](https://codecov.io/gh/flowerfine/rtomde/branch/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=flowerfine_rtomde&metric=alert_status)](https://sonarcloud.io/dashboard?id=flowerfine_rtomde)

`rtomde` converts sql into RESTful, focuses on providing `Readable, Testable, Observable, Maintainable Data Engine`, which is proud for providing automatic pipeline between data warehouse and apps, commonly as data service.

Billons of datas are collected、transformed、loaded by computation engine such as flink or spark, then datas are distributed to analystis、BI and ad-hoc, also apps. All ETL、data warehouse、computation and analystis engine contributes to enterprise data pipeline, `rtomde` gives people strong but clean tools on data delivery approaches for data developers.



## Development

### Requirements

* jdk >= 11
* maven >= 3.6.3

`rtomde` uses and recommends the `IntelliJ IDEA` to develop the `rtomde` project. Minimal requirements for an IDE are java and maven supports.

### Build

```shell
./mvnw clean install
```

## Contributing

The project welcomes everyone to contribute.

## License

`rtomde` is licenced under the Apache License Version 2.0.
