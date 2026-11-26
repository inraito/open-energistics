local scheduler = require('oecore.scheduler').new()
local id = scheduler:add(coroutine.create(function() end))
scheduler:remove(id)
assert(scheduler:sleep(id) == false)
