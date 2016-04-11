# Metarpheus

[![Build Status](https://travis-ci.org/buildo/metarpheus.svg?branch=master)](https://travis-ci.org/buildo/metarpheus)

*Metarpheus* sifts through scala source files and extracts models (represented in scala as `case class`es) and specifically formatted [spray](http://spray.io/) routes (marked with @publishroute). At the current stage, it will output a json-based representation of models and APIs that can be subsequently transformed in:

- javascript type definition (e.g. [flow](http://flowtype.org/), [tcomb](http://gcanti.github.io/tcomb/))
  - [metarpheus-tcomb](https://github.com/buildo/metarpheus-tcomb)
- language-agnostic representation (e.g. [Swagger](http://swagger.io/))
  - [metarpheus-swagger](https://github.com/buildo/metarpheus-swagger)
- human-readable API documentation

## Usage

Refer to the tests for examples of the required spray route formatting. More extensive documentation is a work in progress (along with an effort of open-sourcing our internal set of conventions and support libraries for spray-based servers).

See also [this README](https://github.com/buildo/metarpheus/blob/giogonzo-readme-additions/extractor/README.md)
