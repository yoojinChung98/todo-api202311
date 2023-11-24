package com.example.todo.todoapi.service;

import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.response.TodoDetailResponseDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.entity.Todo;
import com.example.todo.todoapi.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional // jpa 쓸거니까 필요!
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoListResponseDTO create(final TodoCreateRequestDTO requestDTO) throws RuntimeException { // 매개변수에 final 을 붙인다는 말은, 서비스 내에서 해당 파라미터의 값을 바꿀 수 없다는 뜻 (불변성)
        // 즉, 매개변수를 변형하지 말고 넘겨 준 대로 쓰라는 말!
        todoRepository.save(requestDTO.toEntity());
        log.info("할 일 저장 완료! 제목: {}", requestDTO.getTitle());

        return retrieve();
    }

    public TodoListResponseDTO retrieve() { // 앞으로 글목록이 필요할 때마다 얘를 호출하면 됨.
        List<Todo> entityList = todoRepository.findAll();

        List<TodoDetailResponseDTO> dtoList
                = entityList.stream()
                .map(TodoDetailResponseDTO::new) // .map(todo -> new TodoDetailResponseDTO(todo))
                .collect(Collectors.toList());

        return TodoListResponseDTO.builder()
                .todos(dtoList)
                .build();
    }


    public TodoListResponseDTO delete(final String todoId) { //todoId가 변경되는것을 막기 위해 final 추가
        try {
            todoRepository.deleteById(todoId);
        } catch (Exception e) {
            log.error("아이디가 존재하지 않아서 삭제에 실패했습니다. -ID: {}, error: {}"
                    , todoId, e.getMessage());
            throw new RuntimeException("아이디가 존재하지 않아서 삭제에 실패했습니다.");
        }
        return retrieve();
    }

    public TodoListResponseDTO update(final TodoModifyRequestDTO requestDTO) throws RuntimeException {
        Optional<Todo> targetEntity = todoRepository.findById(requestDTO.getId());

        targetEntity.ifPresent(todo -> {
            todo.setDone(requestDTO.isDone()); // 화면 단에서 done 값을 잘 돌려서 보냈으니, 여기서는 반전시키지 않고 그대로 값을 넣음
            // 쌤 생각에는 화면단에서 반전시키고, 백엔드에서는 연동에만 집중하는 것이 이해하기 편한 코드라고 생각한다고 함.

            // 변경감지(dirtyCheck)가 발생할 것이고~
            todoRepository.save(todo);
        });
        return retrieve();
    }
}
