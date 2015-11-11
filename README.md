# ecore2thrift
This is a utility for generating a [Thrift](http://thrift.apache.org/) IDL file from an annotated [Ecore](http://www.eclipse.org/modeling/emf/) metamodel.

It is written in EGL from the [Epsilon](http://www.eclipse.org/epsilon/) project.

To install, please use this update site:

    http://bluezio.github.io/ecore2thrift/updates

## Known problems
* Thrift cannot have any method names that are Thrift reserved words, even if they are not reserved words in the language you are using Thrift to generate.
  
  This includes, for example, "list" and "delete".
* It is impossible to add an annotation to an item in a throws list in EMF, so we cannot generate numbers on exceptions in this way.

## Other info
If you need to change the number of a numbered element, add a `@thrift(n=<num>)` annotation in the .emf file, for example `@thrift(n="3")`
