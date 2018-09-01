


require 'x'
require 'log'
--require 'screencap'
--requrie 'image'


local function main()
    log.trace("trace")
    log.debug("debug")
    log.info("info")
    log.warn("warn")
    log.error("error")
    log.log(log.TRACE, "log -> trace")
    log.log(log.DEBUG, "log -> debug")
    log.log(log.WARN, "log -> warn")
    log.log(log.INFO, "log -> info")
    log.log(log.ERROR, "log -> error")

    log.info("x.getPathTemp(): " .. x.getPathTemp())
    log.info("x.getPathData(): " .. x.getPathData())
    log.info("x.getPathScript(): " .. x.getPathScript())

--    x.delay(10000)
--    local pic = screencap.capture()
end

main()





