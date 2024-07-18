local openerg = require('oe')
local component = require('component')

local oe = openerg.new()
local port = 11037
oe:listen(port)

oecore_args1 = oe

--- This is the slot to put recipes' results back to
--- AE network. Generally this should be a slot in
--- ME Interface that are attached and configured to
--- a adjacent Block Controller.
---
--- Making OE Interface have a specific slot for
--- returning stacks back to AE network seems quite
--- reasonable, but I may fix the OE Interface to
--- be robust to sequence overflow by looping by to zero
--- when overflow, then having a specific slot occupied
--- would be annoying.
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


