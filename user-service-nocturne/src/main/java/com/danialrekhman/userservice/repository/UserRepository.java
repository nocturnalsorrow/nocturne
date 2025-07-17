package com.danialrekhman.userservice.repository;

import com.danialrekhman.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, String> {

    User findByEmail(String email);

    User findByUsername(String username);

    boolean existsByEmail(String email);
}
