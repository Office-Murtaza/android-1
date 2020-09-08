package com.app.belcobtm.data.sockets

class SocketMessageSerializer {
    companion object {
        fun serialize(message: SocketMessage): String {
            val buffer = StringBuilder()
            buffer.append(
                """
                ${message.command}
                
                """.trimIndent()
            )
            for ((key, value) in message.headers) {
                buffer.append(key).append(":").append(value).append("\n")
            }
            buffer.append("\n")
            buffer.append(message.body)
            buffer.append('\u0000')
            return buffer.toString()
        }

        fun deserialize(message: String): SocketMessage {
            val lines = message.split("\n".toRegex()).toTypedArray()
            val command = lines[0].trim { it <= ' ' }
            val result = SocketMessage(command)
            var i = 1
            while (i < lines.size) {
                val line = lines[i].trim { it <= ' ' }
                if (line == "") {
                    break
                }
                val parts = line.split(":".toRegex()).toTypedArray()
                val name = parts[0].trim { it <= ' ' }
                var value = ""
                if (parts.size == 2) {
                    value = parts[1].trim { it <= ' ' }
                }
                result.put(name, value)
                ++i
            }
            val sb = StringBuilder()
            while (i < lines.size) {
                sb.append(lines[i])
                ++i
            }
            val body = sb.toString().trim { it <= ' ' }
            result.body = body
            return result
        }
    }
}