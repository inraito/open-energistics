---
local Machine = {}

--- Basically you should use them like this:
--- ```lua
--- while not complete do
---     local machine = Machine:new(machineType)
---     machine:slot(...)
---     ...
---     machine:func(...)
---     ...
---     machine:assert()
---     register(machine)
--- end
--- ```

function Machine:new(MachineType)
    local instance = {
        type = MachineType,
        slots = {},
        functions = {}
    }
    setmetatable(instance, {__index=self})
    return instance
end

---
---@param id string
---@param addr string address of the storage system
---@param slot number slot number of the storage system
function Machine:slot(id, addr, slot)
    if self.type.slots[id] == nil then
        error('Slot id=' .. id .. ' is not in this machine type')
    end
    self.slots[id] = {addr, slot}
    return true
end

---
---@param id string
---@param method function
function Machine:func(id, method)
    if self.type.functions[id] == nil then
        error('Function id=' .. id .. ' is not in this machine type')
    end
    self.functions[id] = method
    return true
end

--- Check whether the machine has been configured correctly.
---@return boolean true if all 'interfaces' defined in MachineType are implemented
function Machine:check()
    for id, v in pairs(self.type.slots) do
        if self.slots[id] == nil then
            return false
        end
    end
    for id, v in pairs(self.type.functions) do
        if self.functions[id] == nil then
            return false
        end
    end
    return true
end

function Machine:assert()
    if not self:check() then
        error('Machine not configured completely!')
    end
end

---------------------------------------------------------------------------------------

function Machine:push(card, id, num)
    self.type:push(card, self, id, num)
end

function Machine:pop(card, id)
    self.type:pop(card, self, id)
end

function Machine:invoke(id, ...)
    self.type:invoke(self, id, ...)
end

return Machine