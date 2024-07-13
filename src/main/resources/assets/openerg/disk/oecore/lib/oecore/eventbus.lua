
local module = {}

---@class eventbus
local eventbus = {}

---@param scheduler scheduler
function eventbus:init(scheduler)
    self.scheduler = scheduler
    self.subscriber = {}
end

---
---@param id string
---@param callback fun(event:event)
function eventbus:register(id, callback)
    if self.subscriber[id] == nil then
        self.subscriber[id] = {}
    end
    table.insert(self.subscriber[id], callback)
end

function eventbus:post(id, event)
    for _, callback in pairs(self.subscriber[id]) do
        local task = coroutine.create(function()
            callback(event)
        end)
        self.scheduler:add(task, nil)
    end
end

local craftingBus = {}
setmetatable(craftingBus, {__index=eventbus})
function craftingBus:init(scheduler, mainBus)
    eventbus.init(self, scheduler) --super.init()
    ---------------------------------------------

    ---@param event event
    local f = function(event)
        local payload = event:getPayload()
        self:post(payload.id, payload.payload)
    end
    mainBus:register('oecore:craftingbus', f)
end

---@param scheduler scheduler
function module.new(scheduler)
    local ins = {}
    setmetatable(ins, {__index=eventbus})
    ins:init(scheduler)
    return ins
end

---@param scheduler scheduler
---@param mainBus eventbus
function module.craftingBus(scheduler, mainBus)
    local ins = {}
    setmetatable(ins, {__index=craftingBus})
    ins:init(scheduler, mainBus)
    return ins
end

return module