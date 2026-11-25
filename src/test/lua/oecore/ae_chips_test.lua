local oe = require('oe').new()
oecore_args1 = oe
oecore_args2 = {'output', 7}

local function item(name)
    return {registry_name = name, num = 1}
end

local ingredients = {
    [19] = item('unrelated'),
    [20] = item('printed_logic'),
    [21] = item('redstone'),
    [22] = item('printed_silicon')
}
local inscriber, output = {}, {}
-- The example uses "stub!" as its configured controller address.
require('component').me_switching_card = oc.switchingCard({
    interface = ingredients, ['stub!'] = inscriber, output = output
})
dofile('/etc/oecore/crafting/ae_chips.lua')

oc.signal('modem_message', 'computer', 'interface', 11037, 0, 'oe_interface:AE2:logic', 20, 23)
oc.run(oe.scheduler, 6)
assert(ingredients[19].num == 1 and not ingredients[20] and not ingredients[21] and not ingredients[22])
assert(inscriber[8].registry_name == 'printed_logic')
assert(inscriber[10].registry_name == 'redstone' and inscriber[9].registry_name == 'printed_silicon')
assert(output[7] == nil, 'output must wait for the inscriber')

-- Deliver the machine result on a later pass, as a real inscriber would.
inscriber[11] = item('logic_processor')
oc.run(oe.scheduler, 3)
assert(inscriber[11] == nil and output[7].registry_name == 'logic_processor' and output[7].num == 1)
