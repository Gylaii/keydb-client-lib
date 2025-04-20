package messaging

import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubAdapter
import org.slf4j.LoggerFactory

/**
 * Простой и понятный клиент для работы с KeyDB. Минимум абстракций, максимум
 * функциональности.
 */
class KeyDBClient(
    val host: String = System.getenv("KEYDB_HOST") ?: "localhost",
    val port: Int = System.getenv("KEYDB_PORT")?.toIntOrNull() ?: 6379,
) {
    private val logger = LoggerFactory.getLogger(KeyDBClient::class.java)

    // Один клиент для всех операций
    private val client = RedisClient.create("redis://$host:$port")

    // Одно соединение для всего
    private val connection = client.connect()

    // Соединение для PubSub
    private val pubSubConnection = client.connectPubSub()

    /** Публикация сообщения в канал */
    fun pub(channel: String, message: String) {
        try {
            logger.info("Публикация в канал $channel")
            // Используем async для неблокирующей работы
            connection.async().publish(channel, message)
        } catch (e: Exception) {
            logger.error("Ошибка публикации: ${e.message}", e)
        }
    }

    /** Подписка на канал с обработчиком */
    fun sub(channel: String, handler: (String) -> Unit) {
        try {
            // Регистрируем обработчик сообщений
            pubSubConnection.addListener(
                object : RedisPubSubAdapter<String, String>() {
                    override fun message(msgChannel: String, message: String) {
                        if (channel == msgChannel) {
                            try {
                                handler(message)
                            } catch (e: Exception) {
                                logger.error(
                                    "Ошибка в обработчике: ${e.message}",
                                    e,
                                )
                            }
                        }
                    }
                }
            )

            // Подписываемся через reactive (не блокирует поток)
            pubSubConnection.reactive().subscribe(channel).subscribe()
            logger.info("Подписались на канал: $channel")
        } catch (e: Exception) {
            logger.error("Ошибка подписки на канал $channel: ${e.message}", e)
        }
    }

    /** Отправка сообщения в очередь */
    fun push(queue: String, message: String) {
        try {
            connection.async().rpush(queue, message)
            logger.debug("Сообщение отправлено в очередь: $queue")
        } catch (e: Exception) {
            logger.error("Ошибка отправки в очередь $queue: ${e.message}", e)
        }
    }

    /** Изъятие сообщений из очереди */
    fun pop(queue: String, handler: (String) -> Unit) {
        val reactive = connection.reactive()

        fun processNext() {
            reactive
                .brpop(10, queue)
                .subscribe(
                    { message ->
                        // Обработка сообщения
                        handler(message.value)
                        // Продолжаем обработку следующего сообщения сразу
                        processNext()
                    },
                    { error ->
                        logger.error("Ошибка: ${error.message}")
                        // Небольшая пауза перед повторной попыткой при ошибке
                        Thread.sleep(1000)
                        processNext()
                    },
                    {
                        // Этот блок вызывается при таймауте
                        processNext()
                    },
                )
        }
        processNext()
    }

    /** Закрытие всех соединений */
    fun shutdown() {
        try {
            connection.close()
            pubSubConnection.close()
            client.shutdown()
        } catch (e: Exception) {
            logger.error("Ошибка при закрытии соединений: ${e.message}", e)
        }
    }
}
