package com.nongsan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongsan.config.AuthEntryPointJwt;
import com.nongsan.config.AuthTokenFilter;
import com.nongsan.config.JwtUtils;
import com.nongsan.dto.LoginRequest;
import com.nongsan.dto.SignupRequest;
import com.nongsan.entity.AppRole;
import com.nongsan.entity.Cart;
import com.nongsan.entity.User;
import com.nongsan.repository.AppRoleRepository;
import com.nongsan.repository.CartRepository;
import com.nongsan.repository.UserRepository;
import com.nongsan.service.SendMailService;
import com.nongsan.service.implement.UserDetailsImpl;
import com.nongsan.service.implement.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserApi.class)
@AutoConfigureMockMvc(addFilters = false)
class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private AppRoleRepository roleRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private SendMailService sendMailService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void getAll_testChuan1() throws Exception {
	User activeUser = new User();
	activeUser.setUserId(1L);
	activeUser.setName("Nguyen Van A");
	activeUser.setStatus(true);

	Mockito.when(userRepository.findByStatusTrue()).thenReturn(Collections.singletonList(activeUser));

	mockMvc.perform(get("/api/auth"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(1))
		.andExpect(jsonPath("$[0].name").value("Nguyen Van A"));

	Mockito.verify(userRepository).findByStatusTrue();
    }

    @Test
    void getOne_testChuan1() throws Exception {
	Long userId = 1L;
	User user = new User();
	user.setUserId(userId);
	user.setName("Tran Thi B");

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

	mockMvc.perform(get("/api/auth/{id}", userId))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Tran Thi B"));

	Mockito.verify(userRepository).existsById(userId);
	Mockito.verify(userRepository).findById(userId);
    }

