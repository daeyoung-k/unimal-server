package com.unimal.user.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,
    @Value("\${spring.data.redis.port}")
    private val port: Int,
    @Value("\${spring.data.redis.password}")
    private val password: String
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val factory = RedisStandaloneConfiguration()
        factory.hostName = host
        factory.port = port
        factory.setPassword(password)
        return LettuceConnectionFactory(factory)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            // 직렬화 설정
            val stringSerializer = StringRedisSerializer()
            val jsonSerializer = GenericJackson2JsonRedisSerializer()

            this.connectionFactory = redisConnectionFactory
            this.keySerializer = stringSerializer
            this.hashKeySerializer = stringSerializer
            this.valueSerializer = jsonSerializer
            this.hashValueSerializer = jsonSerializer
        }
    }
}