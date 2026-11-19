package com.github.stm7128.hycohyco.domain.util

import kotlinx.coroutines.CancellationException

/**
 * コルーチンのキャンセル例外 (CancellationException) を握りつぶさずに再スローする安全な runCatching 代替関数
 *
 * Kotlin 標準の `runCatching` は `Throwable` を無差別に捕捉するため、
 * コルーチンキャンセル時に親スコープへ例外が伝播せず構造化並行性を破壊する恐れがあります。
 * 本関数は `CancellationException` を検知した瞬間に確実に再スローし、その他の例外のみ `Result.failure` にラップします。
 */
inline fun <R> suspendRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (ce: CancellationException) {
        throw ce
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
