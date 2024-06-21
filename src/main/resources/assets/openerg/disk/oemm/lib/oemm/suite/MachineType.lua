--- Combined with Machine.lua in the same directory, these two classes are
--- the open energistics' implementation of machine types and machines managed
--- by OEMM, and we will call them a suite here. But, OEMM doesn't care how they
--- are implemented, it simply manage them as abstract objects.

--- This suite are designed to be used with MESwitchingCard. By adding an
--- address translation mechanics, users of this suite could keep the trivial
--- details such as the machines' physical layout and logical layout away from
--- most of your code.

--- This class, the MachineType abstract the *view* of a machine, while the
--- Machine class assign each element in the view to a "physical" slot or
--- implementation. We would only focus on slot things here, but feel free
--- to extend this suite to meet your subtle needs.
---
--- So, let's dive into slot things. What the suite do is simply giving each
--- slot an *alias*, which is referred to as `id` in code. And on principle
--- the `MachineType` defines those aliases, and each `Machine` assigns those
--- aliases to actual slots in storage systems. Then, users of the suite could
--- use those aliases without bothering the trivial physical details of a
--- machine. And to achieve better readability and convenience, users of the
--- suite machine should call `Machine:push()`, `Machine:pop()` and
--- `Machine:invoke` instead of those methods with the same identifier in
--- `MachineType`.

local component = require('component')

local MachineType = {}

function MachineType:new(ext)
    enum = enum or {}
    local instance = {
        slots = {},
        functions = {},
        enum = {} -- builtin enum dict, could be used to replace literal string keys
    }
    if ext~=nil and ext.init~=nil then
        for k, v in pairs(ext.init) do
            instance[k] = v
        end
    end
    setmetatable(instance, {__index = self})
    return instance
end

---
---@param id string identifier of a slot, must be unique
---@param ext table extra arguments reserved for future extension
function MachineType:slot(id, ext)
    if ext == nil then
        self.slots[id] = true --- value won't be used
    end
end

function MachineType:push(card, machine, id)
    if(self.slots[id]==nil) then
        return nil, 'Illegal Arguments!'
    end
    local addr, slot = table.unpack(machine.slots[id])
    return card.pop(addr, slot)
end

function MachineType:pop(card, machine, id, num)
    if(self.slots[id]==nil) then
        return nil, 'Illegal Arguments!'
    end
    local addr, slot = table.unpack(machine.slots[id])
    return card.push(addr, slot, num)
end

---
---@param id string identifier of a function, must be unique
---@param ext table extra arguments reserved for future extension
function MachineType:func(id, ext)
    if ext == nil then
        self.functions[id] = true --- value won't be used
    end
end

function MachineType:invoke(machine, id, ...)
    if(self.functions[id]==nil) then
        return nil, 'Illegal Arguments!'
    end
    return machine.functions[id](...)
end

return MachineType