package com.example.todo.todoapi.dto.response;

import com.example.todo.todoapi.entity.Todo;
import lombok.*;

@Setter @Getter
@ToString @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoDetailResponseDTO {

    private String id;
    private String title;
    private boolean done;

    // 엔터티를 DTO로 만들어주는 생성자 : 왜냐하면 DB에서는 엔터티로 오고, 실제로 화면에는 DTO 형태로 뿌릴거니까!
    public TodoDetailResponseDTO(Todo todo) {
        this.id = todo.getTodoId();
        this.title = todo.getTitle();
        this.done = todo.isDone();
    }
}
