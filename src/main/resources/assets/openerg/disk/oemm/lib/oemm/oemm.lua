--- Why this machine manager is provided in this script format instead of the standard OOP format
--- is that when i was writing this, i view it more as an optional, custom object which could be
--- replaced with user's own implementation or would be adapted massively before being used. So I
--- thought a script format would be more friendly to the user.

-- creating an instance of an oemm
local oemm = require('oemm')
local util = require('oemm.util')
local coroutine = require('coroutine')
local queue = require('oemm.util.queue')

local args = oemm_args

---@class oemm
local instance = {}

local config = {
    freeCheck = true, -- disable it to skip free check, catastrophic if you free machines twice
}

local metadata = {
    state = oemm.stateDict.Initializing,
    types = {},
    config = config,
    waitingQueues = {},
}
instance['metadata'] = metadata

local function node(former, next, data)
    return {
        former = former,
        next = next,
        data = data
    }
end

--- Register a machine type.
---@param type any any user defined object
---@param typeID string unique id
function instance:registerType(type, typeID)
    local typeTable = {
        free=node(nil, nil, nil),
        occupied=node(nil, nil, nil),
        type = type
    }
    metadata.types[typeID] = typeTable
end

function instance:registerMachine(typeID, machine)
    if machine == nil then
        error('Machine Empty!')
    end
    local typeTable = metadata.types[typeID]
    local head = typeTable.free
    typeTable.free = node(nil, head, machine)
    head.former = typeTable.free
end

function instance:shutdown()
    if metadata.state == oemm.stateDict.Initializing then
        error('Illegal State!')
    else
        metadata.state = oemm.stateDict.Closing
        if util.size(instance:listOccupied(nil)) == 0 then
            metadata.state = oemm.stateDict.Closed
            return true
        end
        return false
    end
end

-------------------------------------------------------------------------

function instance:listTypes()
    return util.keys(metadata.types)
end

local function linked2List(head)
    local res = {}
    local p = head
    while p.next ~= nil do
        table.insert(res, p.data)
        p = p.next
    end
    return res
end

function instance:listFree(typeID)
    local res = {}
    if typeID == nil then
        for _, typeID in ipairs(self:listTypes()) do
            for _, machine in ipairs(self:listFree(typeID)) do
                table.insert(res, machine)
            end
        end
    else
        res = linked2List(metadata.types[typeID].free)
    end
    return res
end

function instance:listOccupied(typeID)
    local res = {}
    if typeID == nil then
        for _, typeID in ipairs(self:listTypes()) do
            for _, machine in ipairs(self:listOccupied(typeID)) do
                table.insert(res, machine)
            end
        end
    else
        res = linked2List(metadata.types[typeID].occupied)
    end
    return res
end

-------------------------------------------------------------------------

---
---@param typeID string
---@return (nil, string) | (table, any)
function instance:alloc(typeID)
    if metadata.state == oemm.stateDict.Running then
        local typeTable = metadata.types[typeID]
        if typeTable.free.data == nil then
            return nil, 'No available machine.'
        end
        -- detach from free linked list
        local _node = typeTable.free
        typeTable.free = typeTable.free.next
        typeTable.free.former = nil
        -- attach to occupied linked list
        _node.next = typeTable.occupied
        typeTable.occupied.former = _node
        typeTable.occupied = _node
        -- return machine
        return {id=typeID, node=_node}, _node.data
    else
        return nil, 'Allocating Disabled!'
    end
end

function instance:free(descriptor)
    if metadata.state == oemm.stateDict.Closed or metadata.state == oemm.stateDict.Initializing then
        error('Illegal State!')
    end
    local typeID = descriptor.id
    local _node = descriptor.node
    local typeTable = metadata.types[typeID]
    -- check whether given descriptor describes a node on the occupied linked list
    if metadata.config.freeCheck then
        p = _node
        while p.former ~= nil do
            p = p.former
        end
        if p~=typeTable.occupied then
            error('Illegal Argument!')
        end
    end

    -- detach the node to free from occupied linked list
    if _node == typeTable.occupied then
        typeTable.occupied = _node.next
        _node.next.former = nil
    else
        _node.former.next = _node.next
        _node.next.former = _node.former
    end

    -- attach the node to free linked list
    _node.next = typeTable.free
    typeTable.free.former = _node
    typeTable.free = _node

    -- wake sleeping coroutine
    local coroutineID, scheduler = metadata.dequeue(typeID)
    if coroutineID ~= nil then
        scheduler:wake(coroutineID)
    end
    return true
end

-------------------------------------------------------------------------

--- Blocking version of alloc.
---@param typeID string
---@param scheduler scheduler
---@return (nil, string) | (table, any)
function instance:balloc(typeID, scheduler)
    while true do
        local descriptor, machine = instance:alloc(typeID)
        if descriptor ~= nil then
            return descriptor, machine
        end
        local currentID = scheduler:current()
        metadata.queue(typeID, currentID, scheduler)
        scheduler:sleep(currentID)
        coroutine.yield()
    end
end

-------------------------------------------------------------------------
--- Both of these functions are meant to be private, which is why they are
--- in metadata instead of instance.

function metadata.queue(typeID, coroutineID, scheduler)
    if metadata.waitingQueues[typeID] == nil then
        metadata.waitingQueues[typeID] = queue.new()
    end
    metadata.waitingQueues[typeID]:queue({coroutineID, scheduler})
end

function metadata.dequeue(typeID)
    if metadata.waitingQueues[typeID] == nil then
        return nil, 'No waiting coroutines.'
    end
    local res = metadata.waitingQueues[typeID]:dequeue()
    if res == nil then
        return nil, 'No waiting coroutines.'
    end
    return res[1], res[2]
end

-------------------------------------------------------------------------

oemm_args1 = oemm
oemm_args2 = instance
dofile(args['initScript'])
oemm_args1 = nil
oemm_args2 = nil
metadata.state = oemm.stateDict.Running

return instance