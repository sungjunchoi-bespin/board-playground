package com.conduit.tag.domain.port.in;

import java.util.List;

public interface GetTagsUseCase {

  List<String> getAllTagNames();
}
