import java.io.File
import com.fasterxml.jackson.module.kotlin.*
import kotlinx.coroutines.*
import java.util.LinkedList
import java.util.Queue
import kotlin.system.exitProcess

enum class TrafficLight { RED, YELLOW, GREEN }

data class Vehicle(val id: Int, val type: String)

class TrafficSimulation {
    private var light: TrafficLight = TrafficLight.RED
    private val queue: Queue<Vehicle> = LinkedList()
    private val jsonFile = "traffic_data.json"
    private var vehicleCounter = 1

    init {
        loadFromJson()
    }

    fun changeLight() {
        light = when (light) {
            TrafficLight.RED -> TrafficLight.GREEN
            TrafficLight.GREEN -> TrafficLight.YELLOW
            TrafficLight.YELLOW -> TrafficLight.RED
        }
        println("Traffic Light changed to $light")

        if (light == TrafficLight.GREEN) {
            processQueue()
        }
    }

    fun addVehicle(type: String) {
        val vehicle = Vehicle(vehicleCounter++, type)
        queue.add(vehicle)
        println("Vehicle ${vehicle.id} ($type) added to queue.")
        saveToJson()
    }

    private fun processQueue() {
        if (queue.isEmpty()) {
            println("No vehicles to pass.")
            return
        }

        println("Vehicles passing through...")
        while (queue.isNotEmpty()) {
            val vehicle = queue.poll()
            println("Vehicle ${vehicle.id} (${"%-8s".format(vehicle.type)}) passed!")
            delaySimulation(500)
        }
        saveToJson()
    }

    fun showQueue() {
        if (queue.isEmpty()) {
            println("No vehicles in queue.")
        } else {
            println("Current Vehicles in Queue:")
            queue.forEach { println(" - Vehicle ${it.id} (${it.type})") }
        }
    }

    private fun saveToJson() {
        val mapper = jacksonObjectMapper()
        val jsonData = mapper.writeValueAsString(queue)
        File(jsonFile).writeText(jsonData)
    }

    private fun loadFromJson() {
        val file = File(jsonFile)
        if (file.exists()) {
            val mapper = jacksonObjectMapper()
            val jsonData = file.readText()
            val loadedQueue: List<Vehicle> = mapper.readValue(jsonData)
            queue.addAll(loadedQueue)
            println("Loaded ${queue.size} vehicles from previous session.")
        }
    }

    private fun delaySimulation(timeMillis: Long) {
        runBlocking { delay(timeMillis) }
    }
}

fun main() {
    val simulation = TrafficSimulation()

    while (true) {
        println("\n Traffic Light Simulation")
        println("1. Change Traffic Light")
        println("2. Add Vehicle to Queue")
        println("3. Show Vehicle Queue")
        println("4. Exit")
        print("Choose an option: ")

        when (readlnOrNull()?.trim()) {
            "1" -> simulation.changeLight()
            "2" -> {
                print("Enter Vehicle Type (Car, Truck, Bus, etc.): ")
                val type = readlnOrNull()?.trim() ?: "Car"
                simulation.addVehicle(type)
            }
            "3" -> simulation.showQueue()
            "4" -> {
                println("Exiting Simulation...")
                exitProcess(0)
            }
            else -> println("Invalid Option. Try again.")
        }
    }
}