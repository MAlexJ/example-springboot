package com.malex.redis_data_store_for_cache.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/*
 * Issue with serialize/deserialize java LocalDateTime
 *
 * link: https://stackoverflow.com/questions/27952472/serialize-deserialize-java-8-java-time-with-jackson-json-mapper
 */
public record User(
    Integer id,
    String username,
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime created)
    implements Serializable {

  public User(Integer id, String username, LocalDateTime created) {
    this.id = id;
    this.username = username;
    this.created = Objects.isNull(created) ? LocalDateTime.now() : created;
  }
}
