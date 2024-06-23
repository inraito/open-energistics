local module = {}

local policy ={
    Reckless = 'reckless',   --- basically checks nothing, very reckless, but efficient.

    Cautious = 'cautious',   --- checks the src and dst before start moving items, and
                             --- if the moving fails it will try to restore the context,
                             --- undo things it has done.

    Exclusive = 'exclusive', --- meaning the card given to the pipe are exclusively granted
                             --- to it, so leaving items inside the card is acceptable.
}
module.policy = policy

local pipe = {}

function pipe:init(srcAddr, srcSlot, dstAddr, dstSlot)
    self.basic = {
        src = srcAddr,
        srcSlot = srcSlot,
        dst = dstAddr,
        dstSlot = dstSlot
    }
    self._hook = nil
    self._policy = policy.Cautious
end

function pipe:hook(hook)
    self._hook = hook
end

function pipe:policy(policy)
    self._policy = policy
end

local strategy = {}
strategy[policy.Reckless] = function(card)
    --TODO
end
strategy[policy.Cautious] = function(card)
    --TODO
end
strategy[policy.Exclusive] = function(card)
    --TODO
end
pipe.strategy = strategy

---
--- Due to the
function pipe:start(scheduler, card)
    scheduler:add(function()
        self.strategy[self._policy]()
    end, self._hook)
end

function module.new(srcAddr, srcSlot, dstAddr, dstSlot)
    local ins = {}
    setmetatable(ins, {__index=pipe})
    ins:init(srcAddr, srcSlot, dstAddr, dstSlot)
    return ins
end

return module