package org.example.newsfeed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.example.newsfeed.dto.PasswordRequestDTO;
import org.example.newsfeed.dto.UserRequestDTO;
import org.example.newsfeed.exception.InvalidPasswordException;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString
@SuperBuilder
@Setter
@Table(name = "user")
public class User extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(min = 10, max = 20)
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Length(min = 10)
    @Column(nullable = false)
    private String password;

    @Column
    private String name;

    @Email
    @Column(unique = true)
    private String email;

    @Column
    private String comment;

    @Column
    private String status;

    @Column
    @Setter
    private String refreshToken;

    @Column
    private String statusChangeTime;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column
    private LocalDateTime modifyDate;

    @OneToMany(mappedBy = "user")
    private List<Post> newsfeeds;

    public User(String userId, String password, String email, String comment) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.comment = comment;
    }

    public void setStatus(UserStatusEnum status) {
        if (!status.getStatus().equals(this.status)) {
            this.status = status.getStatus();
            this.modifyDate = LocalDateTime.now();
        }
    }

    public User(String userId, String password, String name, String email, String comment,
        String refreshToken, String statusChangeTime, UserStatusEnum status) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.comment = comment;
        this.refreshToken = refreshToken;
        this.statusChangeTime = statusChangeTime;
        this.status = status.getStatus();
    }

    public void updateUser(UserRequestDTO dto) {
        this.name = dto.getName();
        this.comment = dto.getComment();
    }

    public void updatePassword(PasswordRequestDTO dto, BCryptPasswordEncoder passwordEncoder) {

        if (passwordEncoder.matches(dto.getBeforePassword(), this.password)) {
            this.password = passwordEncoder.encode(dto.getUpdatePassword());
        } else {
            throw new InvalidPasswordException("패스워드가 일치하지 않습니다.");
        }
    }
}