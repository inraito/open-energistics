# Synthetic Calculator

This module handles the calculation of synthetic route.
What it does is starting from the actual target and
finding recipes that could produce this target. And 
if the ingredient targets of those recipes are not satisfied,
we do this process again. This process happens recursively,
until we satisfy all targets, or meet a target that can't be
satisfied. Basically, it's a backtracking.

To give users control over the route calculated, we give users
the ability to insert a hook method. Specifically, we allow users
to choose any [Target] to insert a hook,
and when that very target is queried for how to meed it, the hook will
be called with appropriate context.

### Hook
Hook is a method that look like this:  
`hook(sc_context:SCContext):boolean or Process or (Recipe, batch_size, batch_num)`

+ return true if this target will be satisfied automatically, no other action needed.
+ return false if this target can't be meet or you want to block the calculator from this path.  
+ return Process if this target will be meet by doing the process returned. It's a easier way of 
`return (new Recipe(none, process, target), 1, 1)`  
+ return Recipe if you want this target be achieve by the returned recipe. The ingredient target of the recipe returned will be 
calculated by synthetic calculator, and if the batch_size is more than 1, trivial simplification will be
performed. That is, if the ingredient is stackable, we amalgamate them into a single stacked target, and if
it's a state target, we just throw away additional ones.

Creating new recipes at runtime is acceptable, but be careful about memory leak.
Also do make sure the recipe you create and return is a valid one.

### More on Default Behaviour
The synthetic calculator behaves like that for every [Target], there is one and only one
hook attached to it. There is a default one, and when users specify another custom
hook, it replaces the default one. 

And what does the default one do? Its find all recipes whose product is the target
queried, sorts them based on numbers of ingredients(ascending) and recursively tries
them.  Priority within recipes with the same number of ingredients is not in any way 
guaranteed. And the default hook will always return recipes in the form of 
`(recipe, n, 1)`, which would make the crafting running sequentially, prohibiting
parallel processing.

The default hook will simply return false to end the searching if it finds
that the current queried target is already in the stack, to avoid a loop that never
ends. 

And if a recipe is returned by hook, we would simply take it, without any 
additional check. While the default hook will guarantee that the queried [Target] be
included in the product of the returned recipe, the custom ones may not. To put it
another way, the product of the returned recipes will be ignored.

### Processing Ouroboros Recipes
When i say ouroboros recipes, i mean recipes whose ingredient and product form a loop.
For example recipe A is 1M->2N and recipe B is 1N->1M. We can notice that the two recipes
basically allows us to duplicate both M and N if we have any of them, and we can transform
them from and to each other. But it's probably the most simple case, if we have more recipes,
making the loop bigger, and we have more outside ingredient and product, identifying the 
possibilities becomes extremely difficult. The simple recursive solution may not be applicable.

Therefore, as has been said above, default behaviour of the synthetic calculator is to simply block 
those path. That's a fairly conservative solution to avoid infinite loop. And it's actually
quite complicated to calculate a valid path containing ouroboros recipes, so i wouldn't 
recommend it. But if you really want to use [OpenEnergistics] to handle it, you may achieve
that by using custom hooks, but be careful for infinite loop.

### Parallel Processing
All recipes will be processed concurrently, except those having dependency relations.

### Future Prospects
Instead of only focus on a single target each step, we consider all leaf targets that need to be
achieved through recipes. Which allows more reasonable selection of recipes, because we can take
advantages of multi-product recipes.

[Target]: ../target/Target.md
[OpenEnergistics]: ../../OpenEnergistics.md
