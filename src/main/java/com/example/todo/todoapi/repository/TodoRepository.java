package com.example.todo.todoapi.repository;

import com.example.todo.todoapi.entity.Todo;
import com.example.todo.userapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, String> {

    // 특정 회원의 할 일 목록을 리턴
    // 네이티브로 썼다면,,, SELECT * FROM todo t WHERE user_id = ?
    @Query("SELECT t FROM Todo t WHERE t.user = :user")
    List<Todo> findAllByUser(User user); // 위의 :user 과 변수명이 같으므로 매개변수 앞의 @Param("user")를 생략할 수 있음
    


}
