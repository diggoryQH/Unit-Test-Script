package com.nongsan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nongsan.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	List<User> findByStatusTrue();

	Boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	User findByToken(String token);

}
