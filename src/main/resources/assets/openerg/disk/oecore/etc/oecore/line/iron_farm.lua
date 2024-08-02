--- This is a simple of iron farm with Create. And since it's a sample,
--- i'll have the registration of machines attached here rather than in a
--- separate file, to make running it simpler for users.

local oe = oecore_args1

-- do remember to set the oemm variable in /etc/oecore/init.lua to true
-- if you want to use this.
---@type oemm
local mm = oe:extra().mm
local component = require('component')
local card = component.me_switching_card -- change this when necessary

-------------------------------------------------------------------------

-- Registration of machines. In real life, this would be in a separate
-- file, probably in '/etc/oemm'.
local MachineType = require('oemm.suite.MachineType')
local Machine = require('oemm.suite.Machine')

-- stone generator
local stoneGen = MachineType:new()
stoneGen:slot('out')
mm:registerType(stoneGen, 'stone_gen')
local ins = Machine:new(stoneGen)
ins:slot('out', 'stub!', 0)
ins:assert()
mm:registerMachine('stond_gen', ins)

-- grinder
local grind = MachineType:new()
grind:slot('in')
grind:slot('out')
mm:registerType(grind, 'grinder')
local ins = Machine:new(grind)
ins:slot('in', 'stub!', 0)
ins:slot('out', 'stub!', 0)
ins:assert()
mm:registerMachine('grinder', ins)

-- washer
local washer = MachineType:new()
washer:slot('in')
washer:slot('out')
mm:registerType(washer, 'washer')
local ins = Machine:new(washer)
ins:slot('in', 'stub!', 0)
ins:slot('out', 'stub!', 0)
ins:assert()
mm:registerMachine('washer', ins)

-- output, for instance, an ME interface.
local output = MachineType:new()
output:slot('in')
mm:registerType(output, 'output')
local ins = Machine:new(output)
ins:slot('in', 'stub!', 0)
ins:assert()
mm:registerMachine('output', ins)

-------------------------------------------------------------------------

-- Realize the assembly line once, you could realize one multiple times as
-- long as you have enough machines. That's why we call the virtual assembly
-- line powerful.
local pipe = require('oecore.pipe')

-- Recode descriptors so that we can free these machines afterwards.
-- But we have no external controls to free them, so it's useless here.
local descriptor = {}
local s, g, w, o
descriptor[1], s = mm:alloc('stone_gen')
descriptor[2], g = mm:alloc('grinder')
descriptor[3], w = mm:alloc('washer')
descriptor[4], o = mm:alloc('output')
-- Same as above.
local pipes = {}
pipes[1] = pipe.new({
    type = 'machine',
    machine = s,
    id = 'out'
}, {
    type = 'machine',
    machine = g,
    id = 'in'
})
pipes[2] = pipe.new({
    type = 'machine',
    machine = g,
    id = 'out'
}, {
    type = 'machine',
    machine = w,
    id = 'in'
})
pipes[3] = pipe.new({
    type = 'machine',
    machine = w,
    id = 'out'
}, {
    type = 'machine',
    machine = o,
    id = 'in'
})

for _, p in pairs(pipes) do
    p:start(oe.scheduler, card)
end

-- Example on how to free a line:
--
-- for _, p in pairs(pipe) do
--   p:stop()
-- end
-- for _, d in pairs(descriptor) do
--   mm:free(d)
-- end

