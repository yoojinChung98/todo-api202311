package com.example.todo.todoapi.service;

import com.example.todo.todoapi.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional // jpa 쓸거니까 필요!
public class TodoService {

    private final TodoRepository todoRepository;

}
