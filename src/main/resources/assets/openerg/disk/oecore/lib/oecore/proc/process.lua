--- Process used by oe, raw use of coroutine is also acceptable,
--- but sometimes it's more convenient to pack some of them together
--- as a process.
---
--- 1. process give them a natural place to exchange info.
--- 2. we may want to monitor the progress of a certain task in which
---    case combining them together and give the combined process a status
---    would be more reasonable.

local module = {}

---@class process
local process = {}

function process:init()
    --- total number of coroutines and procedures in this process
    self.total = 0
    self.finished_num = 0

    --- coroutines packed in a procedure
    self.procedures = {}
    self.p_index = 1
    --- normal coroutines
    self.coroutines = {}
    self.c_index = 1

    --- callbacks called when the process is finished, i.e. all coroutines and procedures are finished.
    self.callbacks = {}

    --- readable and writeable for coroutines of this process
    self.data = {}
    --- status of this process, write only for coroutines of this process
    self.status = {
        running = false,
        finished = false,
    }
end

---@param c thread
function process:coroutine(c)
    local obj = {
        coroutine = c,
        finished = false,
        index = self.c_index,
    }
    self.coroutines[self.c_index] = obj
    self.c_index = self.c_index + 1
    self.total = self.total + 1
end

---@param procedure procedure
function process:procedure(procedure)
    local obj = {
        procedure = procedure,
        finished = false,
        index = self.p_index,
    }
    procedure.process = self -- add reference to this process
    self.procedures[self.p_index] = obj
    self.p_index = self.p_index + 1
    self.total = self.total + 1
end

---@param process process
---@param obj table
local function c_hook(process, obj)
    process.coroutines[obj.index].finished = true
    process.finished_num = process.finished_num + 1
    if process.finished_num >= process.total then
        process:_finish()
    end
end

---@param process process
---@param obj table
local function p_hook(process, obj)
    process.procedures[obj.index].finished = true
    process.finished_num = process.finished_num + 1
    if process.finished_num >= process.total then
        process:_finish()
    end
end

---not recommended to use this function directly, use manager instead
---@param scheduler scheduler
function process:run(scheduler)
    self.status.running = true
    for idx, obj in pairs(self.coroutines) do
        scheduler:add(obj.coroutine, function() c_hook(self, obj) end)
    end
    for idx, obj in pairs(self.procedures) do
        scheduler:add(obj.coroutine, function() p_hook(self, obj) end)
    end
end

function process:_finish()
    self.status.running = false
    self.status.finished = true
    for _, cb in pairs(self.callbacks) do
        cb()
    end
    --TODO
end

function process:onFinish(cb)
    table.insert(self.callbacks, cb)
end

---@return process
function module.new()
    local ins = {}
    setmetatable(ins, {__index=process})
    ins:init()
    return ins
end

return module