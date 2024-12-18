--- Process Manager of oe.

local coroutine = require('coroutine')
local module = {}

---@class ProcessManager
local manager = {}

---@param oe OpenEnergistics
function manager:init(oe)
    self.oe = oe
end

function manager:run()

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