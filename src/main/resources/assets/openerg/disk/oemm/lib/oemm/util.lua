local util = {}

function util.size(table)
    local count = 0
    for _ in pairs(table) do
        count = count + 1
    end
    return count
end

function util.keys(_table)
    local res = {}
    for k, _ in pairs(_table) do
        table.insert(res, k)
    end
    return res
end

return util