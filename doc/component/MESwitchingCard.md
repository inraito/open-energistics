# ME Switching Card
This component provides lua interface for manipulating storage system.

`push(addr:String, slot:int, num:int):boolean`  
`pop(addr:String, slot:int):boolean`  
Both push and pop are atomic, which means ItemStack will not be split,
they are pushed or popped *all or none*.
