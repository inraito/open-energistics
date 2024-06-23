local module = {}
local queue = {}

local function node(former, next, data)
    return {
        former = former,
        next = next,
        data = data
    }
end

function queue:init()
    self.head = node(nil, nil, nil)
    self.tail = node(self.head, nil, nil)
    self.head.next = self.tail
end

function queue:isEmpty()
    return self.tail.former == self.head
end

function queue:queue(element)
    local toInsert = node(self.tail.former, self.tail, element)
    self.tail.former.next = toInsert
    self.tail.former = toInsert
end

function queue:dequeue()
    if self:isEmpty() then
        return nil, 'Queue Empty!'
    end
    local out = self.head.next
    self.head.next = out.next
    out.next.former = self.head
    return out.data
end

function module.new()
    local ins = {}
    setmetatable(ins, {__index = queue})
    ins:init()
    return ins
end

return module