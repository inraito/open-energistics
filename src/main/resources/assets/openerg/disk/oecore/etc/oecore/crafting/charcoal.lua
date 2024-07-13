local oe = oecore_args1
local pipe = require('oecore.pipe')

---@param event event
local function callback(event)
    local payload = event:getPayload()
    local localAddr = payload.localAddr
    local remoteAddr = payload.remoteAddr
    local sequenceHead = payload.sequenceHead
    local sequenceTail = payload.sequenceTail
end

bus:register('charcoal', callback)