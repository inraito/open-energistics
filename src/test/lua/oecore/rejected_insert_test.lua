local pipes = require('oecore.pipe')
local scheduler = require('oecore.scheduler').new()
local source, target = {[0] = {registry_name = 'iron', num = 3}}, {}
local card = oc.switchingCard({src = source, dst = target})
local pipe = pipes.new({type = 'storage', addr = 'src', slot = 0},
                      {type = 'storage', addr = 'dst', slot = 1}):transferThreshold(1)
local pop = card.pop
card.pop = function(addr, slot)
    if addr == 'dst' then return false end
    return pop(addr, slot)
end
pipe:start(scheduler, card)
oc.run(scheduler, 2)
assert(pipe.transferred == 0 and source[0].num == 3 and not target[1],
    'destination rejected item: transferred=' .. pipe.transferred .. ', source=' .. source[0].num)
