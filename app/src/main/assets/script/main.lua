

require 'x'
require 'log'
require 'image'
require 'screencap'
require 'event'

require 'enhance'

local function main()
--    log.trace("trace")
--    log.debug("debug")
--    log.info("info")
--    log.warn("warn")
--    log.error("error")
--    log.log(log.TRACE, "log -> trace")
--    log.log(log.DEBUG, "log -> debug")
--    log.log(log.WARN, "log -> warn")
--    log.log(log.INFO, "log -> info")
--    log.log(log.ERROR, "log -> error")

    log.info("x.getPathTemp(): " .. x.getPathTemp())
    log.info("x.getPathData(): " .. x.getPathData())
    log.info("x.getPathScript(): " .. x.getPathScript())
    log.info("x.getPathTemp('temp'): " .. x.getPathTemp('temp'))
    log.info("x.getPathData('data'): " .. x.getPathData('data'))
    log.info("x.getPathScript('script'): " .. x.getPathScript('script'))

    local src = screencap.capture(nil)
    local p = image.findColor(src, nil, image.argb(0, 0, 0, 0), image.TOP_LEFT, 0.1)
    log.info(tostring(p))

    local onPong = function(name, data)
        log.info("1 got " .. name .. " " .. tostring(data))
    end
    event.add("pong", onPong)
    event.dispatch("ping", {"hi"})
    event.remove("pong", onPong)
    event.dispatch("ping", {"hello"})
end

main()





