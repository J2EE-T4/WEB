package com.interacthub.config;

import com.interacthub.entity.enums.NotificationType;
import com.interacthub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.InetAddress;
import java.time.Duration;

/**
 * Redis Stream consumer configuration — thay thế InteractHub_Worker (.NET BackgroundService).
 *
 * Lắng nghe stream "interacthub:notifications:stream", parse event, lưu DB và push WebSocket.
 * Tương đương Worker.cs / RedisStreamWorker trong .NET.
 *
 * Chỉ được kích hoạt khi app.redis.stream.enabled=true (mặc định true).
 * Trong test, đặt app.redis.stream.enabled=false để bỏ qua Redis connection.
 */
@Configuration
@ConditionalOnProperty(name = "app.redis.stream.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RedisStreamConfig {

    @Value("${app.redis.stream.stream-key:interacthub:notifications:stream}")
    private String streamKey;

    @Value("${app.redis.stream.group-name:interacthub-workers}")
    private String groupName;

    private final NotificationService notificationService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Khởi tạo consumer group nếu chưa tồn tại (tương đương EnsureConsumerGroupAsync trong .NET).
     */
    private void ensureConsumerGroup() {
        try {
            stringRedisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0-0"), groupName);
            log.info("Redis stream consumer group '{}' created for stream '{}'", groupName, streamKey);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("Consumer group '{}' already exists for stream '{}'", groupName, streamKey);
            } else {
                log.warn("Could not create consumer group: {}", e.getMessage());
            }
        }
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {

        // Đảm bảo stream + consumer group tồn tại
        ensureConsumerGroup();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("redis-stream-");
        executor.initialize();

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(1500))
                        .executor(executor)
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(connectionFactory, options);

        // Consumer name = hostname để hỗ trợ scale horizontal (tương đương .NET MachineName)
        String consumerName = getConsumerName();

        // Subscribe với consumer group
        Subscription subscription = container.receive(
                Consumer.from(groupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                new NotificationStreamListener(notificationService, stringRedisTemplate, streamKey, groupName)
        );

        container.start();
        log.info("Redis Stream listener started. Stream={}, Group={}, Consumer={}", streamKey, groupName, consumerName);

        return container;
    }

    private String getConsumerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "worker-" + System.currentTimeMillis();
        }
    }

    /**
     * Inner listener class — tương đương ProcessEntryAsync() trong Worker.cs (.NET).
     */
    @Slf4j
    static class NotificationStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

        private final NotificationService notificationService;
        private final StringRedisTemplate redisTemplate;
        private final String streamKey;
        private final String groupName;

        NotificationStreamListener(NotificationService notificationService,
                                   StringRedisTemplate redisTemplate,
                                   String streamKey, String groupName) {
            this.notificationService = notificationService;
            this.redisTemplate = redisTemplate;
            this.streamKey = streamKey;
            this.groupName = groupName;
        }

        @Override
        public void onMessage(MapRecord<String, String, String> message) {
            try {
                var values = message.getValue();
                String toUserId   = values.get("toUserId");
                String senderId   = values.get("senderId");
                String postId     = values.get("postId");
                String typeStr    = values.get("notificationType");

                if (toUserId == null || toUserId.isBlank()) {
                    log.warn("Stream message {} missing toUserId, skipping", message.getId());
                    ack(message);
                    return;
                }

                NotificationType type;
                try {
                    type = NotificationType.valueOf(typeStr != null ? typeStr.toUpperCase() : "LIKE");
                } catch (IllegalArgumentException e) {
                    type = NotificationType.LIKE;                    log.warn("Unknown notification type '{}', defaulting to LIKE", typeStr);
                }

                String content = buildContent(type, senderId);
                notificationService.createAndPushNotification(toUserId, senderId, type, postId, content);

                log.info("Stream processed notification {} for user {}", typeStr, toUserId);

            } catch (Exception ex) {
                log.error("Error processing stream message {}", message.getId(), ex);
            } finally {
                // XACK — đưa message ra khỏi PEL (tương đương StreamAcknowledgeAsync trong .NET)
                ack(message);
            }
        }

        private void ack(MapRecord<String, String, String> message) {
            try {
                redisTemplate.opsForStream().acknowledge(streamKey, groupName, message.getId());
            } catch (Exception e) {
                log.warn("Failed to ACK stream message {}: {}", message.getId(), e.getMessage());
            }
        }

        private String buildContent(NotificationType type, String senderId) {
            return switch (type) {
                case LIKE -> "Someone liked your post";
                case COMMENT -> "Someone commented on your post";
                case FRIEND_REQUEST -> "You have a new friend request";
                case ACCEPT_REQUEST -> "Your friend request was accepted";
                default -> "You have a new notification";
            };
        }
    }
}
