# Configuration

A configuration can be provided via `--config`. The file is expected to contain a valid scala expression that typechecks to an instance of the `Config` trait (in `config.scala`). See `fixtures/Config.scala` for an example.

# Build

To obtain a `.jar`:
```sh
cd extractor
sbt assembly
```
