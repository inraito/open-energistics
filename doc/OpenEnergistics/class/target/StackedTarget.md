# Stacked Target

This type of target abstract a countable pile of resources.
When this target is realized, certain number of the resources
are generated and the same number of the resources will be 
consumed when recipe based on this target is crafted.
The resources above can be but no limited to items, fluids,
energy and so on.

### API
`currentAmount():num` --return current amount of the resources  
`neededAmount():num` --return needed amount of the resources
