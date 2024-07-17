---
--- Scheduler of the oe. Be aware that lua uses non-preemptive scheduling,
--- and coroutines run strictly concurrently.
---
local clib = require('coroutine')
local os = require('os')
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
---@param coroutine thread
---@param hook fun():void pass info through global variables.
function scheduler:add(coroutine, hook)
    self.id = self.id + 1
    self.coroutines[self.id] = {
        coroutine = coroutine,
        status = dict.Running,
        hook = hook
    }
    print('Coroutine id=' .. self.id .. ' added.')
    return self.id
end

function scheduler:remove(id)
    self.coroutines[id] = nil
end

function scheduler:schedule()
    while true do
        local toRemove = {}
        for id, coroutine in pairs(self.coroutines) do
            if coroutine.status == dict.Running then
                local c = coroutine.coroutine
                self.currentID = id
                local flag = clib.resume(c)
                if clib.status(c) == 'dead' then
                    print('Coroutine id=' .. id .. ' is dead')
                    if coroutine.hook ~= nil then
                        coroutine.hook()
                    end
                    table.insert(toRemove, id)
                end
            end
        end
        for _, id in pairs(toRemove) do
            self.coroutines[id] = nil
        end
        os.sleep(0)
    end
end

---
---@return string id of current running coroutine
function scheduler:current()
    return self.currentID
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