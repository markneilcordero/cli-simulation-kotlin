import com.fasterxml.jackson.module.kotlin.*
import java.io.File

data class StockOrder(val id: Int, val type: String, val price: Double, val quantity: Int)

class AVLNode(val order: StockOrder, var height: Int = 1) {
    var left: AVLNode? = null
    var right: AVLNode? = null
}

class AVLTree {
    private var root: AVLNode? = null

    private fun height(node: AVLNode?): Int = node?.height ?: 0

    private fun balanceFactor(node: AVLNode?): Int = height(node?.left) - height(node?.right)

    private fun rotateRight(y: AVLNode): AVLNode {
        val x = y.left ?: return y
        y.left = x.right
        x.right = y
        y.height = maxOf(height(y.left), height(y.right)) + 1
        x.height = maxOf(height(x.left), height(x.right)) + 1
        return x
    }

    private fun rotateLeft(x: AVLNode): AVLNode {
        val y = x.right ?: return x
        x.right = y.left
        y.left = x
        x.height = maxOf(height(x.left), height(x.right)) + 1
        y.height = maxOf(height(y.left), height(y.right)) + 1
        return y
    }

    private fun balance(node: AVLNode): AVLNode {
        val balanceFactor = balanceFactor(node)
        return when {
            balanceFactor > 1 && balanceFactor(node.left) >= 0 -> rotateRight(node)
            balanceFactor > 1 && balanceFactor(node.left) < 0 -> {
                node.left = rotateLeft(node.left!!)
                rotateRight(node)
            }
            balanceFactor < -1 && balanceFactor(node.right) <= 0 -> rotateLeft(node)
            balanceFactor < -1 && balanceFactor(node.right) > 0 -> {
                node.right = rotateRight(node.right!!)
                rotateLeft(node)
            }
            else -> node
        }
    }

    fun insert(order: StockOrder) {
        root = insert(root, order)
    }

    private fun insert(node: AVLNode?, order: StockOrder): AVLNode {
        if (node == null) return AVLNode(order)

        if (order.price < node.order.price) {
            node.left = insert(node.left, order)
        } else {
            node.right = insert(node.right, order)
        }

        node.height = maxOf(height(node.left), height(node.right)) + 1
        return balance(node)
    }

    fun inOrderTraversal(): List<StockOrder> {
        val orders = mutableListOf<StockOrder>()
        inOrder(root, orders)
        return orders
    }

    private fun inOrder(node: AVLNode?, orders: MutableList<StockOrder>) {
        if (node != null) {
            inOrder(node.left, orders)
            orders.add(node.order)
            inOrder(node.right, orders)
        }
    }
}

class OrderBook {
    private val buyOrders = AVLTree()
    private val sellOrders = AVLTree()
    private val jsonMapper = jacksonObjectMapper()
    private val file = File("order_book.json")

    init {
        loadOrders()
    }

    private fun loadOrders() {
        if (!file.exists()) return
        val data: Map<String, List<StockOrder>> = jsonMapper.readValue(file.readText())
        data["buy"]?.forEach { buyOrders.insert(it) }
        data["sell"]?.forEach { sellOrders.insert(it) }
    }

    private fun saveOrders() {
        val data = mapOf(
            "buy" to buyOrders.inOrderTraversal(),
            "sell" to sellOrders.inOrderTraversal()
        )
        file.writeText(jsonMapper.writeValueAsString(data))
    }

    fun addOrder(order: StockOrder) {
        if (order.type == "buy") {
            buyOrders.insert(order)
        } else {
            sellOrders.insert(order)
        }
        matchOrders()
        saveOrders()
    }

    private fun matchOrders() {
        val buyList = buyOrders.inOrderTraversal().sortedByDescending { it.price }
        val sellList = sellOrders.inOrderTraversal().sortedBy { it.price }

        if (buyList.isNotEmpty() && sellList.isNotEmpty()) {
            val highestBuy = buyList.first()
            val lowestSell = sellList.first()

            if (highestBuy.price >= lowestSell.price) {
                println("Matched Order! ${highestBuy.quantity} shares at ${highestBuy.price}")
                buyOrders.insert(highestBuy.copy(quantity = highestBuy.quantity - 1))
                sellOrders.insert(lowestSell.copy(quantity = lowestSell.quantity - 1))
            }
        }
    }

    fun displayOrders() {
        println("\nBuy Orders:")
        buyOrders.inOrderTraversal().forEach { println(it) }
        println("\nSell Orders:")
        sellOrders.inOrderTraversal().forEach { println(it) }
    }
}

fun main() {
    val orderBook = OrderBook()

    while (true) {
        println("\nWelcome to the Stock Market Order Book Simulation!")
        println("--------------------------------------------------")
        print("1. Add Order\n2. View Orders\n3. Exit\nEnter your choice: ")
        when (readln().toInt()) {
            1 -> {
                print("Enter order type (buy/sell): ")
                val type = readln()
                print("Enter price: ")
                val price = readln().toDouble()
                print("Enter quantity: ")
                val quantity = readln().toInt()

                orderBook.addOrder(StockOrder((1..1000).random(), type, price, quantity))
                println("Order added successfully.")
            }
            2 -> orderBook.displayOrders()
            3 -> return
            else -> println("Invalid option!")
        }
    }
}