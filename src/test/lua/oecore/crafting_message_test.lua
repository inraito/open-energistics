local oe = require('oe').new()
oe:listen(1024)

local jobs = {}
oe.craftingBus:register('processor', function(event)
    table.insert(jobs, event:getPayload())
end)

oc.signal('modem_message', 'computer', 'wrong-port', 1, 0, 'oe_interface:processor', 2, 5)
oc.signal('modem_message', 'computer', 'other-device', 1024, 0, 'processor', 4, 5)
oc.signal('modem_message', 'computer', 'interface', 1024, 0, 'oe_interface:processor', 8, 11)
oc.run(oe.scheduler, 5)

assert(#jobs == 1, 'only the matching crafting request should reach the subscriber')
local job = jobs[1]
assert(job.localAddr == 'computer' and job.remoteAddr == 'interface')
assert(job.sequenceHead == 8 and job.sequenceTail == 11, 'ingredient range changed')
