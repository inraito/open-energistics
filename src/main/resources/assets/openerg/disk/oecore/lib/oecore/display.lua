local term = require("term")
local event = require("event")

local display = {}

---@class DisplayState
---@field data table
---@field currentPage number
---@field maxRows number
---@field columns table
---@field refreshCounter number
local DisplayState = {}
DisplayState.__index = DisplayState

function DisplayState.new(data, maxRows, columns)
    return setmetatable({
        data = data or {},
        currentPage = 1,
        maxRows = maxRows or 10,
        columns = columns or {"id", "Name", "\tProgress"},
        refreshCounter = 0
    }, DisplayState)
end

function display.render(state)
    term.clear()
    local startIdx = (state.currentPage - 1) * state.maxRows + 1
    local endIdx = math.min(startIdx + state.maxRows - 1, #state.data)

    print(string.format("Page %d/%d", state.currentPage, math.ceil(#state.data / state.maxRows)))

    -- Render header
    local header = ""
    for i, col in ipairs(state.columns) do
        header = header .. string.format("%-16s", col)
    end
    print(header)

    -- Render rows
    for i = startIdx, endIdx do
        local row = state.data[i]
        if row then
            local line = ""
            for _, value in ipairs(row) do
                line = line .. string.format("%-16s", tostring(value))
            end
            print(line)
        end
    end

    print("\nPress ↑/↓ to navigate, Q to quit.")
end

function display.handleInput(state, eventName, address, char, code)
    if eventName == "key_down" then
        if code == 200 then -- ↑
            state.currentPage = math.max(1, state.currentPage - 1)
        elseif code == 208 then -- ↓
            state.currentPage = math.min(math.ceil(#state.data / state.maxRows), state.currentPage + 1)
        elseif char == string.byte("q") or char == string.byte("Q") then -- Quit
            return false
        end
    end
    return true
end

function display.run(fetchDataCallback, maxRows, columns, refreshInterval)
    local state = DisplayState.new(fetchDataCallback(), maxRows, columns)
    local running = true

    refreshInterval = refreshInterval or 5

    while running do
        if state.refreshCounter >= refreshInterval then
            state.data = fetchDataCallback()
            state.refreshCounter = 0
        end

        display.render(state)
        state.refreshCounter = state.refreshCounter + 1

        local eventName, address, char, code = event.pull(0.5, "key_down")
        if eventName then
            running = display.handleInput(state, eventName, address, char, code)
        end
    end

    term.clear()
    print("Exited display module.")
end


function display.drawProgressBar(current, max, width)
    width = width or 16
    local progress = math.floor((current / max) * width)
    local bar = "\t[" .. string.rep("■", progress) .. string.rep(" ", width - progress) .. "|" .. string.format(" %d/%d", current, max) .. "]"
    return bar
end


-- Test function
-- Test with command:
-- `display.run(virtualFetch)`
exampleRefreshCounter = 0

function virtualFetch()
    exampleRefreshCounter = exampleRefreshCounter + 1
    if exampleRefreshCounter > 100 then
        exampleRefreshCounter = 0
    end
    return {
        {1, "Example Progress", display.drawProgressBar(exampleRefreshCounter, 20)}
    }

end

return display
