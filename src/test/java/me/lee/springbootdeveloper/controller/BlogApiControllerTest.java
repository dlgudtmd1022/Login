package me.lee.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.lee.springbootdeveloper.domain.Article;
import me.lee.springbootdeveloper.domain.User;
import me.lee.springbootdeveloper.dto.AddArticleRequest;
import me.lee.springbootdeveloper.dto.UpdateArticleRequest;
import me.lee.springbootdeveloper.repository.BlogRepository;
import me.lee.springbootdeveloper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화를 위한 클래스

    @Autowired
    private WebApplicationContext context;

    @Autowired
    BlogRepository blogRepository;

    // OAuth2 로그인 추가
    @Autowired
    UserRepository userRepository;

    // OAuth2 로그인 추가
    User user;



    @BeforeEach // 테스트 실행 전 실행하는 메서드
    public void mockMvcSetUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        blogRepository.deleteAll();
    }
    @BeforeEach // OAuth2 로그인 추가
    void setSecurityContext(){
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                        .email("user@gamil.com")
                        .password("test")
                .build());

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    @DisplayName("글 추가")
    @Test
    public void addArticle() throws Exception {
        //given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // OAuth2 로그인 추가
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .principal(principal) // OAuth2 로그인 추가
                        .content(requestBody));

        //  then
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertEquals(1, articles.size());
        assertEquals(title, articles.get(0).getTitle());
        assertEquals(content, articles.get(0).getContent());
    }

    @DisplayName("전체글 조회")
    @Test
    public void findAllArticles() throws Exception{
        // given
        final String url = "/api/articles";
//        final String title = "title"; OAuth2 로그인 수정
//        final String content = "content"; OAuth2 로그인 수정
        Article savedArticle = createDefaultArticle(); // OAuth2 로그인 추가

//        blogRepository.save(Article.builder() OAuth2 로그인 수정
//                        .title(title)
//                        .content(content)
//                        .build());
        // when
        final ResultActions resultActions = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].content").value(content)) OAuth2 로그인 수정
//                .andExpect(jsonPath("$[0].title").value(title)); OAuth2 로그인 수정
                .andExpect(jsonPath("$[0].content").value(savedArticle.getContent()))  //  OAuth2 로그인 추가
                .andExpect(jsonPath("$[0].title").value(savedArticle.getTitle())); //  OAuth2 로그인 추가
    }

    @DisplayName("글 조회")
    @Test
    public void findArticle() throws Exception {
        final String url = "/api/articles/{id}";
//        final String title = "title"; OAuth2 로그인 수정
//        final String content = "content"; OAuth2 로그인 수정
        Article savedArticle = createDefaultArticle(); // OAuth2 로그인 추가

//        Article savedArticle = blogRepository.save(Article.builder()  OAuth2 로그인 수정
//                .title(title)
//                .content(content)
//                .build());

        final ResultActions resultActions = mockMvc.perform(get(url, savedArticle.getId()));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(savedArticle.getContent()))  //  OAuth2 로그인 추가
                .andExpect(jsonPath("$.title").value(savedArticle.getTitle())); //  OAuth2 로그인 추가
    }

    @DisplayName("삭제")
    @Test
    public void deleteArticle()  throws Exception{
        final String url = "/api/articles/{id}";
//        final String title = "title"; OAuth2 로그인 수정
//        final String content = "content"; OAuth2 로그인 수정
        Article savedArticle = createDefaultArticle(); // OAuth2 로그인 추가

//        Article savedArticle = blogRepository.save(Article.builder()  OAuth2 로그인 수정
//                .title(title)
//                .content(content)
//                .build());

        mockMvc.perform(delete(url, savedArticle.getId()))
                .andExpect(status().isOk());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();
    }

    @DisplayName("업데이트")
    @Test
    public void updateArticle() throws Exception{
        final String url = "/api/articles/{id}";
//        final String title = "title"; OAuth2 로그인 수정
//        final String content = "content"; OAuth2 로그인 수정
        Article savedArticle = createDefaultArticle(); // OAuth2 로그인 추가

//        Article savedArticle = blogRepository.save(Article.builder()  OAuth2 로그인 수정
//                .title(title)
//                .content(content)
//                .build());

        final String newTitle = "new title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertEquals(newTitle, article.getTitle());
        assertEquals(newContent, article.getContent());

    }

    // OAuth2 로그인 추가
    private Article createDefaultArticle(){
        return blogRepository.save(Article.builder()
                .title("title")
                .author(user.getUsername())
                .content("content")
                .build());
    }
}