package com.example.todo.todoapi.dto.request;

import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.entity.Todo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class TodoCreateRequestDTO {

    @NotBlank
    @Size(min = 2, max = 30)
    private String title;

    // dto를 엔터티로 변환
    public Todo toEntity() {
        return Todo.builder()
                .title(this.title)
                .build();
    }



}
