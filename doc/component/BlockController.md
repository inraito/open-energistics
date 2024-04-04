# Block Controller
This component is a [storage system](../OpenEnergistics/module/storage/StorageSystem.md).
***
Component that allows full controls upon adjacent blocks.  

### ItemStack Handling
As it is said above, this component is a storage system, it sort of combined all adjacent 
ItemHandler and Inventory together. To put it more clearly, it maintains a configurable mapping
from slots in storage system to slots in adjacent blocks. And of course, all operations on
slots in storage system is actually operated on slots in adjacent blocks.
