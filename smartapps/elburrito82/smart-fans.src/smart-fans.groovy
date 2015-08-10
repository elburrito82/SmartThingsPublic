/**
 *  Smart fans
 *
 *  Copyright 2015 David Nelson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Smart fans",
    namespace: "elburrito82",
    author: "David Nelson",
    description: "turns on a fan if motion is detected, and it's above a certain temperature",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Choose a motion sensor"){
		input "motion1", "capability.motionSensor", title: "Where?"
    }    
	section("Choose a temperature sensor... "){
		input "sensor1", "capability.temperatureMeasurement", title: "Temp Sensor"
    }
    section("When the temperature is above...") {
		input "temperature1", "number", title: "Temp?"
	}
	section("Turn off when there's been no motion for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off switches..."){
		input "switches", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(motion1, "motion", motionHandler)
	schedule("0 * * * * ?", "scheduleCheck")
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
	unschedule()
	schedule("0 * * * * ?", "scheduleCheck")
}

def motionHandler(evt) {
 log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		def lastTemp = sensor1.currentTemperature
		if (lastTemp >= temperature1){
			log.debug "turning on fans"
			switches.on()
			state.inactiveAt = null
            }
	} else if (evt.value == "inactive") {
		if (!state.inactiveAt) {
			state.inactiveAt = now()
		}
  	}
}

def scheduleCheck() {
	log.debug "schedule check, ts = ${state.inactiveAt}"
	if (state.inactiveAt) {
		def elapsed = now() - state.inactiveAt
		def threshold = 1000 * 60 * minutes1
		if (elapsed >= threshold) {
			log.debug "turning off fans"
			switches.off()
			state.inactiveAt = null
		}
		else {
			log.debug "${elapsed / 1000} sec since motion stopped"
		}
	}
}

// TODO: implement event handlers