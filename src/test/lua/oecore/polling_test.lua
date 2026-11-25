local polling = require('oecore.util.polling')
local ready = false
local poll = polling.create():predicate(function() return ready end)
local task = coroutine.create(function() poll:start() end)
assert(coroutine.resume(task) and coroutine.status(task) == 'suspended')
ready = true
assert(coroutine.resume(task) and coroutine.status(task) == 'dead')
