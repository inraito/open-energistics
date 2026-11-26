-- Run the stock OpenOS installer planner with one read-only floppy and one system drive.
local disk = ...
local root = {address = 'system', isReadOnly = function() return false end}
local floppy = {address = 'floppy', isReadOnly = function() return true end}
floppy.getLabel = function() return disk end
local function canonical(path) return (path:gsub('/+', '/')) end
local fs = {
    get = function() return root end,
    mounts = function() return pairs({[root] = '/', [floppy] = '/mnt/disk'}) end,
    list = function() return function() return 'performance' end end,
    open = function(path)
        assert(canonical(path) == '/mnt/disk/.prop')
        return {read = function() return properties end, close = function() end}
    end,
    exists = function() return false end,
    canonical = canonical,
    concat = function(...) return canonical(table.concat({...}, '/')) end
}
package.loaded.filesystem = fs
package.loaded.computer = {tmpAddress = function() return 'temp' end}
package.loaded.component = {list = function() return {system = true, floppy = true} end}
package.loaded.shell = {parse = function(label, options) return {label}, options end}
io.read = function() return 'y' end

for _, choice in ipairs({'default', 'performance', 'robust'}) do
    local options = choice == 'default' and {} or {fromDir = choice}
    local plan = assert(installer(disk, options))
    local edition = choice == 'default' and 'performance' or choice
    assert(plan.label == disk and plan.target.dev == root)
    local copy = plan.cp_args[1]
    assert(copy[1][1] == '/mnt/disk/' .. edition .. '/.' and copy[1][2] == '/')
    assert(copy[2].r)
    local init = disk == 'oecore' and 'etc/oecore/init.lua' or 'etc/oemm/mm1/init.lua'
    local protected = false
    for i = 2, #plan.cp_args do
        local action = plan.cp_args[i]
        if action[1][1] == '/mnt/disk/' .. edition .. '/' .. init then
            protected = action[1][2] == '/' .. init and action[2].n
        end
    end
    assert(protected, 'existing configuration would be overwritten')
end
