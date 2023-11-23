package com.example.todo.todoapi.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Setter @Getter
@ToString @EqualsAndHashCode(of = "todoId")
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_todo")
public class Todo {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String todoId;

    @Column(nullable = false, length = 30)
    private String title; // 할 일

    private boolean done; // 할 일 완료 여부

    @CreationTimestamp
    private LocalDateTime createDate; // 등록 시간
}