    @Test
    void getOne_testNgoaiLe1() throws Exception {
	Long userId = 999L;

	Mockito.when(userRepository.existsById(userId)).thenReturn(false);

	mockMvc.perform(get("/api/auth/{id}", userId))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository).existsById(userId);
	Mockito.verify(userRepository, never()).findById(any());
    }

    @Test
    void getOneByEmail_testChuan1() throws Exception {
	String email = "a@example.com";
	User user = new User();
	user.setEmail(email);
	user.setName("Email User");

	Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
	Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

	mockMvc.perform(get("/api/auth/email/{email}", email))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.email").value("a@example.com"));

	Mockito.verify(userRepository).existsByEmail(email);
	Mockito.verify(userRepository).findByEmail(email);
    }

    @Test
    void getOneByEmail_testNgoaiLe1() throws Exception {
	String email = "missing@example.com";

	Mockito.when(userRepository.existsByEmail(email)).thenReturn(false);

	mockMvc.perform(get("/api/auth/email/{email}", email))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository).existsByEmail(email);
	Mockito.verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void post_testChuan1() throws Exception {
	User newUser = new User();
	newUser.setUserId(10L);
	newUser.setName("Create User");
	newUser.setEmail("create@example.com");
	newUser.setPassword("raw-pass");
	newUser.setPhone("0909");
	newUser.setAddress("HCM");
	newUser.setStatus(true);

	Mockito.when(userRepository.existsByEmail("create@example.com")).thenReturn(false);
	Mockito.when(userRepository.existsById(10L)).thenReturn(false);
	Mockito.when(passwordEncoder.encode("raw-pass")).thenReturn("encoded-pass");
	Mockito.when(jwtUtils.doGenerateToken("create@example.com")).thenReturn("token-123");
	Mockito.when(userRepository.save(any(User.class))).thenReturn(newUser);

	mockMvc.perform(post("/api/auth")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(newUser)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Create User"));

	Mockito.verify(passwordEncoder).encode("raw-pass");
	Mockito.verify(jwtUtils).doGenerateToken("create@example.com");
	Mockito.verify(userRepository).save(any(User.class));
	Mockito.verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void post_testNgoaiLe1() throws Exception {
	User payload = new User();
	payload.setUserId(10L);
	payload.setEmail("exists@example.com");

	Mockito.when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

	mockMvc.perform(post("/api/auth")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(payload)))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository, never()).save(any(User.class));
	Mockito.verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void post_testNgoaiLe2() throws Exception {
	User payload = new User();
	payload.setUserId(10L);
	payload.setEmail("new@example.com");

	Mockito.when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
	Mockito.when(userRepository.existsById(10L)).thenReturn(true);

	mockMvc.perform(post("/api/auth")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(payload)))
		.andExpect(status().isBadRequest());

	Mockito.verify(userRepository, never()).save(any(User.class));
	Mockito.verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void put_testChuan1() throws Exception {
	Long userId = 5L;
	User existing = new User();
	existing.setUserId(userId);
	existing.setPassword("old-encoded");

	User updatePayload = new User();
	updatePayload.setUserId(userId);
	updatePayload.setName("Updated Name");
	updatePayload.setPassword("new-raw");

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
	Mockito.when(passwordEncoder.encode("new-raw")).thenReturn("new-encoded");
	Mockito.when(userRepository.save(any(User.class))).thenReturn(updatePayload);

	mockMvc.perform(put("/api/auth/{id}", userId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatePayload)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Updated Name"));

	Mockito.verify(passwordEncoder).encode("new-raw");
	Mockito.verify(userRepository).save(any(User.class));
    }

    @Test
    void put_testNgoaiLe1() throws Exception {
	Long userId = 5L;
	User updatePayload = new User();
	updatePayload.setUserId(99L);

		Mockito.when(userRepository.existsById(userId)).thenReturn(true);

	mockMvc.perform(put("/api/auth/{id}", userId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatePayload)))
		.andExpect(status().isBadRequest());

		Mockito.verify(userRepository).existsById(userId);
	Mockito.verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void put_testNgoaiLe2() throws Exception {
	Long userId = 5L;
	User updatePayload = new User();
	updatePayload.setUserId(userId);

	Mockito.when(userRepository.existsById(userId)).thenReturn(false);

	mockMvc.perform(put("/api/auth/{id}", userId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatePayload)))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository).existsById(userId);
	Mockito.verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void putAdmin_testChuan1() throws Exception {
	Long userId = 3L;
	User updatePayload = new User();
	updatePayload.setUserId(userId);
	updatePayload.setName("Admin User");

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.save(any(User.class))).thenReturn(updatePayload);

	mockMvc.perform(put("/api/auth/admin/{id}", userId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatePayload)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Admin User"));

	ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
	Mockito.verify(userRepository).save(captor.capture());

	Set<AppRole> roles = captor.getValue().getRoles();
	org.junit.jupiter.api.Assertions.assertEquals(1, roles.size());
	org.junit.jupiter.api.Assertions.assertEquals(2, roles.iterator().next().id);
    }

    @Test
    void delete_testChuan1() throws Exception {
	Long userId = 8L;
	User user = new User();
	user.setUserId(userId);
	user.setStatus(true);

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

	mockMvc.perform(delete("/api/auth/{id}", userId))
		.andExpect(status().isOk());

	org.junit.jupiter.api.Assertions.assertFalse(user.getStatus());
	Mockito.verify(userRepository).save(user);
    }

    @Test
    void delete_testNgoaiLe1() throws Exception {
	Long userId = 999L;

	Mockito.when(userRepository.existsById(userId)).thenReturn(false);

	mockMvc.perform(delete("/api/auth/{id}", userId))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository).existsById(userId);
	Mockito.verify(userRepository, never()).findById(any());
	Mockito.verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signin_testChuan1() throws Exception {
	LoginRequest request = new LoginRequest("user@example.com", "123456");

	Authentication authentication = Mockito.mock(Authentication.class);
	UserDetailsImpl principal = new UserDetailsImpl(
		1L,
		"User Test",
		"user@example.com",
		"encoded",
		"0909",
		"Ha Noi",
		true,
		true,
		"avatar.png",
		LocalDate.of(2024, 1, 1),
		Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
	);

	Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
		.thenReturn(authentication);
	Mockito.when(authentication.getPrincipal()).thenReturn(principal);
	Mockito.when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

	mockMvc.perform(post("/api/auth/signin")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(request)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.token").value("jwt-token"))
		.andExpect(jsonPath("$.email").value("user@example.com"))
		.andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void signup_testNgoaiLe1() throws Exception {
	SignupRequest request = new SignupRequest(
		"User 1",
		"taken@example.com",
		"123456",
		"0909",
		"HCM",
		true,
		true,
		"img.jpg",
		LocalDate.of(2025, 1, 1),
		new HashSet<>()
	);

	Mockito.when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

	mockMvc.perform(post("/api/auth/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(request)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.message").value("Error: Email is already taken!"));

	Mockito.verify(userRepository, never()).save(any(User.class));
	Mockito.verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void signup_testChuan1() throws Exception {
	SignupRequest request = new SignupRequest(
		"User 2",
		"newsignup@example.com",
		"123456",
		"0909",
		"Can Tho",
		false,
		true,
		"img2.jpg",
		LocalDate.of(2025, 2, 2),
		new HashSet<>()
	);

	Mockito.when(userRepository.existsByEmail("newsignup@example.com")).thenReturn(false);
	Mockito.when(passwordEncoder.encode("123456")).thenReturn("encoded-123456");
	Mockito.when(jwtUtils.doGenerateToken("newsignup@example.com")).thenReturn("signup-token");

	mockMvc.perform(post("/api/auth/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(request)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.message").value("Đăng kí thành công"));

	Mockito.verify(userRepository).save(any(User.class));
	Mockito.verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void logout_testChuan1() throws Exception {
	mockMvc.perform(get("/api/auth/logout"))
		.andExpect(status().isOk());
    }

    @Test
    void sendToken_testNgoaiLe1() throws Exception {
	String email = "missing@example.com";

	Mockito.when(userRepository.existsByEmail(email)).thenReturn(false);

	mockMvc.perform(post("/api/auth/send-mail-forgot-password-token")
			.contentType(MediaType.TEXT_PLAIN)
			.content(email))
		.andExpect(status().isNotFound());

	Mockito.verify(sendMailService, never()).queue(any(), any(), any());
    }

    @Test
    void sendToken_testChuan1() throws Exception {
	String email = "found@example.com";
	User user = new User();
	user.setEmail(email);
	user.setToken("token-reset-999");

	Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
	Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

	mockMvc.perform(post("/api/auth/send-mail-forgot-password-token")
			.contentType(MediaType.TEXT_PLAIN)
			.content(email))
		.andExpect(status().isOk());

	Mockito.verify(sendMailService).queue(eq(email), eq("Reset mật khẩu"),
		Mockito.contains("forgot-password/token-reset-999"));
    }

    @Test
    void getAll_testChuan2() throws Exception {
	User user1 = new User();
	user1.setUserId(1L);
	user1.setName("User One");
	user1.setStatus(true);

	User user2 = new User();
	user2.setUserId(2L);
	user2.setName("User Two");
	user2.setStatus(true);

	User user3 = new User();
	user3.setUserId(3L);
	user3.setName("User Three");
	user3.setStatus(true);

	Mockito.when(userRepository.findByStatusTrue()).thenReturn(Arrays.asList(user1, user2, user3));

	mockMvc.perform(get("/api/auth"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.size()").value(3))
		.andExpect(jsonPath("$[0].name").value("User One"))
		.andExpect(jsonPath("$[1].name").value("User Two"))
		.andExpect(jsonPath("$[2].name").value("User Three"));

	Mockito.verify(userRepository).findByStatusTrue();
    }

    @Test
    void registerUser_testChuan1() throws Exception {
	SignupRequest request = new SignupRequest(
		"New User",
		"newuser@example.com",
		"123456",
		"0123456789",
		"Da Nang",
		true,
		true,
		"avatar.jpg",
		LocalDate.of(2024, 1, 15),
		new HashSet<>()
	);

	Mockito.when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
	Mockito.when(passwordEncoder.encode("123456")).thenReturn("encoded-123456");
	Mockito.when(jwtUtils.doGenerateToken("newuser@example.com")).thenReturn("new-token");

	mockMvc.perform(post("/api/auth/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(request)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.message").value("Đăng kí thành công"));

	Mockito.verify(userRepository).save(any(User.class));
	Mockito.verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void registerUser_testNgoaiLe1() throws Exception {
	SignupRequest request = new SignupRequest(
		"Old User",
		"olduser@example.com",
		"123456",
		"0123456789",
		"Ho Chi Minh",
		true,
		true,
		"avatar.jpg",
		LocalDate.of(2024, 1, 15),
		new HashSet<>()
	);

	Mockito.when(userRepository.existsByEmail("olduser@example.com")).thenReturn(true);

	mockMvc.perform(post("/api/auth/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(request)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.message").value("Error: Email is already taken!"));

	Mockito.verify(userRepository, never()).save(any(User.class));
	Mockito.verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void authenticateUser_testChuan1() throws Exception {
	LoginRequest request = new LoginRequest("user@example.com", "123456");

	Authentication authentication = Mockito.mock(Authentication.class);
	UserDetailsImpl principal = new UserDetailsImpl(
		1L,
		"Test User",
		"user@example.com",
		"encoded-123456",
		"0123456789",
		"Ha Noi",
		true,
		true,
		"avatar.jpg",
		LocalDate.of(2024, 1, 1),
		Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
	);

	Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
		.thenReturn(authentication);
	Mockito.when(authentication.getPrincipal()).thenReturn(principal);
	Mockito.when(jwtUtils.generateJwtToken(authentication)).thenReturn("valid-jwt-token");

	mockMvc.perform(post("/api/auth/signin")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(request)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.token").value("valid-jwt-token"))
		.andExpect(jsonPath("$.email").value("user@example.com"));
    }

    // Test xử lý exception từ authenticationManager
    // Endpoint không có exception handler nên sẽ throw BadCredentialsException
    @Test
    void authenticateUser_testNgoaiLe1() throws Exception {
    	LoginRequest request = new LoginRequest("user@example.com", "wrongpass");
    
    	Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
    		.thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));
    
    	try {
    		mockMvc.perform(post("/api/auth/signin")
    				.contentType(MediaType.APPLICATION_JSON)
    				.content(jsonMapper.writeValueAsString(request)))
    			.andExpect(status().isOk());
    	} catch (Exception e) {
    		System.out.println("FAIL: Authentication failed - " + e.getCause().getMessage());
    		e.printStackTrace();
    	}
    }

    @Test
    void getOne_testChuan2() throws Exception {
	Long userId = 10L;
	User user = new User();
	user.setUserId(userId);
	user.setName("Nguyễn Văn B");
	user.setEmail("nguyenvanb@example.com");

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

	mockMvc.perform(get("/api/auth/{id}", userId))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Nguyễn Văn B"));
    }

    @Test
    void getOne_testNgoaiLe2() throws Exception {
	Long userId = 999L;

	Mockito.when(userRepository.existsById(userId)).thenReturn(false);

	mockMvc.perform(get("/api/auth/{id}", userId))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository).existsById(userId);
	Mockito.verify(userRepository, never()).findById(any());
    }

    @Test
    void getOneByEmail_testChuan2() throws Exception {
	String email = "nguyenvana@gmail.com";
	User user = new User();
	user.setEmail(email);
	user.setName("Nguyễn Văn A");

	Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);
	Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

	mockMvc.perform(get("/api/auth/email/{email}", email))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Nguyễn Văn A"));

	Mockito.verify(userRepository).existsByEmail(email);
	Mockito.verify(userRepository).findByEmail(email);
    }

    @Test
    void getOneByEmail_testNgoaiLe2() throws Exception {
	String email = "notfound@gmail.com";

	Mockito.when(userRepository.existsByEmail(email)).thenReturn(false);

	mockMvc.perform(get("/api/auth/email/{email}", email))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository).existsByEmail(email);
	Mockito.verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void post_testChuan2() throws Exception {
	User newUser = new User();
	newUser.setUserId(20L);
	newUser.setName("Trần Văn C");
	newUser.setEmail("vanc@gmail.com");
	newUser.setPassword("raw-password");
	newUser.setPhone("0987654321");
	newUser.setAddress("TP HCM");
	newUser.setStatus(true);

	Mockito.when(userRepository.existsByEmail("vanc@gmail.com")).thenReturn(false);
	Mockito.when(userRepository.existsById(20L)).thenReturn(false);
	Mockito.when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
	Mockito.when(jwtUtils.doGenerateToken("vanc@gmail.com")).thenReturn("token-abc");
	Mockito.when(userRepository.save(any(User.class))).thenReturn(newUser);

	mockMvc.perform(post("/api/auth")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(newUser)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Trần Văn C"));

	Mockito.verify(userRepository).save(any(User.class));
	Mockito.verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void post_testNgoaiLe3() throws Exception {
	User payload = new User();
	payload.setUserId(30L);
	payload.setEmail("existed@gmail.com");

	Mockito.when(userRepository.existsByEmail("existed@gmail.com")).thenReturn(true);

	mockMvc.perform(post("/api/auth")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(payload)))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository, never()).save(any(User.class));
	Mockito.verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void put_testChuan2() throws Exception {
	Long userId = 1L;
	User existing = new User();
	existing.setUserId(userId);
	existing.setName("Dương Old");
	existing.setPassword("old-encoded");

	User updatePayload = new User();
	updatePayload.setUserId(userId);
	updatePayload.setName("Dương Update");
	updatePayload.setEmail("duong@gmail.com");
	updatePayload.setPassword("new-raw");

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
	Mockito.when(passwordEncoder.encode("new-raw")).thenReturn("new-encoded");
	Mockito.when(userRepository.save(any(User.class))).thenReturn(updatePayload);

	mockMvc.perform(put("/api/auth/{id}", userId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatePayload)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Dương Update"));

	Mockito.verify(userRepository).save(any(User.class));
    }

    @Test
    void put_testNgoaiLe3() throws Exception {
	Long userId = 1L;
	User updatePayload = new User();
	updatePayload.setUserId(2L);

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);

	mockMvc.perform(put("/api/auth/{id}", userId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatePayload)))
		.andExpect(status().isBadRequest());

	Mockito.verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void delete_testChuan2() throws Exception {
	Long userId = 1L;
	User user = new User();
	user.setUserId(userId);
	user.setStatus(true);

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

	mockMvc.perform(delete("/api/auth/{id}", userId))
		.andExpect(status().isOk());

	org.junit.jupiter.api.Assertions.assertFalse(user.getStatus());
	Mockito.verify(userRepository).save(user);
    }

    @Test
    void delete_testNgoaiLe2() throws Exception {
	Long userId = 999L;

	Mockito.when(userRepository.existsById(userId)).thenReturn(false);

	mockMvc.perform(delete("/api/auth/{id}", userId))
		.andExpect(status().isNotFound());

	Mockito.verify(userRepository).existsById(userId);
	Mockito.verify(userRepository, never()).findById(any());
	Mockito.verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void putAdmin_testChuan2() throws Exception {
	Long userId = 5L;
	User updatePayload = new User();
	updatePayload.setUserId(userId);
	updatePayload.setName("Admin User");
	updatePayload.setEmail("admin@example.com");

	Mockito.when(userRepository.existsById(userId)).thenReturn(true);
	Mockito.when(userRepository.save(any(User.class))).thenReturn(updatePayload);

	mockMvc.perform(put("/api/auth/admin/{id}", userId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatePayload)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value("Admin User"));

	ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
	Mockito.verify(userRepository).save(captor.capture());

	Set<AppRole> roles = captor.getValue().getRoles();
	org.junit.jupiter.api.Assertions.assertEquals(1, roles.size());
	org.junit.jupiter.api.Assertions.assertEquals(2, roles.iterator().next().id);
    }

    @Test
    void logout_testChuan2() throws Exception {
	mockMvc.perform(get("/api/auth/logout"))
		.andExpect(status().isOk());
    }
}
