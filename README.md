# Open Energistics
A minecraft forge mod for minecraft version 1.16.5.

## About
This mod adds some useful components for oc computers to use. With these components, you could integrate an OC network into a AE2
network, making the former a crafting subnet of the latter, which means you could use lua scripts to control the crafting process
of an AE2 pattern that could still be called from a ME terminal(or AE2 CPU). Annoyed by complicated recipes like Botania's runic altar? 
This is the one mod to solve them all.

Besides, components added also enables you to amalgamate all the machines you have, regardless of their "layout", creating a
gigantic machine repository, then occupy them when needed and free them when not. And with the help of a ME Switching Card,
which enables a novel logistics that haven't been possible before in minecraft history, you could even make your factory <b>virtual</b>.
Don't bother to contemplate for the physical positions of machines and actual pipes, just leave it to OE, and focus on the logical
structure of your assembly line.

This mod depends on Applied Energistics 2 and OpenComputers.  
**See Also**:
- https://github.com/AppliedEnergistics/Applied-Energistics-2
- https://github.com/KosmosPrime/OpenComputers and https://github.com/MightyPirates/OpenComputers

<b>
Disclaimer: Neither the java code nor the lua code has been rigorously tested, and we won't be
held responsible for any damages caused by the use of this mod.
</b>

## Installing OECore and OEMM in OpenOS

The mod adds OECore and OEMM floppies and each floppy offers a performance edition and a robust edition. Insert the matching floppy and run the OpenOS command for the edition you want. For example, to combine a performance scheduler with checked machine handles:

```sh
install oecore --fromDir=performance
install oemm --fromDir=robust
```

You can choose either edition for either component. Omitting `--fromDir` installs the performance edition. To switch later, install the other edition from the same floppy onto the same OpenOS drive, then restart the OC computer to reload its Lua modules. The installer preserves existing configuration and example scripts under `/etc/oecore` and `/etc/oemm`.

See the [source annotations and test commands](./doc/testing/UnitTests.md).

## Tutorial
[OC Interface](./tutorial/v1.0.0/tutorial.md)  
[ME Switching Card and Block Controller](./tutorial/v1.1.1/tutorial.md)  
[OE Interface and OE](./tutorial/v1.2.0/tutorial.md)  

## License
* All code
  - (c) 2023-2024, inraito and GlassCabbage  
  - [![License](https://img.shields.io/badge/License-MIT-red.svg?style=flat-square)](http://opensource.org/licenses/MIT)
* Textures
  - (c) 2023-2024, GlassCabbage
  - [![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%203.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-sa/3.0/)
