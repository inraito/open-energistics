local scheduler = require('oecore.scheduler')
local eventbus = require('oecore.eventbus')

local oe = {}

local openEnergistics = {}

function openEnergistics:init()
    self.scheduler = scheduler.new()
    self.mainBus = eventbus.new(self.scheduler)
    self.craftingBus = eventbus.new(self.scheduler)
end



function oe.new()
    local ins = {}
    setmetatable(ins, {__index=openEnergistics})
    ins:init()
    return ins
end

return oe