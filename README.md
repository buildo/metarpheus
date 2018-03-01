# Metarpheus

[![Build Status](https://drone.our.buildo.io/api/badges/buildo/metarpheus/status.svg)](https://drone.our.buildo.io/buildo/metarpheus)

*Metarpheus* sifts through scala source files and extracts models (represented in scala as `case class`es) and specifically formatted [spray](http://spray.io/) routes or [Wiro](https://buildo.github.io/wiro/) operations. At the current stage, it will output a json-based representation of models and APIs that can be subsequently transformed in:

- javascript type definitions (e.g. [TypeScript](http://www.typescriptlang.org/), [flow](http://flowtype.org/), [tcomb](http://gcanti.github.io/tcomb/))
  - [metarpheus-io-ts](https://github.com/buildo/metarpheus-io-ts)
  - [metarpheus-tcomb](https://github.com/buildo/metarpheus-tcomb)
- javascript http clients
  - [metarpheus-js-http-api](https://github.com/buildo/metarpheus-js-http-api)
- language-agnostic representation (e.g. [Swagger](http://swagger.io/))
  - [metarpheus-swagger](https://github.com/buildo/metarpheus-swagger)
- human-readable API documentation

## Usage

Refer to the tests for examples of the required spray route formatting. More extensive documentation is a work in progress.
