local seen = {}
require('component').block_controller = {
    map = function(index, relative, side, slot)
        assert(not seen[index])
        seen[index] = {relative, side, slot}
    end
}
dofile('/bin/oe_map.lua')
local directions = {'up', 'down', 'south', 'north', 'west'}
for i, direction in ipairs(directions) do
    for j, endpoint in ipairs({{'up', 0}, {'down', 0}, {'east', 0}, {'east', 1}}) do
        local actual = seen[4*(i-1) + j-1]
        assert(actual[1] == direction and actual[2] == endpoint[1] and actual[3] == endpoint[2])
    end
end
