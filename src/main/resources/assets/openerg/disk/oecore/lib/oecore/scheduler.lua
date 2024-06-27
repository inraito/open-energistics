---
--- Scheduler of the oe. Be aware that lua uses non-preemptive scheduling,
--- and coroutines run strictly concurrently.
---
local queue = require('oecore.util.queue')
local clib = require('coroutine')
local module = {}
local dict = {
    Running = 'running',
    Waiting = 'waiting'
}
module.Status = dict

---@class scheduler
scheduler = {}

function scheduler:init()
    self.id = 0
    self.coroutines = {}
    return self
end

---
---@param coroutine thread note that only the first return value transferred to hook
function scheduler:add(coroutine, hook)
    self.id = self.id + 1
    self.coroutines[self.id] = {
        coroutine = coroutine,
        status = dict.Running,
        hook = hook
    }
    return self.id
end

function scheduler:schedule()
    while true do
        local toRemove = {}
        for id, coroutine in pairs(self.coroutines) do
            if coroutine.status == Running then
                local c = coroutine.coroutine
                local flag, res = clib.resume(c)
                if flag then
                    if coroutine.hook ~= nil then
                        coroutine.hook(res)
                    end
                    table.insert(toRemove, id)
                end
            end
        end
        for _, id in pairs(toRemove) do
            self.coroutines[id] = nil
        end
    end
end

function scheduler:wake(id)
    if self.coroutines[id].status ~= dict.Waiting then
        return false
    end
    self.coroutines[id].status = dict.Running
    return true
end

function scheduler:sleep(id)
    if self.coroutines[id].status ~= dict.Running then
        return false
    end
    self.coroutines[id].status = dict.Waiting
    return true
end

function module.new()
    local ins = {}
    setmetatable(ins, {__index=scheduler})
    return ins:init()
end

return module