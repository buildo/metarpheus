# metarpheus

A JS API for [`metarpheus`](https://github.com/buildo/metarpheus).

It exposes a single function:

- `run(paths: string[], config?: Config): API`

where:

- `paths` is a list of **absolute** paths to analyze (they can be files or directories)
- `config` [optional] is a configuration with the following (optional) keys:
  - `modelsForciblyInUse`: a list of models that are included even if unused by the exposed API.

and `API` is an object composed by two fields:

- `models`: a list of models exposed by the API (or forcibly included, see above)
- `routes`: a description of the HTTP `routes` available

Example:

```js
const { run } = require('metarpheus');

const paths = [
  '/Users/example/buildo/project/api/src/main/scala'
];

const config = {
  modelsForciblyInUse: ['Swan']
};

const { models, routes } = run(paths, config);

console.log(JSON.stringify(models, null, 2), JSON.stringify(routes, null, 2));
```
