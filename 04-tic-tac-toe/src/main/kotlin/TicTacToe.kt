/*
Напишите консольную сетевую версию игры "Крестики-нолики": один игрок является сервером, другой — клиентом.
Определите массив с символами (как object) на обеих сторонах и меняйте его элементы в зависимости от
ввода пользователя и данных, полученных от клиента (сервера). Выводить массив можно как поле игры на обеих сторонах
после каждого хода. Для простоты можно считать, что сервер всегда начинает и играет крестиками.
В первой версии можно не проверять корректность ходов и обнаруживать конец игры.
Клетки можно нумеровать числами от 0 до 8 как индексы элементов массива.
 */

import kotlin.random.Random
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import kotlin.math.abs

fun Server(hostname: String, port: Int, size: Int) = runBlocking {
    val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind(InetSocketAddress(hostname, port))
    println("Started server at ${server.localAddress}")
    var field = Array(size * size, {'.'})

    while (true) {
        val socket = server.accept()

        launch {
            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            var i = 1
            while (i <= size * size) {
                print(field[i - 1])
                if (i % size == 0)
                    println()
                i++
            }
            println("Your choice, Server")
            var answer = readLine()
            while (true) {
                if (answer != null && !answer.isBlank() && field[answer.toInt() - 1] == '.') {
                    field[answer.toInt() - 1] = 'x'
                    println("you: $answer")
                    break
                } else {
                    println("You can not do this!")
                    answer = readLine()
                }
            }

            output.writeStringUtf8("$answer\r\n")

            while (true) {
                val line = input.readUTF8Line()

                if (line != null) {
                    field[line.toInt() - 1] = 'o'
                    var i = 1
                    while (i <= size * size) {
                        print(field[i - 1])
                        if (i % size == 0)
                            println()
                        i++
                    }
                }
                else
                    socket.close()

                println("Your choice, Server")
                var answer = readLine()
                while (true) {
                    if (answer != null && !answer.isBlank() && field[answer.toInt() - 1] == '.') {
                        field[answer.toInt() - 1] = 'x'
                        println("you: $answer")
                        break
                    } else {
                        println("You can not do this!")
                        answer = readLine()
                    }
                }

                output.writeStringUtf8("$answer\r\n")
            }
        }
    }
}

fun Client(hostname: String, port: Int, size: Int) = runBlocking {
    var field = Array(size * size, {'.'})
    val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(InetSocketAddress(hostname, port))
    val input = socket.openReadChannel()
    val output = socket.openWriteChannel(autoFlush = true)

    while (true) {
        val line = input.readUTF8Line()

        if (line != null) {
            field[line.toInt() - 1] = 'x'
            var i = 1
            while (i <= size * size) {
                print(field[i - 1])
                if (i % size == 0)
                    println()
                i++
            }
        }
        else
            socket.close()

        println("Your choice, Client")
        var answer = readLine()
        while (true) {
            if (answer != null && !answer.isBlank() && field[answer.toInt() - 1] == '.') {
                field[answer.toInt() - 1] = 'o'
                println("you: $answer")
                break
            } else {
                println("You can not do this!")
                answer = readLine()
            }
        }

        output.writeStringUtf8("$answer\r\n")
    }
}

fun main(args: Array<String>) {
    val size = 3

    if(args.size == 1) {
        when(args[0]) {
            "server" -> Server("127.0.0.1", 2323, size)
            "client" -> Client("127.0.0.1", 2323, size)
        }
    }
}
