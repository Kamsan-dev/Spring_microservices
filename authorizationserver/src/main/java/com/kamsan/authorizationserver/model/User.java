package com.kamsan.authorizationserver.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String userUUID;

    public String getUserUUID() {
        return userUUID;
    }
}
