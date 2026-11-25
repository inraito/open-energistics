local scheduler = require('oecore.scheduler').new()
local pipe = require('oecore.pipe')
local source = {[2] = {registry_name = 'iron', num = 3}}
local destination = {[7] = {registry_name = 'copper', num = 1}}
local card = oc.switchingCard({source = source, destination = destination})

local transfer = pipe.new(
    {type = 'storage', addr = 'source', slot = 2},
    {type = 'storage', addr = 'destination', slot = 7})
local finished = 0
transfer:transferThreshold(2):hook(function() finished = finished + 1 end)
transfer:start(scheduler, card)
oc.run(scheduler, 2)
assert(source[2].num == 3 and destination[7].registry_name == 'copper' and finished == 0,
    'incompatible destination should stall')

destination[7] = {registry_name = 'iron', num = 4}
oc.run(scheduler, 2)
assert(source[2].num == 1 and destination[7].num == 6)
assert(transfer.transferred == 2 and finished == 1)
assert(next(scheduler.coroutines) == nil, 'completed pipe still scheduled')
