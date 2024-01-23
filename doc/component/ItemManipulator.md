# Item Manipulator

Component with a 1-slot inventory. Acting the same way as the *holding slot* of
a player, it allows more intuitive and convenient way of handling inventories.
It should be used in combination with transposer, and provide a way to move items
across different transposer(or inventories attached to different transposer).

### API
pull(inventory_descriptor, slot, num) : num  
push(inventory_descriptor, slot, num) : num  

inventory_descriptor = {"transposer", transposer_addr, side}