--- Pipe create a virtual passage for items, which has a start and an end.
--- Items in the starting slot will be constantly transferred to ending slot.
--- This transfer leverage lua's coroutine, yield itself if further transfer
--- can't happen due to all sorts of reasons.

local coroutine = require('coroutine')

local module = {}

local policy ={
    Reckless = 'reckless',   --- checks nothing, and it won't yield, reckless, but efficient.

    Cautious = 'cautious',   --- checks the src and dst before start moving items, and
                             --- if the moving fails it will try to restore the context,
                             --- put things in the card back to where they were, yield then.

    Paranoia = 'paranoia',   --- same as cautious, but uses a spill zone if the pipe can't
                             --- put things back.

    Exclusive = 'exclusive', --- meaning the card given to the pipe are exclusively granted
                             --- to it, so leaving items inside the card is acceptable.

    AEInscriber = 'inscriber'--- used for stupid ae2 inscribers.
}
module.policy = policy

---@class pipe
local pipe = {}

local helper = {
    machine = {
        pull = function(card, machine, id)
            return machine:pop(card, id, 1)
        end,
        push = function(card, machine, id)
            return machine:push(card, id)
        end,
        peek = function(card, machine, id)
            return machine:peek(card, id)
        end
    },
    storage = {
        pull = function(card, addr, slot)
            return card.push(addr, slot, 1)
        end,
        push = function(card, addr, slot)
            return card.pop(addr, slot)
        end,
        peek = function(card, addr, slot)
            return card.peek(addr, slot)
        end
    },
    arg = function(loc)
        return loc.machine or loc.addr, loc.id or loc.slot
    end
}

function pipe:init(src, dst)
    self.src = src
    self.dst = dst
    self._hook = nil
    self._policy = policy.Cautious
    self._stackSize = 64
    self.transferred = 0
end

function pipe:transferThreshold(threshold)
    self.threshold = threshold
    return self
end

function pipe:hook(hook)
    self._hook = hook
    return self
end

function pipe:policy(policy)
    self._policy = policy
    return self
end

function pipe:stackSize(size)
    self._stackSize = size
    return self
end

---@param spill table same as `src` and `dst`
function pipe:goParanoia(spill)
    self._policy = policy.Paranoia
    self.spill = spill
    return self
end
---------------------------------------------------------------------------
-- A refused item can remain on the card across scheduler passes. Only the
-- pipe that pulled it may retry that delivery.
local pending = {}

local function push(card, location)
    return helper[location.type].push(card, helper.arg(location))
end

function pipe:transfer(card)
    local key = card.address or card
    if pending[key] and pending[key] ~= self then
        return false
    end
    if not pending[key] then
        if not helper[self.src.type].pull(card, helper.arg(self.src)) then
            return false
        end
        pending[key] = self
    end

    local delivered = push(card, self.dst)
    if delivered or (self._policy ~= policy.Exclusive and push(card, self.src))
            or (self.spill and self._policy == policy.Paranoia and push(card, self.spill)) then
        pending[key] = nil
    end
    return delivered
end

local function canTransfer(self, card)
    local src = helper[self.src.type].peek(card, helper.arg(self.src))
    if not src then return false end
    local dst = helper[self.dst.type].peek(card, helper.arg(self.dst))
    return not dst or (src.registry_name == dst.registry_name and dst.num < self._stackSize)
end

function pipe:goInscriber()
    self._policy = policy.AEInscriber
    return self
end

local function run(self, card)
    local key = card.address or card
    local reckless = self._policy == policy.Reckless
    while not self.threshold or self.transferred < self.threshold do
        if (pending[key] == self or reckless or canTransfer(self, card)) and self:transfer(card) then
            self.transferred = self.transferred + 1
        elseif reckless and not pending[key] then
            return
        else
            coroutine.yield()
        end
    end
end

local strategy = {}
for _, name in pairs(policy) do
    strategy[name] = run
end
pipe.strategy = strategy

---@param scheduler scheduler
function pipe:start(scheduler, card)
    self.card = card
    local c = coroutine.create(function()
        local ok, reason = pcall(self.strategy[self._policy], self, card)
        if not ok then
            local key = card.address or card
            if pending[key] == self then pending[key] = nil end
            error(reason)
        end
    end)
    self.scheduler = scheduler
    self.id =  scheduler:add(c, self._hook)
end

function pipe:stop()
    if self.id then
        self.scheduler:remove(self.id)
        local key = self.card.address or self.card
        if pending[key] == self then
            pending[key] = nil
            print('Pipe stopped with an undelivered item on the switching card.')
        end
        self.id = nil
        return
    end
    error("pipe is not running")
end

---
---@param src table {type='storage', addr=`addr`, slot=`slot`}
---                 or {type='machine', machine=`machine`, id = `id`}
---@param dst table same as `src`
---@return pipe
function module.new(src, dst)
    local ins = {}
    setmetatable(ins, {__index=pipe})
    ins:init(src, dst)
    return ins
end

return module