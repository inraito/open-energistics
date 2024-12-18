--- util for achieving a async procedure
--- It is an AOV graph effectively, each vertex is a coroutine,
--- and each coroutine will only be started when all its
--- predecessors are finished.

local module = {}

---@class procedure
local procedure = {}

function procedure:init()
    ---@type number
    self.p = 1
    ---@type table
    self.roots = {}
    ---@type table
    self.vertices = {}
end

---@param c thread
---@return table node
function procedure:_new_node(c)
    return {
        coroutine=c, child={},
        dependency={},
        running = false, -- true once committed to scheduler
        finished=false, -- true once finished
    }
end

---@param obj table node
---@return number descriptor
function procedure:_insert2vert(obj)
    local p = self.p
    self.vertices[p] = obj
    self.p = self.p + 1
    return p
end

function procedure:node(c)
    local obj = self:_new_node(c)
    local descriptor = self:_insert2vert(obj)
    return descriptor
end

---@param c thread
---@return number descriptor
function procedure:root(c)
    local descriptor = self:node(c)
    table.insert(self.roots, descriptor)
    return descriptor
end

---@param parent_descriptor number descriptor
---@param child_descriptor number descriptor
function procedure:add_dependency(parent_descriptor, child_descriptor)
    table.insert(self.vertices[parent_descriptor].child, child_descriptor)
    table.insert(self.vertices[child_descriptor].dependency, parent_descriptor)
end

---@param scheduler scheduler
---@param procedure procedure
---@param descriptor number
local function hook(scheduler, procedure, descriptor)
    local v = procedure.vertices[descriptor]
    v.finished = true
    for _, child_descriptor in pairs(v.child) do
        local child = procedure.vertices[child_descriptor]
        local flag = true
        for _, dependency in pairs(child.dependency) do
            if not procedure.vertices[dependency].finished then
                flag = false
            end
        end
        if flag then
            procedure.vertices[dependency].running = true
            scheduler:schedule(procedure.vertices[dependency].coroutine, function()
                hook(scheduler, procedure, dependency)
            end)
        end
    end
end

---@param scheduler scheduler
function procedure:start(scheduler)
    for _, v in pairs(self.root) do
        self.vertices[v].running = true
        scheduler:schedule(self.vertices[v].coroutine, function()
            hook(scheduler, self, v)
        end)
    end
end

function module.new()
    local ins = {}
    setmetatable(ins, {__index=procedure})
    ins:init()
    return ins
end

return module