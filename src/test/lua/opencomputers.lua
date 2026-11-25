-- Each test script gets its own VM, pending signals and component table.
local listeners, signals = {}, {}
local event = {}

function event.listen(name, callback)
    listeners[name] = listeners[name] or {}
    table.insert(listeners[name], callback)
    return true
end

function event.pull()
    local signal = table.remove(signals, 1)
    if signal then
        for _, callback in ipairs(listeners[signal[1]] or {}) do
            callback(table.unpack(signal))
        end
        return table.unpack(signal)
    end
end

local component = {}
package.loaded.component = component
package.loaded.event = event

oc = {}
function oc.signal(...)
    table.insert(signals, {...})
end

-- One item per transfer, as requested by OECore's pipe strategies.
function oc.switchingCard(inventories)
    local held
    local card = {}
    function card.peek(addr, slot)
        return inventories[addr][slot]
    end
    function card.push(addr, slot, count)
        assert(count == 1 and held == nil)
        local stack = card.peek(addr, slot)
        if not stack then return false end
        held = stack.registry_name
        stack.num = stack.num - 1
        if stack.num == 0 then inventories[addr][slot] = nil end
        return true
    end
    function card.pop(addr, slot)
        assert(held)
        local stack = card.peek(addr, slot)
        if stack and stack.registry_name ~= held then return false end
        inventories[addr][slot] = {registry_name = held, num = (stack and stack.num or 0) + 1}
        held = nil
        return true
    end
    return card
end

-- Run the real scheduler. Its call to OpenOS sleep marks the end of a pass.
function oc.run(scheduler, passes)
    assert(passes >= 1)
    local done = {}
    local previous = os.sleep
    local resume = coroutine.resume
    coroutine.resume = function(...)
        local result = table.pack(resume(...))
        if not result[1] then error(result[2]) end
        return table.unpack(result, 1, result.n)
    end
    os.sleep = function()
        passes = passes - 1
        if passes == 0 then error(done) end
    end
    local ok, reason = pcall(function() scheduler:schedule() end)
    os.sleep = previous
    coroutine.resume = resume
    if ok or reason ~= done then error(reason or "scheduler exited unexpectedly") end
end
