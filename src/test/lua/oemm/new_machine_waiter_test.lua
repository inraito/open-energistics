local mm = require('oemm').new('/etc/oemm/mm1/init.lua')
local scheduler = require('oecore.scheduler').new()
mm:registerType({}, 'grinder')
local cancelled = scheduler:add(coroutine.create(function()
    mm:balloc('grinder', scheduler)
end))
oc.run(scheduler, 1)
scheduler:remove(cancelled)

local acquired
scheduler:add(coroutine.create(function()
    local lease, machine = mm:balloc('grinder', scheduler)
    acquired = machine
    assert(mm:free(lease))
end))
oc.run(scheduler, 1)
local grinder = {}
mm:registerMachine('grinder', grinder)
oc.run(scheduler, 3)
assert(acquired == grinder, 'a machine is free but the blocked caller did not wake')
