require('component').get = function() return 'controller' end
local mm = require('oemm').new('/etc/oemm/mm1/furnace.lua')
local free = mm:listFree('furnace')
assert(#free == 2)
local slots = {}
for _, machine in ipairs(free) do
    machine:assert()
    for _, id in ipairs({'fuel', 'ing', 'pro'}) do
        local addr, slot = table.unpack(machine.slots[id])
        assert(addr == 'controller' and not slots[slot])
        slots[slot] = true
    end
end
for i = 1, 6 do assert(slots[i]) end
