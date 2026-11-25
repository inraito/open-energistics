local pipes = require('oecore.pipe')
local scheduler = require('oecore.scheduler').new()
local source, target = {[0] = {registry_name = 'iron', num = 3}}, {}
local card = oc.switchingCard({src = source, dst = target})
local pipe = pipes.new({type = 'storage', addr = 'src', slot = 0},
                      {type = 'storage', addr = 'dst', slot = 1}):transferThreshold(1)
pipe:goParanoia({type = 'storage', addr = 'src', slot = 1}):start(scheduler, card)
oc.run(scheduler, 2)
assert(target[1] and target[1].num == 1, 'paranoia policy ended without moving available item')
