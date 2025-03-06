import java.io.File
import java.util.PriorityQueue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class Booking(
    val id: Int,
    val guestName: String,
    val guestType: String,
    val checkInDate: String,
    val checkOutDate: String,
    val stayDuration: Int
) : Comparable<Booking> {
    override fun compareTo(other: Booking): Int {
        return when {
            this.guestType == "VIP" && other.guestType != "VIP" -> -1
            this.guestType != "VIP" && other.guestType == "VIP" -> 1
            this.stayDuration > other.stayDuration -> -1
            this.stayDuration < other.stayDuration -> 1
            else -> 0
        }
    }
}

class HotelBookingSystem {
    private val bookingQueue = PriorityQueue<Booking>()
    private val objectMapper = jacksonObjectMapper()
    private val jsonFile = "bookings.json"

    init {
        loadBookings()
    }

    private fun loadBookings() {
        if (File(jsonFile).exists()) {
            val bookings: List<Booking> = objectMapper.readValue(File(jsonFile))
            bookingQueue.addAll(bookings)
        }
    }

    private fun saveBookings() {
        objectMapper.writeValue(File(jsonFile), bookingQueue.toList())
    }

    fun addBooking() {
        println("\nEnter Guest Name: ")
        val name = readln()
        println("Enter Guest Type (VIP/Regular): ")
        val type = readln()
        println("Enter Check-in Date(YYYY-MM-DD): ")
        val checkIn = readln()
        println("Enter Check-out Date (YYYY-MM-DD): ")
        val checkOut = readln()
        println("Enter Stay Duration (in days): ")
        val duration = readln().toInt()

        val booking = Booking(
            id = bookingQueue.size + 1,
            guestName = name,
            guestType = type.uppercase(),
            checkInDate = checkIn,
            checkOutDate = checkOut,
            stayDuration = duration
        )

        bookingQueue.add(booking)
        saveBookings()
        println("Booking Added Successfully!\n")
    }

    fun processBooking() {
        if (bookingQueue.isNotEmpty()) {
            val booking = bookingQueue.poll()
            saveBookings()
            println("\nProcessing Booking for: ${booking.guestName}")
            println("Guest Type: ${booking.guestType}")
            println("Stay Duration: ${booking.stayDuration} days")
            println("Check-in Date: ${booking.checkInDate}")
            println("Check-out Date: ${booking.checkOutDate}\n")
        } else {
            println("\nNo Bookings Available!\n")
        }
    }

    fun listBookings() {
        if (bookingQueue.isNotEmpty()) {
            println("\n--- Booking List (Priority Order) ---")
            bookingQueue.forEach {
                println("${it.guestName} (${it.guestType}) - ${it.stayDuration} days")
            }
            println("------------------------------------\n")
        } else {
            println("\nNo Bookings Available!\n")
        }
    }
}

fun main() {
    val hotelSystem = HotelBookingSystem()
    while (true) {
        println("\n=== Hotel Booking System ===")
        println("1. Add Booking")
        println("2. Process Booking")
        println("3. View Booking List")
        println("4. Exit")
        print("Choose an option: ")

        when (readln()) {
            "1" -> hotelSystem.addBooking()
            "2" -> hotelSystem.processBooking()
            "3" -> hotelSystem.listBookings()
            "4" -> {
                println("Exiting Hotel Booking System. Goodbye!")
                break
            }
            else -> println("Invalid choice, please try again.")
        }
    }
}