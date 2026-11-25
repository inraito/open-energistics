local oe = require('oe').new()
local calls = 0
oe.craftingBus:register('valid', function() calls = calls + 1 end)
oc.signal('modem_message', 'local', 'other', 11037, 0, 'other:oe_interface:valid', 1, 2)
oc.run(oe.scheduler, 3)
assert(calls == 0, 'unrelated message was dispatched as an OE job')
