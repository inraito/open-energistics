# Target

From the synthetic route calculation perspective, this class abstract a node on a AOV graph.
And from the implementation perspective, this class is only a predicate, judging the whole
system and environment and determining whether this target(or requirement) is satisfied.

The simplest form of Target is ItemStack. In this case, it
check whether the given ItemStack present or not. However, 
Target can be more complicated stuffs, otherwise we would 
just call it Item or Fluid. The gist of Target is to provide
more direct flexibilities instead of using some ItemStack as
marks, which is how AE2 deals with it.

Do note that targets are independent. Side effects will lead to 
unexpected failure.

+ [StackedTarget](./StackedTarget.md)
+ [StateTarget](./StateTarget.md)

### API
`check():boolean` --true if satisfied, false otherwise