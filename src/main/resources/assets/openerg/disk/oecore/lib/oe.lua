local scheduler = require('oecore.scheduler')
local eventbus = require('oecore.eventbus')
local event = require('event')
local oeEvent = require('oecore.event')
local string = require('string')

local oe = {}

local openEnergistics = {}

local function initHooks(self)
    event.listen('modem_message', function(localAddr, remoteAddr, port, distance, ...)
        local id = string.match(arg[1], 'oe_interface:(.*)', 1)
        if port == self.port and id~=nil then
            local payload = {
                localAddr = localAddr,
                remoteAddr = remoteAddr,
                sequenceHead = arg[2],
                sequenceTail = arg[3]
            }
            local e = oeEvent.new(id, payload)
            self.craftingBus:publish(e)
        end
    end)
end

function openEnergistics:listen(port)
    self.port = port
end

function openEnergistics:init()
    self.scheduler = scheduler.new()
    self.mainBus = eventbus.new(self.scheduler)
    self.craftingBus = eventbus.new(self.scheduler)
    self.port = 11037

    initHooks(self)
end

function openEnergistics:schedule()
    self.scheduler:schedule()
end

function oe.new()
    local ins = {}
    setmetatable(ins, {__index=openEnergistics})
    ins:init()
    return ins
end

return oe