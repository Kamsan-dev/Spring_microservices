package com.kamsan.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "devices")
public class Device {
    @Id
    private Long deviceId;
    private Long userId;
    private String machine;
    private String client;
    private String ipAddress;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
