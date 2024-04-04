# MachineType

This class abstract a type of machine. 

For example, it could be a electric blast furnace.
But an actual electric blast furnace is a highly-customized, it could have multiple item or
fluid input and output. In addition to that, all these i/o block could be in different
positions. So in fact, a machine type only represent a outer interface, to an extent, it's
quite similar to the recipe we see when pressing r with NEI/JEI. Besides, if we want to
allocate machines at runtime, it's better if we can handle different setup of the same
*machine type*. That's another reason why it's designed this way. However if you have two
blast furnace but only one of them can handle fluid, you'd better make them of different
machine types.

### Technical Details
All operation on an actual machine should be done through its machine type. And all these
operations' machine type interface should look like `machine_type.op(machine:Machine, arg1, arg2, ...)`
Inside the implementation, the op() method should check the metadata inside the machine object
and then conduct the operation accordingly.

In most cases, it's just a class with multiple getter methods.
Take furnace as an example, you probably would have three methods:
`getIngredientTarget()`, `getFuelTarget()` and `getProductTarget`.