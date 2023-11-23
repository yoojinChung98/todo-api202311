package com.example.todo.todoapi.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoModifyRequestDTO { // 체크 박스를 눌렀을 때 done 의 값을 수정해줄 애!

    @NotBlank
    private String id;
    private boolean done;

}
