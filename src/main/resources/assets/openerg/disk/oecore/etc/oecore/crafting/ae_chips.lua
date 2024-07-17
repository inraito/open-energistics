
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

