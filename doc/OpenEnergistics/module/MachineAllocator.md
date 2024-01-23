# Machine Allocator

This modules handles the registration and allocation of machine and machine types.
### API
#### Method
`register_type(machine_type: MachineType)`  
`register_machine(machine: Machine)`  
`init()` --end init phase, configuration operation above not accepted after that  
`alloc(machine_type: MachineType) : machine_descriptor`  
`alloc(machine_type: MachineType, num) : machine_descriptor[]`
`free(machine_descriptor) : boolean`
#### State
`AllocationTable: MachineDescriptor -> Machine`