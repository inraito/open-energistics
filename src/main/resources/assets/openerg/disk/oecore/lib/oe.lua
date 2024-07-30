local scheduler = require('oecore.scheduler')
local eventbus = require('oecore.eventbus')
local event = require('event')
local oeEvent = require('oecore.event')
local string = require('string')
local coroutine = require('coroutine')

local oe = {}

---@class OpenEnergistics
local openEnergistics = {}

---@param self OpenEnergistics
local function initHooks(self)
    local handler = event.listen('modem_message', function(_, localAddr, remoteAddr, port, distance, msg, sequenceHead, sequenceTail)
        local id = string.match(msg, 'oe_interface:(.*)', 1)
        if port == self.port and id~=nil then
            local payload = {
                localAddr = localAddr,
                remoteAddr = remoteAddr,
                sequenceHead = sequenceHead,
                sequenceTail = sequenceTail
            }
            local e = oeEvent.new(id, payload)
            print('Posting event `' .. id .. '`.')
            self.craftingBus:post(id, e)
        end
    end)
    if handler == nil then
        error('Could not register modem_message handler.')
    end
end

--- This coroutine is used to pull events manually so that handlers
--- could receive the events they needed. The main reason why we need
--- it is that OpenEnergistics seems to occupy the whole CPU, crowd
--- out the openos' signal puller, so we have to do it ourselves.
---@param self OpenEnergistics
local function initEventPuller(self)
    ---@type scheduler
    local scheduler = self.scheduler
    local c = coroutine.create(function()
        while(true) do
            event.pull(0)
            coroutine.yield()
        end
    end)
    scheduler:add(c, nil)
end

function openEnergistics:listen(port)
    self.port = port
end

function openEnergistics:init()
    self.scheduler = scheduler.new()
    self.mainBus = eventbus.new(self.scheduler)
    self.craftingBus = eventbus.craftingBus(self.scheduler, self.mainBus)
    self.port = 11037
    self.ext = {}

    initHooks(self)
    initEventPuller(self)
end

function openEnergistics:schedule()
    self.scheduler:schedule()
end

function openEnergistics:extra()
    return self.ext
end

function oe.new()
    local ins = {}
    setmetatable(ins, {__index=openEnergistics})
    ins:init()
    return ins
end

return oe