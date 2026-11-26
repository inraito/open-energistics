# Machine Manager

This module handles the registration and allocation of machine and machine types.
### API
#### Method
`registerMachineType(type: MachineType, typeID : string)`  
`registerMachine(typeID: string, machine: Machine)`  
`listTypes() : List<MachineType>`  
`listFree(typeID: string) : List<Machine>`  
`listOccupied(typeID: string) : List<Machine>`  
`alloc(machine_type: MachineType) : descriptor:MachineDescriptor, machine:Machine`  
`free(descriptor:MachineDescriptor) : boolean`  
`balloc(machine_type: MachineType) : descriptor:MachineDescriptor, machine:Machine`

Each successful `alloc` or `balloc` returns a descriptor for that allocation. Pass it to `free` once and discard it after release. If the same machine is allocated again, use the new descriptor.

The robust edition records the current descriptor per allocation and checks it on release. Its default `metadata.config.freeCheck = true` rejects stale, repeated or foreign descriptors before changing the lists. Existing initialization scripts can set this field to `false` to disable rejection. The performance disk omits both the allocation records and the release check at build time.
