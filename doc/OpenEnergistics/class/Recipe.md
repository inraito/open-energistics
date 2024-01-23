# Recipe

This class abstract a recipe. And apis here are not intended for common users.
They should use other wrapped API instead.   

As it is said in their respective docs, Target is merely a predicate acting
as AOV node to calculate the route, and process is merely the code that will
be run when actually *crafting* the recipe. It's obvious that this class should
be constructed at runtime, and to be more precisely, according to pre-set
configurations and runtime states such as machine allocating status.
### Property
process: [Process](process/Process.md)  
ingredient: [Target](target/Target.md)  
product: [Target](target/Target.md)  