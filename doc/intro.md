# How to make this thing run?

Clone the repo, `cd` inside. Now there are generally 2 options:

1. REPL way
 ```
 lein run
 ```
 In this case, blogiseq looks into the `resources/` directory in the folder and
 server resources from there.

2. Self-contained way
 ```
 lein uberjar
 ```
 This will generate a jar in the `target/` dir which can be run as follows:
 ```
 java -jar blogiseq-0.1.0-standalone.jar # replace with your standalone file :)

 ```
 In this case it is important that you have the `resources/` directory populated
 with meaningful stuff (see the `resources/` directory in the project repository
 for examples).

# Required files
They are under the `required/` directory:
* `index.md` - main page
* `left-column.md` - the left column :)

# The `resources/` directory structure
blogiseq is just a bunch of code that reads stuff in `resources/` dir and
presents it in the form of a web page.

## `meta.edn`
File containing the metadata about resources. Currently it is used for
generating the "right-hand-side" menu).

## `resources/articles`
`blogiseq` parses markdown-formatted files (articles) in this directory.
Including images (or other resources) in the arcticles is supported.

For examples (hopefully self-explanatory), please refer to the `resources/` dir
in the project sources.

