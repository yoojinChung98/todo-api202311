package com.example.todo.todoapi.api;

import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Repeatable;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/todos")
//@CrossOrigin(origins = "http://localhost:3000") // 여러개 걸고 싶으면 중괄호로 감싸면 돼용~~~ (spring 시큐리티모듈때문에 먼가 시큐리티 설정때문에 패치 요청이 막횜)
@CrossOrigin() // 응답된 자원을 전달할 수 있다고는 표현해야 함.
// 입력한 곳에서 요청이 왔을 때, 이곳으로 요청의 응답을 할 수 있도록 해주세요! 하는 것.
public class TodoController {


    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<?> createTodo(
            @Validated @RequestBody TodoCreateRequestDTO requestDTO,
            BindingResult result
    ){
        if(result.hasErrors()) {
            log.warn("DTO 검증 에러 발생: {}", result.getFieldError());
            return ResponseEntity
                    .badRequest()
                    .body(result.getFieldError());
        }

        try {   // service 단의 save 등의 메서드에서 에러 발생 가능성이 있어서 호출부로 throw 처리 해놓았음
            TodoListResponseDTO responseDTO = todoService.create(requestDTO);
            return ResponseEntity
                    .ok()
                    .body(responseDTO);

        } catch (RuntimeException e) {
            e.printStackTrace();; //  log.error(e.getMessage()) 를 해도 무방! 그냥 조금 짧게 보려구 getMessage()해놓은 것.
            return ResponseEntity
                    .internalServerError()
                    .body(TodoListResponseDTO.builder().error(e.getMessage()).build()); // 에러가 발생하면 그 DTO를 만들었을 때, List는 제대로 이행되지 않아 받아오지 못햇을 것.
        }
    }

    // 할 일 목록 요청
    @GetMapping
    public ResponseEntity<?> retrieveTodoList() {
        log.info("/api/todos GET request");
        TodoListResponseDTO responseDTO = todoService.retrieve();

        return ResponseEntity.ok().body(responseDTO);
    }

    // 할 일 삭제 요청
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(
        @PathVariable("id") String todoId

    ) {
        log.info("/api/todos/{} DELETE request!", todoId);

        if(todoId == null || todoId.trim().equals("")) {
            return ResponseEntity
                    .badRequest()
                    .body(TodoListResponseDTO.builder().error("ID를 전달해주세요.").build());
        }
        // if 문을 건너뛴다면 아이디는 제대로 온 것!

        // 삭제가 된 후의 리스트르 전달받겠움
        try {
            TodoListResponseDTO responseDTO = todoService.delete(todoId);
            return ResponseEntity.ok().body(responseDTO);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(TodoListResponseDTO.builder().error(e.getMessage()).build());
        }
    }


    // 할 일 수정하기
    @RequestMapping(method = {RequestMethod.PATCH, RequestMethod.PUT}) // 패치와 풋 둘다 받고 싶을땐 약간 전통적인 방식으로 받아야 함.
    public ResponseEntity<?> updateTodo(
            @Validated @RequestBody TodoModifyRequestDTO requestDTO, //이 DTO id에 @NotBlank 걸려있어서 검증한거 확인하려고 Validated 사용
            BindingResult result,
            HttpServletRequest request // 이건 나중에 어떤 요청이 들어온건지 확인하기 위해서 선언한것(필수 아님)
    ) {
        if(result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError());
        }

        log.info("api/todos {} request!", request.getMethod());
        log.info("modifying dto: {}", requestDTO);

        // 수정 후 수정 완료된 새로운 리스트를 다시 받아올 것.
        try {
            TodoListResponseDTO responseDTO = todoService.update(requestDTO);
            return ResponseEntity.ok().body(responseDTO);

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(TodoListResponseDTO.builder().error(e.getMessage()).build());
        }

    }


}