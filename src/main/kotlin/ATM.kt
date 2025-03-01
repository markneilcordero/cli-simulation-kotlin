import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.util.PriorityQueue

data class User(val accountNumber: String, val pin: String, var balance: Double)

data class Transaction(val type: Int, val amount: Double, val priority: Int, val accountNumber: String) : Comparable<Transaction> {
    override fun compareTo(other: Transaction): Int = this.priority - other.priority
}

class ATM {
    private val users: MutableList<User> = mutableListOf()
    private val transactionQueue = PriorityQueue<Transaction>()
    private val dataFile = "users.json"
    private val objectMapper = jacksonObjectMapper()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        val file = File(dataFile)
        if (file.exists() && file.length() > 0) {
            try {
                users.addAll(objectMapper.readValue(file))
            } catch (e: Exception) {
                println("Error reading users.json: ${e.message}")
            }
        }
    }

    private fun saveUsers() {
        try {
            objectMapper.writeValue(File(dataFile), users)
        } catch (e: Exception) {
            println("Error saving users: ${e.message}")
        }
    }

    private fun authenticateUser(): User? {
        println("Enter Account Number:")
        val acc = readLine()!!
        println("Enter PIN:")
        val pin = readLine()!!
        return users.find { it.accountNumber == acc && it.pin == pin }
    }

    fun registerUser() {
        println("Enter Account Number:")
        val acc = readLine()!!
        println("Set PIN:")
        val pin = readLine()!!
        users.add(User(acc, pin, 1000.0))
        saveUsers()
        println("Account Registered Successfully!")
    }

    fun addTransaction(user: User) {
        println("Choose Transaction Type: [1] Withdraw, [2] Deposit, [3] Balance Inquiry")
        val choice = readLine()!!.toIntOrNull() ?: return println("Invalid choice")

        val priority = when (choice) {
            1 -> 1
            2 -> 2
            3 -> 3
            else -> 3
        }

        var amount = 0.0
        if (choice != 3) {
            println("Enter Amount:")
            amount = readLine()!!.toDoubleOrNull() ?: return println("Invalid amount")
        }

        transactionQueue.add(Transaction(choice, amount, priority, user.accountNumber))
        println("Transaction added successfully!")
    }

    fun processTransactions() {
        if (transactionQueue.isEmpty()) {
            println("No transactions to process.")
            return
        }

        while (transactionQueue.isNotEmpty()) {
            val transaction = transactionQueue.poll()
            val user = users.find { it.accountNumber == transaction.accountNumber } ?: continue

            when (transaction.type) {
                1 -> {
                    if (user.balance >= transaction.amount) {
                        user.balance -= transaction.amount
                        println("Withdrawn: \$${transaction.amount}, New Balance: \$${user.balance}")
                    } else {
                        println("Insufficient funds!")
                    }
                }
                2 -> {
                    user.balance += transaction.amount
                    println("Deposited: \$${transaction.amount}, New Balance: \$${user.balance}")
                }
                3 -> println("Balance Inquiry: \$${user.balance}")
            }
            saveUsers()
        }
    }

    fun run() {
        while (true) {
            println("\nATM Simulation")
            println("[1] Register")
            println("[2] Login")
            println("[3] Exit")
            when (readLine()!!) {
                "1" -> registerUser()
                "2" -> {
                    val user = authenticateUser()
                    if (user != null) {
                        println("Welcome, ${user.accountNumber}!")
                        while (true) {
                            println("\n[1] New Transaction")
                            println("[2] Process Transactions")
                            println("[3] Logout")
                            when (readLine()!!) {
                                "1" -> addTransaction(user)
                                "2" -> processTransactions()
                                "3" -> break
                                else -> println("Invalid Choice")
                            }
                        }
                    } else {
                        println("Invalid credentials!")
                    }
                }
                "3" -> break
                else -> println("Invalid Choice")
            }
        }
    }
}

fun main() {
    val atm = ATM()
    atm.run()
}