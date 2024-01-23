# Machine

This class abstract a *machine*, it could be a block(or TileEntity) attached to a
[Block Controller](../../component/BlockController.md), or it could be a multi-block
machine that consists of multiple blocks, all of which attached to Block Controllers.
It represents a machine ready to use but in order to use it, you should call methods
on its machine_type.
### Properties
`machine_type:MachineType` --read-only, not only the type of this machine, but also api entry  
`machine_metadata:Table` --read-only, data needed when manipulating this machine, list of block controllers and sides for example
