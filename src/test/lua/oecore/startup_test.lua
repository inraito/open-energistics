local component = require('component')
local modemPort, interfacePort
component.modem = {open = function(port) modemPort = port end}
component.oe_interface = {setPort = function(port) interfacePort = port end}
-- OpenOS launches this script with /etc/oecore as its working directory.
local diskFile = dofile
dofile = function(path) return diskFile((path:gsub('^%./', '/etc/oecore/'))) end
local firstPass = {}
os.sleep = function() error(firstPass) end
local ok, reason = pcall(dofile, '/etc/oecore/init.lua')
assert(not ok and reason == firstPass, tostring(reason))
assert(modemPort == 11037 and modemPort == interfacePort)
assert(oecore_args1.port == modemPort and oecore_args1:extra().mm == nil)
