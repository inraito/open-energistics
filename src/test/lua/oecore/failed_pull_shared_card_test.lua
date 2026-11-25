local pipes = require('oecore.pipe')
local scheduler = require('oecore.scheduler').new()
local source, target = {[0] = {registry_name = 'iron', num = 3}}, {}
local card = oc.switchingCard({src = source, dst = target})
local pipe = pipes.new({type = 'storage', addr = 'src', slot = 0},
                      {type = 'storage', addr = 'dst', slot = 1}):transferThreshold(1)
-- Another pipe left a different item at the top of the shared card.
local top = 'gold'
card.push = function() return false end
card.pop = function()
    target[1] = {registry_name = top, num = 1}
    top = nil
    return true
end
pipe:start(scheduler, card)
oc.run(scheduler, 1)
assert(top == 'gold' and target[1] == nil, 'failed pull delivered the previous gold item')
