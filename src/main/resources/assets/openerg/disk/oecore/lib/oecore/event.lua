local module = {}

local event = {}

function event:setID(id)
    self.id = id
end

function event:getID()
    return self.id
end

function event:setPayload(payload)
    self.payload = payload
end

function event:getPayload()
    return self.payload
end

--------------------------------------------------

function module.new(id, payload)
    local ins = {}
    setmetatable(ins, {__index=event})
    ins:setID(id)
    ins:setPayload(payload)
    return ins
end

return module