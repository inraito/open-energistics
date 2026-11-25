local oe = require('oe').new()
oecore_args1 = oe
oe:extra().mm = require('oemm').new('/etc/oemm/mm1/init.lua')
dofile('/etc/oecore/line/iron_farm.lua')
assert(#oe:extra().mm:listOccupied() == 4, 'expected four assigned machines')
