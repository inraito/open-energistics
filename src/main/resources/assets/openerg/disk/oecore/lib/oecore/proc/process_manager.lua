--- Process Manager of oe.

local coroutine = require('coroutine')
local module = {}

---@class ProcessManager
local manager = {}

---@param oe OpenEnergistics
function manager:init(oe)
    self.oe = oe
    self.processes = {}
end

---@param process process
function manager:run(process)
    self.processes[process] = true
    process:run(self.oe.scheduler)
end

---@param oe OpenEnergistics
---@return ProcessManager
function module.new(oe)
    ---@type ProcessManager
    local ins = {}
    setmetatable(ins, {__index=manager})
    ins:init(oe)
    return ins
end

return module