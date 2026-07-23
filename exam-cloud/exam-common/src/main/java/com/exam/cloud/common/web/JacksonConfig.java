package com.exam.cloud.common.web;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 解决雪花 ID 精度丢失：Long 序列化为 String，反序列化接受数字或字符串。
 * JavaScript Number 最大安全整数为 2^53-1，雪花 ID 超出。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizeJackson() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            LongDeserializer d = new LongDeserializer();
            builder.deserializerByType(Long.class, d);
            builder.deserializerByType(Long.TYPE, d);
        };
    }

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
