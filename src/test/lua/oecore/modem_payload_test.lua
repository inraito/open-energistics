local oe = require('oe').new()
local calls = 0
oe.craftingBus:register('valid', function() calls = calls + 1 end)
oc.signal('modem_message', 'local', 'foreign', 42, 0, false)
oc.signal('modem_message', 'local', 'interface', 11037, 0, 'oe_interface:valid', 0, 1)
oc.run(oe.scheduler, 4)
assert(calls == 1, 'valid job following unrelated packet did not arrive')
