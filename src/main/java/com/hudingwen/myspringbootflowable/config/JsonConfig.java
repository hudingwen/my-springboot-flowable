package com.hudingwen.myspringbootflowable.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ClassName:JsonConfig
 * Package:com.hudingwen.myspringbootflowable.config
 * Description:描述
 *
 * @Date:2022/12/23 13:03
 * @Author:胡丁文
 * @E-mail:admin@aiwanyun.cn
 **/
@Configuration
@Slf4j
public class JsonConfig {
    @Bean
    public HttpMessageConverters httpMessageConverters() {
        MappingJackson2HttpMessageConverter httpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = httpMessageConverter.getObjectMapper();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleModule simpleModule = new SimpleModule();
        //序列化日期格式
        simpleModule.addSerializer(Date.class, new DateSerializer(false, simpleDateFormat));
        //Long序列化为String
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        //字符串转String
//        simpleModule.addDeserializer(Date.class, new JsonDeserializer<Date>() {
//            @Override
//            public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
//                String date = jsonParser.getText();
//                try {
//                    return simpleDateFormat.parse(date);
//                } catch (ParseException e) {
//                    log.error("自定义全局序列化String2Date异常:{}",e.getMessage());
//                    throw new RuntimeException(e);
//                }
//            }
//        });
        //反序列化日期格式
        objectMapper.registerModule(simpleModule);
        //序列化多余字段不报错
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //序列化地区
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        //如果值为null时字段key还是否输出:ALWAYS(总是输出),NON_NULL(为null的不输出)
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object o, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
                String fieldName = gen.getOutputContext().getCurrentName();
                try {
                    //反射获取字段类型
                    Field field = gen.getCurrentValue().getClass().getDeclaredField(fieldName);
                    if (Objects.equals(field.getType(), String.class)) {
                        //字符串型空值""
                        gen.writeString("");
                        return;
                    } else if (Objects.equals(field.getType(), List.class)) {
                        //列表型空值返回[]
                        gen.writeStartArray();
                        gen.writeEndArray();
                        return;
                    } else if (Objects.equals(field.getType(), Map.class)) {
                        //map型空值返回{}
                        gen.writeStartObject();
                        gen.writeEndObject();
                        return;
                    }
                } catch (NoSuchFieldException e) {
                }
                //默认返回""
                gen.writeString("");
            }
        });
        return new HttpMessageConverters(httpMessageConverter);
    }
}
