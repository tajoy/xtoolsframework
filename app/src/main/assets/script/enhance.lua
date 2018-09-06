--
-- Created by IntelliJ IDEA.
-- User: jacky-pro
-- Date: 2018/9/6
-- Time: 下午5:26
-- To change this template use File | Settings | File Templates.
--


local function table_val_to_str ( v )
    if "string" == type( v ) then
        v = string.gsub( v, "\n", "\\n" )
        if string.match( string.gsub(v,"[^'\"]",""), '^"+$' ) then
            return "'" .. v .. "'"
        end
        return '"' .. string.gsub(v,'"', '\\"' ) .. '"'
    else
        return "table" == type( v ) and table.tostring( v ) or
                tostring( v )
    end
end

local function table_key_to_str ( k )
    if "string" == type( k ) and string.match( k, "^[_%a][_%a%d]*$" ) then
        return k
    else
        return "[" .. table_val_to_str( k ) .. "]"
    end
end

local function table_tostring( tbl )
    local result, done = {}, {}
    for k, v in ipairs( tbl ) do
        table.insert( result, table_val_to_str( v ) )
        done[ k ] = true
    end
    for k, v in pairs( tbl ) do
        if not done[ k ] then
            table.insert( result,
                table_key_to_str( k ) .. "=" .. table_val_to_str( v ) )
        end
    end
    return "{" .. table.concat( result, "," ) .. "}"
end

local mt_table = getmetatable(table) or {}
mt_table["__tostring"] = table_tostring
setmetatable(table, mt_table)


