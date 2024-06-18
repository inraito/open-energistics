-- creating an instance of an oemm
local oemm = require('oemm')
local util = require('oemm.util')

local args = oemm_args

local instance = {}

local config = {
    rigorousFreeCheck = true, -- disable it to skip free check, catastrophic if you free machines twice
}

local metadata = {
    state = oemm.stateDict.Running,
    types = {},
    config = config,
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
function instance.registerType(type, typeID)
    local typeTable = {
        free=node(nil, nil, nil),
        occupied=node(nil, nil, nil),
        type = type
    }
    metadata.types[typeID] = typeTable
end

function instance.listTypes()
    return metadata.types.keys()
end

function instance.registerMachine(typeID, machine)
    if machine == nil then
        error('Machine Empty!')
    end
    local typeTable = metadata.types[typeID]
    local head = typeTable.free
    typeTable.free = node(nil, head, machine)
    head.former = typeTable.free
end

function instance.shutdown()
    if metadata.state == oemm.stateDict.Initializing then
        error('Illegal State!')
    else
        metadata.state = oemm.stateDict.Closing
        if util.size(instance.listAllocated()) == 0 then
            metadata.state = oemm.stateDict.Closed
            return true
        end
        return false
    end
end

function instance.listAllocated()
    -- TODO
end

function instance.alloc(typeID)
    if metadata.state == oemm.stateDict.Running then
        local typeTable = metadata.types[typeID]
        if typeTable.free.data == nil then
            return nil, 'No available machine.'
        end
        -- detach from free linked list
        local _node = typeTable.free.data
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

function instance.free(descriptor)
    if metadata.state == oemm.stateDict.Closed or metadata.state == oemm.stateDict.Initializing then
        error('Illegal State!')
    end
    local typeID = descriptor.id
    local _node = descriptor.node
    local typeTable = metadata.types[typeID]
    -- check whether given descriptor describes a node on the occupied linked list
    if metadata.config.rigorousFreeCheck then
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
    return true
end

oemm_args = instance
dofile(args['initScript'])
oemm_args = nil

return instance