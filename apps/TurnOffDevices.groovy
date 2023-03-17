definition(
    name: "Turn Off Devices",
    namespace: "lakhanibytes-HubbaHubba",
    author: "Lakhani Bytes",
    description: "Turn off all switches and dimmers at a user-selected time, sunset, or sunrise, with an optional random delay, excluding specified devices.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    section("Select time, sunset, or sunrise to turn off devices:") {
        input "offTimeOption", "enum", title: "Off Time Option", options: ["Specific Time", "Sunset", "Sunrise"], required: true
        input "offTime", "time", title: "Specific Off Time", defaultValue: "00:30", required: false
    }
    section("Random delay settings:") {
        input "useRandomDelay", "bool", title: "Enable Random Delay", defaultValue: false, required: false
        input "maxRandomDelay", "number", title: "Max Random Delay (minutes)", range: "0..*", defaultValue: 30, required: false
    }
    section("Exclude devices:") {
        input "excludedDevices", "capability.switch", title: "Excluded Devices", multiple: true, required: false
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    initialize()
}

def initialize() {
    switch (offTimeOption) {
        case "Specific Time":
            schedule(offTime, turnOffDevices)
            break
        case "Sunset":
            schedule(location.sunsetTime, turnOffDevices)
            break
        case "Sunrise":
            schedule(location.sunriseTime, turnOffDevices)
            break
    }
}

def turnOffDevices() {
    if (useRandomDelay) {
        def delay = new Random().nextInt(maxRandomDelay + 1) * 60
        runIn(delay, executeTurnOff)
    } else {
        executeTurnOff()
    }
}

def executeTurnOff() {
    def devicesToTurnOff = getAllSwitchesAndDimmers() - excludedDevices
    devicesToTurnOff.each { device ->
        device.off()
    }
}

def getAllSwitchesAndDimmers() {
    def switches = location.devices.findAll { it.hasCapability("switch") }
    return switches
}
