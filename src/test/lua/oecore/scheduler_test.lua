local scheduler = require('oecore.scheduler').new()
local ran, finished = 0, 0
local id = scheduler:add(coroutine.create(function()
    ran = ran + 1
    assert(scheduler:sleep(scheduler:current()))
    coroutine.yield()
    ran = ran + 1
end), function() finished = finished + 1 end)
oc.run(scheduler, 3)
assert(ran == 1 and finished == 0 and not scheduler:sleep(id))
assert(scheduler:wake(id) and not scheduler:wake(id))
oc.run(scheduler, 2)
assert(ran == 2 and finished == 1 and not scheduler.coroutines[id])
