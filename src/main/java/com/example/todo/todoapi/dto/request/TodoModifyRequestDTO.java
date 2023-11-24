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
    private String id; // 낫블랭크가 걸려있으니 얘를 부르는 단에서는 @validated 한번 해줘야겠네! 검증한거 확인해야징~!
    private boolean done;

}
