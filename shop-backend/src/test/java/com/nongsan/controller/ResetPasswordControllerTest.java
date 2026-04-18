package com.nongsan.controller;

import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.entity.User;
import com.nongsan.repository.UserRepository;
import com.nongsan.service.implement.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ResetPasswordController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResetPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void resetPassword_testChuan1() throws Exception {
	String token = "valid-token-123";
	User user = new User();
	user.setUserId(1L);
	user.setName("Nguyen Van A");
	user.setEmail("user@example.com");
	user.setToken(token);

	Mockito.when(userRepository.findByToken(token)).thenReturn(user);

	mockMvc.perform(get("/forgot-password/{token}", token))
		.andExpect(status().isOk())
		.andExpect(view().name("/reset-password"))
		.andExpect(model().attribute("name", "Nguyen Van A"))
		.andExpect(model().attribute("email", "user@example.com"))
		.andExpect(model().attribute("token", token))
		.andExpect(model().attribute("password", ""))
		.andExpect(model().attribute("confirm", ""));

	Mockito.verify(userRepository).findByToken(token);
    }

    @Test
    void resetPassword_testNgoaiLe1() throws Exception {
	String token = "invalid-token-xyz";

	Mockito.when(userRepository.findByToken(token)).thenReturn(null);

	mockMvc.perform(get("/forgot-password/{token}", token))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/forgot-password/error"));

	Mockito.verify(userRepository).findByToken(token);
    }

    @Test
    void reset_testChuan1() throws Exception {
	String token = "valid-token-123";
	String password = "newPassword123";
	String confirm = "newPassword123";
	String email = "user@example.com";
	String name = "Nguyen Van A";

	User user = new User();
	user.setUserId(1L);
	user.setName(name);
	user.setEmail(email);
	user.setToken(token);
	user.setPassword("old-encoded-password");
	user.setStatus(false);

	Mockito.when(userRepository.findByToken(token)).thenReturn(user);
	Mockito.when(passwordEncoder.encode(password)).thenReturn("new-encoded-password");
	Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

	mockMvc.perform(post("/forgot-password")
			.param("password", password)
			.param("confirm", confirm)
			.param("email", email)
			.param("name", name)
			.param("token", token))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/forgot-password/done"));

	Mockito.verify(userRepository).findByToken(token);
	Mockito.verify(passwordEncoder).encode(password);
	Mockito.verify(userRepository).save(any(User.class));
    }

    @Test
    void reset_testNgoaiLe1() throws Exception {
	String token = "valid-token-123";
	String password = "short";
	String confirm = "short";
	String email = "user@example.com";
	String name = "Nguyen Van A";

	Mockito.when(passwordEncoder.encode(password)).thenReturn("encoded-short");

	mockMvc.perform(post("/forgot-password")
			.param("password", password)
			.param("confirm", confirm)
			.param("email", email)
			.param("name", name)
			.param("token", token))
		.andExpect(status().isOk())
		.andExpect(view().name("/reset-password"))
		.andExpect(model().attribute("password", password))
		.andExpect(model().attribute("confirm", confirm))
		.andExpect(model().attribute("errorPassword", "error"))
		.andExpect(model().attribute("name", name))
		.andExpect(model().attribute("email", email))
		.andExpect(model().attribute("token", token));

	Mockito.verify(userRepository, never()).findByToken(token);
	Mockito.verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void reset_testNgoaiLe2() throws Exception {
	String token = "valid-token-123";
	String password = "newPassword123";
	String confirm = "differentPassword456";
	String email = "user@example.com";
	String name = "Nguyen Van A";

	mockMvc.perform(post("/forgot-password")
			.param("password", password)
			.param("confirm", confirm)
			.param("email", email)
			.param("name", name)
			.param("token", token))
		.andExpect(status().isOk())
		.andExpect(view().name("/reset-password"))
		.andExpect(model().attribute("errorConfirm", "error"))
		.andExpect(model().attribute("name", name))
		.andExpect(model().attribute("email", email))
		.andExpect(model().attribute("password", password))
		.andExpect(model().attribute("confirm", confirm))
		.andExpect(model().attribute("token", token));

	Mockito.verify(userRepository, never()).findByToken(token);
	Mockito.verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void done_testChuan1() throws Exception {
	mockMvc.perform(get("/forgot-password/done"))
		.andExpect(status().isOk())
		.andExpect(view().name("/done"));
    }

    @Test
    void error_testChuan1() throws Exception {
	mockMvc.perform(get("/forgot-password/error"))
		.andExpect(status().isOk())
		.andExpect(view().name("/error"));
    }
}
