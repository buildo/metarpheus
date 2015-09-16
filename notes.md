## Notes

The collection-like API is really handy - I was in fact thinking of writing a similar helper and then realised it was already available.

The only approach I could think of for searching for the (scaladoc) comment attached to a certain definition is somewhat awkward. I scan the token backwards until I hit something that's not a comment or whitespace (which would likely be another definition).

```scala
t : Defn.Val

val emptyTokens = Set(" ", "\\n", "comment")

// search for the comment associated with this definition
val tokenIdx = source.tokens.indexOf(t.tokens(0))
val comment = source.tokens.take(tokenIdx).reverse
  .takeWhile(c => emptyTokens.contains(c.name))
  .find(_.name == "comment")
  .map(c => stripCommentMarkers(c.code).trim)
```

The syntactic API is not incredibly ergonomic for cases like the following, where an attribute with and without params has two (or more) different possible forms. I assume that the semantic API would be the way to go here.

```scala
source: Source

source.topDownBreak.collect {
  case x: Defn.Val if x.mods.collectFirst {
    case Mod.Annot(
      Ctor.Ref.Name("publishroute") |
      Term.Apply(
        Ctor.Ref.Name("publishroute"),
        _
      )
    ) => ()
  }.isDefined => x
}
```
