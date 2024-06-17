package org.example.newsfeed.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Setter
public class UserRequestDTO {

        private String userId;

        @NotBlank(message = "Password cannot be Blank")
        private String password;

        @NotBlank
        private String name;

        private String comment;

        @Email
        private String email;


}

