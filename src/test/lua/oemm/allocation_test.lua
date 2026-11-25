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
assert(require('oemm').shutdown(manager))
assert(manager:alloc('furnace') == nil, 'closed manager allocated a machine')
