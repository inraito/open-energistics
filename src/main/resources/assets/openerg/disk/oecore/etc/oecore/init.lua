local openerg = require('oe')
local component = require('component')

local oe = openerg.new()
local port = 11037
oe:listen(port)

oecore_args1 = oe
oecore_args2 = {
    'stub', -- addr
    0       -- slot
}

print('Opening modem port.')
component.modem.open(port)

print('Setting OE Interface port')
component.oe_interface.setPort(port)

print('Initializing crafting hooks.')
dofile('./initCrafting.lua')

print('OECore starting to schedule.')
oe:schedule()


