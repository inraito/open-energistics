local manager = require('oemm').new('/etc/oemm/mm1/init.lua')
local scheduler = require('oecore.scheduler').new()
local machine = {}
manager:registerType({}, 'furnace')
manager:registerMachine('furnace', machine)

local descriptor, first = manager:alloc('furnace')
assert(first == machine and manager:listOccupied('furnace')[1] == machine)
assert(manager:alloc('furnace') == nil, 'machine was allocated twice')

local received, released
scheduler:add(coroutine.create(function()
    local nextDescriptor, nextMachine = manager:balloc('furnace', scheduler)
    received = nextMachine
    released = manager:free(nextDescriptor)
end))
oc.run(scheduler, 2)
assert(received == nil and #manager:listFree('furnace') == 0)
assert(manager:free(descriptor))
oc.run(scheduler, 2)
assert(received == machine and released == true, 'release did not wake waiting task')
assert(#manager:listOccupied('furnace') == 0 and manager:listFree('furnace')[1] == machine)
-- Release the older allocation first, then use the new handle after reallocation.
local otherMachine = {}
manager:registerMachine('furnace', otherMachine)
local older, olderMachine = manager:alloc('furnace')
local younger, youngerMachine = manager:alloc('furnace')
assert(olderMachine == otherMachine and youngerMachine == machine)
assert(manager:free(older))
local current, allocatedAgain = manager:alloc('furnace')
assert(allocatedAgain == olderMachine)
assert(current.node.former == nil, 'reallocated list head still points to the younger allocation')
assert(manager:free(current))
assert(manager:free(younger))
local available = manager:listFree('furnace')
assert(#available == 2 and available[1] == youngerMachine and available[2] == olderMachine)
assert(#manager:listOccupied('furnace') == 0)

assert(require('oemm').shutdown(manager))
assert(manager:alloc('furnace') == nil, 'closed manager allocated a machine')
