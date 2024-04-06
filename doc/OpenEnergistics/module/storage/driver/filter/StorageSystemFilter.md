# StorageSystemFilter Driver
This driver provides support to a few of storage system filter, 
including exact matching and fuzzy matching.

This driver is dependent on [SM]. It uses the registration information
inside [SM] to find storage system available for usage and how they 
should be used. It uses naive iteration methods to achieve its functionality.
Therefore, the storage system it operates on must be continuous, an array
of [0:n] where n is the return value of `getSlotNum()`.

[SM]: ../../StorageSystemManager.md