import com.fasterxml.jackson.module.kotlin.*
import java.io.File
import java.util.PriorityQueue
import kotlin.system.exitProcess

enum class OrderPriority(val level: Int) {
    VIP(3), REGULAR(2), TAKEOUT(1)
}

data class Order(
    val id: Int,
    val customerName: String,
    val orderType: OrderPriority,
    val items: List<String>
) : Comparable<Order> {
    override fun compareTo(other: Order): Int = other.orderType.level - this.orderType.level
}

val orderQueue = PriorityQueue<Order>()

const val JSON_FILE = "orders.json"
val mapper = jacksonObjectMapper()

fun loadOrders() {
    val file = File(JSON_FILE)
    if (file.exists() && file.readText().isNotEmpty()) {
        val orders: List<Order> = mapper.readValue(file)
        orderQueue.addAll(orders)
    }
}

fun saveOrders() {
    File(JSON_FILE).writeText(mapper.writeValueAsString(orderQueue.toList()))
}

fun placeOrder() {
    println("Enter Customer Name:")
    val name = readLine()!!

    println("Select Order Type (1- VIP, 2- Regular, 3- Takeout):")
    val type = when (readLine()!!.toInt()) {
        1 -> OrderPriority.VIP
        2 -> OrderPriority.REGULAR
        else -> OrderPriority.TAKEOUT
    }

    println("Enter Items (comma-separated):")
    val items = readLine()!!.split(",").map { it. trim() }

    val orderId = (orderQueue.maxByOrNull { it.id }?.id ?: 0) + 1
    val order = Order(orderId, name, type, items)
    orderQueue.add(order)

    println("Order #$orderId placed successfully!")
    saveOrders()
}

fun modifyOrder() {
    println("Enter Order ID to Modify:")
    val id = readLine()!!.toInt()

    val order = orderQueue.find { it.id == id }
    if (order != null) {
        orderQueue.remove(order)

        println("Enter New Items (comma-separated):")
        val newItems = readLine()!!.split(",").map { it.trim() }

        val newOrder = order.copy(items = newItems)
        orderQueue.add(newOrder)
        println("Order #$id modified successfully!")
        saveOrders()
    } else {
        println("Order not found.")
    }
}

fun cancelOrder() {
    println("Enter Order ID to Cancel:")
    val id = readLine()!!.toInt()

    val order = orderQueue.find { it.id == id }
    if (order != null) {
        orderQueue.remove(order)
        println("Order #$id cancelled.")
        saveOrders()
    } else {
        println("Order not found.")
    }
}

fun processOrder() {
    if (orderQueue.isNotEmpty()) {
        val order = orderQueue.poll()
        println("Processing Order #${order.id} for ${order.customerName} (type: ${order.orderType})")
        saveOrders()
    } else {
        println("No orders to process.")
    }
}

fun main() {
    loadOrders()

    while (true) {
        println("\nRestaurant Order System")
        println("1. Place Order")
        println("2. Modify Order")
        println("3. Cancel Order")
        println("4. Process Next Order")
        println("5. View Pending Orders")
        println("6. Exit")

        when (readLine()!!.toInt()) {
            1 -> placeOrder()
            2 -> modifyOrder()
            3 -> cancelOrder()
            4 -> processOrder()
            5 -> println(orderQueue)
            6 -> {
                println("Exiting...")
                exitProcess(0)
            }
            else -> println("Invalid choice. Try again.")
        }
    }
}