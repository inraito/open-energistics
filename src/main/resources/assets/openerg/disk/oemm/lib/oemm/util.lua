local util = {}

function util.size(table)
    local count = 0
    for _ in pairs(table) do
        count = count + 1
    end
    return count
end

return util