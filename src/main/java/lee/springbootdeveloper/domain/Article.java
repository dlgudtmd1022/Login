package lee.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.Builder;

@Entity // 엔터티로 지정
public class Article {

    @Id // id필드를 기본키로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 자동으로 1씩 증가
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false) // 'title'이라는 not null 컬럼과 매핑
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Builder // 빌더패턴으로 객체 생성
    public Article(String title, String content){
        this.title = title;
        this.content = content;
    }

    protected Article() { // 기본 생성자
    }

    public Long getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getContent(){
        return content;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }
}

