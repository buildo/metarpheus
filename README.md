# Metarpheus

[![Build Status](https://drone.our.buildo.io/api/badges/buildo/metarpheus/status.svg)](https://drone.our.buildo.io/buildo/metarpheus)

*Metarpheus* sifts through scala source files and extracts models (represented in scala as `case class`es) and [Wiro](https://buildo.github.io/wiro/) operations.
It will output a json-based representation of models and APIs that can be subsequently transformed in:

- javascript type definitions (e.g. [TypeScript](http://www.typescriptlang.org/), [flow](http://flowtype.org/), [tcomb](http://gcanti.github.io/tcomb/))
  - [metarpheus-io-ts](https://github.com/buildo/metarpheus-io-ts)
  - [metarpheus-tcomb](https://github.com/buildo/metarpheus-tcomb)
- javascript http clients
  - [metarpheus-js-http-api](https://github.com/buildo/metarpheus-js-http-api)
- language-agnostic representation (e.g. [Swagger](http://swagger.io/))
  - [metarpheus-swagger](https://github.com/buildo/metarpheus-swagger)
- human-readable API documentation

## Usage
Metarpheus can be used either as a packaged JAR or as a Node.js npm package.

To run as jar, run

```
sbt cli/assembly
```

Then you can take the generated jar and run it with

```
java -jar metarpheus.jar --config config.json # config is optional
```

To run as npm package, refer to the [npm package README](jsFacade/npm/README.md)

