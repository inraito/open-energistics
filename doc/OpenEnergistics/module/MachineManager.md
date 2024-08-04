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