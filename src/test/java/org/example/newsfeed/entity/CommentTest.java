package org.example.newsfeed.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {
    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    public void setUp() {
        user = User.builder()
            .userId("testUserId")
            .password("$2a$10$abcdefg1234567")
            .name("Test User")
            .email("test@example.com")
            .build();

        post = Post.builder()
            .user(user)
            .content("Post content")
            .build();

        comment = Comment.builder()
            .user(user)
            .content("Initial comment content")
            .build();

        comment.setPost(post);
    }

    @Test
    @DisplayName("빌더 테스트")
    public void testCommentBuilder() {
        assertNotNull(comment);
        assertEquals("Initial comment content", comment.getContent());
        assertEquals(user, comment.getUser());
        assertEquals(post, comment.getPost());
    }

    @Test
    @DisplayName("댓글 내용을 올바르게 업데이트 하는지 검증")
    public void testSetContent() {
        comment.setContent("Updated comment content");
        assertEquals("Updated comment content", comment.getContent());
    }

    @Test
    @DisplayName("댓글이 연결된 게시물을 올바르게 설정하는지 테스트")
    public void testSetPost() {
        Post newPost = Post.builder()
            .user(user)
            .content("New post content")
            .build();

        comment.setPost(newPost);

        assertEquals(newPost, comment.getPost());
    }
}