-- Exercise the production error path without oc.run's test-only resume wrapper.
local scheduler = require('oecore.scheduler').new()
local diagnostic
local function record(text)
    if tostring(text):find('recipe failure', 1, true) then diagnostic = true end
end
print = record
local hookRan = false
scheduler:add(coroutine.create(function() error('recipe failure') end), function(reason)
    hookRan = true
    record(reason)
end)
local passComplete = {}
os.sleep = function() error(passComplete) end
local ok, reason = pcall(function() scheduler:schedule() end)
if not ok and reason ~= passComplete then record(reason) end
assert(diagnostic, 'coroutine failed; reason discarded, completion hook ran=' .. tostring(hookRan))
