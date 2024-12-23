local reinstall = {}

function reload(moduleName)
    package.loaded[moduleName] = nil
    return require(moduleName)
end

return reinstall