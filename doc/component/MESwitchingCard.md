# ME Switching Card
This component provides lua interface for manipulating storage system.

`push(addr:String, slot:int, num:int):boolean`  
`pop(addr:String, slot:int):boolean`  
`peekTop():ItemStackDescriptor`  
`peek(addr:String, slot:int):ItemStackDescriptor`

`pop` is atomic(from the switching card's perspective).
Either the whole ItemStack on the top is popped out or the ItemStack
on the top remains, in other words nothing happens. It will not
split the ItemStack.

And be careful about the `push`, if the count of the ItemStack in the requested
slot is not enough, that is it's less than the given `num`, then the whole ItemStack
will be pushed into the switching card and the `push` method will return `true`.

