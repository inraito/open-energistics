local oemm = oemm_args1
local instance = oemm_args2

local component = require('component')
local MachineType = require('oemm.suite.MachineType')
local Machine = require('oemm.suite.Machine')

local dict = {
    FURNACE = 'furnace',
    FUEL = 'fuel',
    INGREDIENT = 'ing',
    PRODUCT = 'pro'
}
local furnace = MachineType:new({
    init = {
        enum = dict
    }
})

furnace:slot(dict.FUEL)
furnace:slot(dict.INGREDIENT)
furnace:slot(dict.PRODUCT)
instance:registerType(furnace, dict.FURNACE)

local controllerADDR = component.get('stub!') -- put the right addr of a block controller here
local machine
for i = 1,2 do
    -- so basically we assume that there are 2 furnace attached to the block controller
    -- and both of them have been properly configured with the following map:
    -- 1~3 slots mapped to fuel, ingredient and product slots of the first furnace,
    -- and 4~6 slot mapped the corresponding slots of the second furnace.
    machine = Machine:new(furnace)
    machine:slot(dict.FUEL, controllerADDR, i*3-2)
    machine:slot(dict.INGREDIENT, controllerADDR, i*3-1)
    machine:slot(dict.PRODUCT, controllerADDR, i*3)
    machine:assert()
    instance:registerMachine(dict.FURNACE, machine)
end

