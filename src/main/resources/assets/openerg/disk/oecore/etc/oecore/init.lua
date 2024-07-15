local openerg = require('oe')

local oe = openerg.new()
oe:listen(11037)

oecore_args1 = oe
oecore_args2 = {
    out_addr = 'stub',
    out_slot = 0
}

dofile('./initCrafting.lua')

oe:schedule()


