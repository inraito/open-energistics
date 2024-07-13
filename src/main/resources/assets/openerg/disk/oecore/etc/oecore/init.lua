local openerg = require('oe')

local oe = openerg.new()
oe:listen(11037)

oecore_args1 = oe
dofile('./initCrafting.lua')

oe:schedule()


