---This file is used to init the block controller used for the crafting of AE2 chips.
local c = require('component')
local b = c.block_controller
local direction ={
    'up', --calculation
    'down', --silicon
    'south', --completed processor
    'north', --engineering
    'west' --logic
}
for i=0,4 do
    local addr=i*4
    b.map(addr, direction[i+1], 'up', 0)
    b.map(addr+1, direction[i+1], 'down', 0)
    b.map(addr+2, direction[i+1], 'east', 0)
    b.map(addr+3, direction[i+1], 'east', 1)
end