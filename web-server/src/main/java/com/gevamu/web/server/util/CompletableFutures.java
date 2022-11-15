package com.gevamu.web.server.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@UtilityClass
public class CompletableFutures {
    public static <T> CompletionStage<T> completedStage(T value) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.complete(value);
        return future;
    }
    public static <T> CompletionStage<T> failedStage(@NonNull Throwable error) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }
}
