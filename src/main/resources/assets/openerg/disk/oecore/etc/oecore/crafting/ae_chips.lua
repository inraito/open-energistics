--- This is an example of how to use oe as a ae2's crafting subnet.
--- As you can see, this file implement 7 recipes of ae2's chips.
---
--- But to make it work, you need:
--- 1.  configure the patterns of these chips to OE Interface manually,
---     with the ids at the end of this file.
--- 2.  use the script in /bin/oe_map.lua to init the block controller
---     and place the AE Inscribers accordingly.
--- 3.  besides having a computer with the components previously mentioned,
---     you also need the computer to have:
---     (a) an ME Switching Card.
---     (b) a network card.
--- 4.  replace the string 'stub!' with the right value. currently, there
---     are two of them, one in this file, and one in /etc/oecore/init.lua
---
--- Yes, this script doesn't use machine manager, because the nature of
--- inscribers allows using it this way: just pushing things in, no need
--- to bother if there is anyone else using it. Also you can see that
--- each pipe only transfer 1 item, it's very likely that the result
--- of a call is not transferred by the pipe it created. But that's
--- fine, because it is fine, nothing to be worried about.

---@type OpenEnergistics
local oe = oecore_args1
local out_addr, out_slot = table.unpack(oecore_args2)
local pipe = require('oecore.pipe')
local component = require('component')

local block_controller_addr = 'stub!'

--- used to leverage static analyzer, could be replaced with simply return payload.
---@param event event
local function unpack(event)
    local payload = event:getPayload()
    return {
        payload = payload,
        localAddr = payload.localAddr,
        remoteAddr = payload.remoteAddr,
        sequenceHead = payload.sequenceHead,
        sequenceTail = payload.sequenceTail
    }
end

---@param event event
local function printed(event, slotIn, slotOut)
    local data = unpack(event)
    local pipe1 = pipe.new({
        type = 'storage',
        addr = data.remoteAddr,
        slot = data.sequenceHead
    }, {
        type = 'storage',
        addr = block_controller_addr,
        slot = slotIn
    })
    local pipe2 = pipe.new({
        type = 'storage',
        addr = block_controller_addr,
        slot = slotOut
    }, {
        type = 'storage',
        addr = out_addr,
        slot = out_slot
    })
    return pipe1, pipe2
end

--- Assume all chips pattern are configured in this way:
--- printed core, redstone, printed_silicon
local function _chips(event, up, middle, down, slotOut)
    local data = unpack(event)
    local pipeUp = pipe.new({
        type = 'storage',
        addr = data.remoteAddr,
        slot = data.sequenceHead
    }, {
        type = 'storage',
        addr = block_controller_addr,
        slot = up
    })
    local pipeMiddle = pipe.new({
        type = 'storage',
        addr = data.remoteAddr,
        slot = data.sequenceHead + 1
    }, {
        type = 'storage',
        addr = block_controller_addr,
        slot = middle
    })
    local pipeDown = pipe.new({
        type = 'storage',
        addr = data.remoteAddr,
        slot = data.sequenceHead + 2
    }, {
        type = 'storage',
        addr = block_controller_addr,
        slot = down
    })
    local pipeOut = pipe.new({
        type = 'storage',
        addr = block_controller_addr,
        slot = slotOut
    }, {
        type = 'storage',
        addr = out_addr,
        slot = out_slot
    })
    return pipeUp, pipeMiddle, pipeDown, pipeOut
end

---@param p pipe
local function runPipe(p)
    p:stackSize(1)
    p:transferThreshold(1)
    -- AE's inscriber behave very weirdly, so we have to use this strategy.
    -- You can find details of this strategy here in '/lib/oecore/pipe.lua'
    p:goInscriber()
    p:start(oe.scheduler, component.me_switching_card)
end

local function printed_calculation(event)
    local p1, p2 = printed(event, 2, 3)
    runPipe(p1)
    runPipe(p2)
    return
end

local function printed_silicon(event)
    local p1, p2 = printed(event, 6, 7)
    runPipe(p1)
    runPipe(p2)
    return
end

local function printed_engineering(event)
    local p1, p2 = printed(event, 14, 15)
    runPipe(p1)
    runPipe(p2)
    return
end

local function printed_logic(event)
    local p1, p2 = printed(event, 18, 19)
    runPipe(p1)
    runPipe(p2)
    return
end

local function chips(event)
    local up, middle, down, out = _chips(event, 8, 10, 9, 11)
    runPipe(up)
    runPipe(middle)
    runPipe(down)
    runPipe(out)
    return
end

local bus = oe.craftingBus

bus:register('AE2:printed_calculation', printed_calculation)
bus:register('AE2:printed_silicon', printed_silicon)
bus:register('AE2:printed_engineering', printed_engineering)
bus:register('AE2:printed_logic', printed_logic)
bus:register('AE2:calculation', chips)
bus:register('AE2:engineering', chips)
bus:register('AE2:logic', chips)

