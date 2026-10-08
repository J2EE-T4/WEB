package com.interacthub.dto.common;
import java.util.List;
public record PagedResponse<T>(List<T> data, Object nextCursor, boolean hasMore) {}
