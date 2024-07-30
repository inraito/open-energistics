--- Initialize assembly line.
--- unlike the crafting, it seems more reasonable if we have a network handler
--- to provide external controls of when and which assembly line should be
--- turned on or off, and some options to toggle maybe.
--- But i will simply leave it to you :p

local oe = oecore_args1

--- (Cont.) Therefore, the sample here basically shows how to use oecore and
--- mm(actually oemm) to create an virtual assembly line and how to realize it,
--- yet without any controls. We just start, or realize it once whenever oecore
--- is started.
--- P.S. oemm is compulsory for this sample code to run, so make sure it is installed.
--dofile('./line/iron_farm.lua')