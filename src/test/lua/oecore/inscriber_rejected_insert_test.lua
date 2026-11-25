local pipes = require('oecore.pipe')
local scheduler = require('oecore.scheduler').new()
local source, target = {[0] = {registry_name = 'iron', num = 3}}, {}
local card = oc.switchingCard({src = source, dst = target})
local pipe = pipes.new({type = 'storage', addr = 'src', slot = 0},
                      {type = 'storage', addr = 'dst', slot = 1}):transferThreshold(1)
card.pop = function() return false end
pipe:goInscriber():start(scheduler, card)
oc.run(scheduler, 1)
assert(pipe.transferred == 0, 'inscriber declined item but transferred=' .. pipe.transferred)
