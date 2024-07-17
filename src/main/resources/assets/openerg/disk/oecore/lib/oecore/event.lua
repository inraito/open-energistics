local module = {}

---@class event
local event = {}

function event:setID(id)
    self.id = id
    return self
end

function event:getID()
    return self.id
end

function event:setPayload(payload)
    self.payload = payload
    return self
end

function event:getPayload()
    return self.payload
end

--------------------------------------------------

function module.new(id, payload)
    ---@type event
    local ins = {}
    setmetatable(ins, {__index=event})
    ins:setID(id)
    ins:setPayload(payload)
    return ins
end

return module