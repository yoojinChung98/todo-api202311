package com.example.todo.todoapi.dto.response;

import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoListResponseDTO {

    private String error; // 에러 발생 시 에러 메세지를 담을 필드(근데 머 문자열로 걍 갖다 던져도 문제는 없음_
    // 할 일 객체가 모여있는 목록
    private List<TodoDetailResponseDTO> todos;

}
