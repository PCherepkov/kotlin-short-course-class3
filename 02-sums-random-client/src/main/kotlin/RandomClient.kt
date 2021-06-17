import kotlin.random.Random
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import kotlin.math.abs

/*
Напишите клиента, который генерирует случайное количество случайных целых чисел (Random.nextInt),
формирует запрос с числами на сервер, отправляет его, получает от сервера и печатает результат.
Всего клиент должен посылать 100 запросов.
 */

fun rndServer(hostname: String, port: Int) = runBlocking {
    val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind(InetSocketAddress(hostname, port))
    println("Started server at ${server.localAddress}")

    while (true) {
        val socket = server.accept()

        launch {
            println("Socket accepted: ${socket.remoteAddress}")

            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            while (true) {
                val line = input.readUTF8Line()

                println("${socket.remoteAddress}: $line")
                output.writeStringUtf8("$line\r\n")
            }
        }
    }
}

fun rndClient(hostname: String, port: Int) = runBlocking {
    val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(InetSocketAddress(hostname, port))
    val input = socket.openReadChannel()
    val output = socket.openWriteChannel(autoFlush = true)

    repeat(100) {
        var n = abs(Random.nextInt() % 10) + 1
        var request = Array(n, {Random.nextInt()})

        output.writeStringUtf8("${request.joinToString(" ")}\r\n")
        val response = input.readUTF8Line()
        println("server said: $response")
    }
}

fun usage() {
    println("""
        Usage: 
            sums server
            sums client
    """.trimIndent())
}

fun main(args: Array<String>) {
    if(args.size == 1) {
        when(args[0]) {
            "client" -> rndClient("127.0.0.1", 2323)
            "server" -> rndServer("127.0.0.1", 2323)
            else -> usage()
        }
    } else {
        usage()
    }
}
