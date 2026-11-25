local oe = require('oe').new()
local events = require('oecore.event')
local received = {}
local job = events.new('processor', {slot = 4})
for i = 1, 2 do
    oe.craftingBus:register('processor', function(e) received[i] = e end)
end
oe.mainBus:post('oecore:craftingbus', events.new('route', {id = 'processor', payload = job}))
oc.run(oe.scheduler, 3)
assert(received[1] == job and received[2] == job and job:getPayload().slot == 4)
