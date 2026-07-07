package com.exam.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 解决雪花 ID 精度丢失：所有 Long 类型序列化为 String / 反序列化接受 String
 * JavaScript Number 最大安全整数为 2^53-1，雪花 ID 超出范围
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizeJackson() {
        return builder -> {
            // 序列化: Long → String（防止JS精度丢失）
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // 反序列化: String → Long（前端传回的字符串ID可正确解析）
            LongDeserializer deserializer = new LongDeserializer();
            builder.deserializerByType(Long.class, deserializer);
            builder.deserializerByType(Long.TYPE, deserializer);
        };
    }

    /** 自定义 Long 反序列化器：同时支持数字和字符串 */
    private static class LongDeserializer extends JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.getCurrentToken().isNumeric()) {
                return p.getLongValue();
            }
            String text = p.getText();
            if (text == null || text.isEmpty()) return null;
            return Long.valueOf(text);
        }
    }
}
