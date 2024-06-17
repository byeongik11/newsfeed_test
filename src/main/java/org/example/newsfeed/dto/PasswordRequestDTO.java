package org.example.newsfeed.dto;

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
public class PasswordRequestDTO {

    private String beforePassword;

    private String updatePassword;

}
