# Process

This class abstract a general crafting process.
Code inside the run method will be run on coroutine so be careful about deadlock and atomicity.

The protocol is you acquire and release all resources you need atomically.
You always asked for all the resources you need and if you don't get all of
them, release all you have acquired and yield. Do it again when you are waked. 
It's crucial for avoiding deadlock. And when actually crafting, always yielding
instead of blocking, otherwise the whole program will be blocked by you.  

There will be a lot of coroutines running when actually crafting, it's common
for some of them to fail at acquiring resources. Just yield and retry later
when that happens. As long as you really have enough resources, eventually,
other coroutines will end and release the resources you need. Nothing to worry
about, unless you didn't follow the protocol described above.
## Implementation
[MachineProcess](./MachineProcess.md)

### Methods
`run()`