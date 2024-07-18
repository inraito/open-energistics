# Introduction

Documentations in this folder describe a software with the same
name as the mod itself, OpenEnergistics or OE for short, running on
OpenComputer computers. It is designed to be part of AE2's crafting
network, a subnet that uses oc's lua script to control the 
crafting process.

## Key Features
### Highly Customizable
The whole OpenEnergistics is highly customizable, users can achieve
their specific and unique goals either by configurations within the 
framework or by easily extending the framework.
+ Customizing Crafting Processes
+ Customizing Machine Allocation Logic(by adapting the oemm yourself :p)

### Machine Abstraction
OE provides a machine abstraction that allow machine users to ignore
the "physical" details of a specific machine, and focus on the bigger
pictures of the crafting process. With this abstraction, users can 
build a gigantic "machine repository" that contains all the machines
users have and use them with the machine allocation feature of OE that
will be described in the following section.

### Machine Allocation
Crafting process could allocate machines from a need-to-use
basis. That is, we only occupy a machine when we really need it, and
when we don't need it, it could be used by other crafting process.

### Pipes
Pipes are an abstraction of an ItemStack pipe that connect two "slot"
together. It's easier to understand if we compare them to normal pipes
in other classic mods, say buildcraft. Instead of connecting two TileEntity,
physically, OE pipes connect two slots, in the same or in different tiles.
And though that is the case in a more technical perspective, most OE pipes,
from the users' perspective, are connecting two slots in "machines". The 
point is that pipes could depend on abstract machines, instead of a specific
tile entity. And that bring us to the next difference, OE pipes are not 
"physical", they only exist in codes, more specifically, oc scripts running
on oc's cpu, which makes them extremely flexible, because you can establish
and destroy one whenever you want and the cost of which is almost zero.

### Virtual Assembly Line
With the machine abstraction, the machine allocation and the pipes, users
could use them to build a virtual assembly line just in code. And the
assembly line created this way could be easily dismissed, recreated and scaled.
Say goodbye to idle machines in your idle assembly line, because if you have
an idle assembly line, why not just dismiss it and use the machines on other
things, like a different line or a crafting process.

### Efficiency Achieved by Coroutines
OE uses coroutine mechanics to achieve high efficiency, crafting
processes only occupies cpu when they are actually doing something,
and if not, they yield to let other processes run.

