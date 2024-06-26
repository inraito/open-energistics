# Storage System
Storage system is general abstraction we use to cope with heterogeneity between different
ItemStack containers. It's actually a standard on components, every component that follows this standard
could be called storage system and could be used by [Open Energistics].

A storage system would manage ItemStacks in it as itself wishes, but it should respond to [Open Energistics]'s and
others' request according to the following protocols. Note that these protocols are basically implemented inside the
`onMessage()` method in `li.cil.oc.api.network.Environment`.

---
Methods described here should be interpreted as pseudo code(java style), not its actual format.
And as you can see, it's literally the same as ItemHandler of forge.

`int getSlotNum()`  
`ItemStack push(int slot, ItemStack itemStack[, boolean atomic])`  
`ItemStack pop(int slot, int num)` `num=-1` for pop all  
`ItemStack_READ check(int slot)`  

And apart from these methods, a storage system must also respond to echo messages, allowing 
itself to be discovered by broadcasting. The responses returned could contain information
needed by [StorageSystemFilter driver](./driver/filter/StorageSystemFilter.md), which would help
it to function without a registration mechanic.
---

### Future Prospect
Adding more efficient type of storage system. These storage systems should be more similar to
database, instead of a giant chest.

[Open Energistics]: ../../OpenEnergistics.md