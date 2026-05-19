package com.conduit.comment.domain.port.in;

public interface DeleteCommentUseCase {

    void deleteComment(String slug, Long commentId, Long currentUserId);
}
