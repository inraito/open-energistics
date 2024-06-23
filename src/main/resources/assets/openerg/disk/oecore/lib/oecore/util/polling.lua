---
--- This is sort of a utility for blocking condition check.
--- It allows you to block the coroutine until the given condition is met.

local polling
local coroutine = require('coroutine')
local os = require('os')

poll = {}
function poll:init(ext)
    for k, v in pairs(ext) do
        self[k] = v
    end
    self.mod = 1
    self.counter = 0
    self.timeThreshold = 0
    self.yieldTime = 0
    self.timeSupplier = function()
        return os.time()
    end
end

---
--- Set a counter to moderate polling rate.
--- Counter starts at zero and each time the poll yield, it counts once.
--- The predicate gets evaluated when and only when the count is at zero.
---@param mod number the modulus of the counter, default to one which means
---                  predicate are always evaluated
function poll:count(mod)
    self.mod = mod
    return self
end

function poll:predicate(p)
    self.p = p
    return self
end

function poll:time(time)
    self.timeThreshold = time
    return self
end

---
---@param getter fun(): number
function poll:timeGetter(getter)
    self.timeSupplier = getter
    return self
end

function poll:start()
    if self.p == nil then
        error('Predicate nil!')
    end

    --i don't think someone would use both of them, so i'll just write it this way.
    while self.timeSupplier()-self.yieldTime < self.timeThreshold
            or self.counter~=0
            or not self.p()
    do
        self.yieldTime = self.timeSupplier()
        coroutine.yield()
        self.counter = (self.counter + 1) % self.mod
    end
end

function polling.create()
    local ins = {}
    setmetatable(ins, {__index=poll})
    ins:init()
    return ins
end

return polling