local oemm = {} -- Open Energistics Machine Manager

oemm.stateDict = {
    Initializing    = 'init',
    Running         = 'running',
    Closing         = 'closing',
    Closed          = 'closed'
}

function oemm.new(initPath)
    if initPath == nil then
        error('`initPath` is nil')
    end
    oemm_args = {initScript=initPath}
    local mmInstance = dofile('/lib/oemm/oemm.lua')
    oemm_args = nil
    return mmInstance
end

function oemm.shutdown(mmInstance)
    local state = mmInstance.metadata.state
    if state == oemm.stateDict.Initializing then
        return nil, 'instance is initializing, and could not be shutdown now'
    elseif state == oemm.stateDict.Closed then
        return true
    else
        return mmInstance:shutdown()
    end
end

return oemm