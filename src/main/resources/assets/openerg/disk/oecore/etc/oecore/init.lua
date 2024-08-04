local openerg = require('oe')
local component = require('component')

---@type OpenEnergistics
local oe = openerg.new()
local port = 11037
local oemm = false -- set it to true to if you have oemm installed and want to use it.
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
--- be robust to sequence overflow by looping back to zero
--- when overflow, then having a specific slot occupied
--- would be annoying.
oecore_args2 = {
    'stub', -- addr
    0       -- slot
}

if oemm then
    local mm = require('oemm')
    oe:extra().mm = mm.new('/etc/oemm/mm1/init.lua')
end

local ascii_art =[[
***************************************************
*  ___                                            *
* / _ \ _ __   ___ _ __                           *
*| | | | '_ \ / _ \ '_ \                          *
*| |_| | |_) |  __/ | | |                         *
* \___/| .__/ \___|_| |_|                         *
*      |_|                                        *
* _____                      _     _   _          *
*| ____|_ __   ___ _ __ __ _(_)___| |_(_) ___ ___ *
*|  _| | '_ \ / _ \ '__/ _` | / __| __| |/ __/ __|*
*| |___| | | |  __/ | | (_| | \__ \ |_| | (__\__ \*
*|_____|_| |_|\___|_|  \__, |_|___/\__|_|\___|___/*
*                      |___/                      *
***************************************************]]
print(ascii_art)

print('Opening modem port.')
component.modem.open(port)

print('Setting OE Interface port')
component.oe_interface.setPort(port)

print('Initializing crafting hooks.')
dofile('./initCrafting.lua')

print('Initializing assembly line hooks.')
dofile('./initLine.lua')

print('OECore starting to schedule.')
oe:schedule()

--- We don't design any way to stop the OE yet.
--- So the official way to exit it is to reboot the computer :(
--- or you could implement it yourself :)
--- illegal state caused by reboot is a known issue, so it's
--- recommended that you don't :p