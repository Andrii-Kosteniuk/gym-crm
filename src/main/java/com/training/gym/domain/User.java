package com.training.gym.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private Boolean active;
}
