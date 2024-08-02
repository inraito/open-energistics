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
local strategy = {}
strategy[policy.Reckless] = function(self, card)
    while true do
        local flag = helper[self.src.type].pull(card, helper.arg(self.src))
        if flag then
            helper[self.dst.type].push(card, helper.arg(self.dst))
            self.transferred = self.transferred + 1
            if self.threshold~=nil and self.transferred >= self.threshold then
                self:stop()
            end
        else
            break --stop when nothing could be transferred
        end
    end
end
---------------------------------------------------------------------------
function pipe:transfer(card)
    -- This trick of returning multiple values and making them as arguments of the next call
    -- is only correct if these values are the last arguments. If you use this trick on args
    -- in the middle, only the first value returned will be passed to the call.
    helper[self.src.type].pull(card, helper.arg(self.src))
    helper[self.dst.type].push(card, helper.arg(self.dst))
end

local function canTransfer(srcItem, dstItem, stackSize)
    if not dstItem then
        return true
    end
    return srcItem.registry_name == dstItem.registry_name and (1 + dstItem.num <= stackSize)
end

---@param self pipe
strategy[policy.Cautious] = function(self, card)
    while true do
        local srcItem = helper[self.src.type].peek(card, helper.arg(self.src))
        local dstItem = helper[self.dst.type].peek(card, helper.arg(self.dst))

        if not srcItem then
            -- Stall the pipe and wait for more items to be available
            coroutine.yield()
        elseif canTransfer(srcItem, dstItem, self._stackSize) then
            self:transfer(card)
            self.transferred = self.transferred + 1
            if self.threshold~=nil and self.transferred >= self.threshold then
                return
            end
        else
            coroutine.yield()
        end
    end
end
---------------------------------------------------------------------------

function pipe:goInscriber()
    self._policy = policy.AEInscriber
    return self
end

strategy[policy.AEInscriber] = function(self, card)
    while true do
        local srcItem = helper[self.src.type].peek(card, helper.arg(self.src))
        local dstItem = helper[self.dst.type].peek(card, helper.arg(self.dst))

        if not srcItem then
            -- Stall the pipe and wait for more items to be available
            coroutine.yield()
        elseif canTransfer(srcItem, dstItem, self._stackSize) then
            -- this is designed for inscriber because:
            -- `present ~= could be pulled`
            local flag = helper[self.src.type].pull(card, helper.arg(self.src))
            if(flag) then
                helper[self.dst.type].push(card, helper.arg(self.dst))
                self.transferred = self.transferred + 1
                if self.threshold~=nil and self.transferred >= self.threshold then
                    return
                end
            else
                coroutine.yield()
            end
        else
            coroutine.yield()
        end
    end
end

strategy[policy.Exclusive] = function(self, card)
    --TODO
end
strategy[policy.Paranoia] = function(self, card)
    --TODO
end
pipe.strategy = strategy

---@param scheduler scheduler
function pipe:start(scheduler, card)
    local c = coroutine.create(function()
        local flag, err = pcall(function ()
            self.strategy[self._policy](self, card)
        end)
        if not flag then
            print(err)
        end
    end)
    self.scheduler = scheduler
    self.id =  scheduler:add(c, self._hook)
end

function pipe:stop()
    if self.id then
        self.scheduler:remove(self.id)
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