package com.conduit.comment.domain.port.out;

import com.conduit.comment.domain.model.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentRepository {

  Comment save(Comment comment);

  List<Comment> findByArticleId(Long articleId);

  Optional<Comment> findById(Long id);

  void deleteById(Long id);
}
