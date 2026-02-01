package com.jackyblackson.idunntemplates.core.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 模仿 TypeScript Promise 的 Bukkit 实现。
 * 特性：所有的 .then() 回调都会被调度到下一个 Server Tick 执行，确保线程安全。
 *
 * @param <T> 解析值的类型
 */
public class BukkitPromise<T> {

    private enum State {
        PENDING,
        FULFILLED,
        REJECTED
    }

    // 必须持有 Plugin 实例用于调度任务
    private final Plugin plugin;

    private State state = State.PENDING;
    private T value;
    private Throwable error;

    // 回调队列
    private final List<Consumer<T>> onFulfilledCallbacks = new ArrayList<>();
    private final List<Consumer<Throwable>> onRejectedCallbacks = new ArrayList<>();

    // 函数式接口定义
    @FunctionalInterface
    public interface Executor<T> {
        void execute(Resolver<T> resolve, Rejector reject) throws Exception;
    }

    @FunctionalInterface
    public interface Resolver<T> {
        void resolve(T value);
    }

    @FunctionalInterface
    public interface Rejector {
        void reject(Throwable error);
    }

    // --- 构造函数 ---

    /**
     * 创建一个新的 Promise。
     * executor 会立即被调度到下一个 Tick 执行（或者你可以选择立即执行，取决于具体需求，
     * 但为了符合题目"每次都在下个tick"，这里让构造函数内的逻辑也在下个tick跑）。
     */
    public BukkitPromise(Plugin plugin, Executor<T> executor) {
        this.plugin = plugin;
        scheduleNextTick(() -> {
            try {
                executor.execute(this::resolve, this::reject);
            } catch (Exception e) {
                this.reject(e);
            }
        });
    }

    // 私有构造，用于链式调用内部生成
    private BukkitPromise(Plugin plugin) {
        this.plugin = plugin;
    }

    // --- 核心逻辑 ---

    private void resolve(T value) {
        if (this.state != State.PENDING) return;

        // 处理 resolve 了一个 Promise 的情况 (Unwrapping)
        if (value instanceof BukkitPromise) {
            // 强制转换为泛型 T 的 Promise (Unchecked cast 是不可避免的，但在 Promise 逻辑中是安全的)
            BukkitPromise<T> nestedPromise = (BukkitPromise<T>) value;

            nestedPromise.then(
                    v -> {
                        this.resolve(v);
                        return null; // 显式返回 null 以满足 Function 接口要求
                    },
                    err -> {
                        this.reject(err);
                        return null; // 显式返回 null
                    }
            );
            return;
        }

        this.value = value;
        this.state = State.FULFILLED;
        executeCallbacks();
    }

    private void reject(Throwable error) {
        if (this.state != State.PENDING) return;
        this.error = error;
        this.state = State.REJECTED;
        executeCallbacks();
    }

    private void executeCallbacks() {
        if (state == State.PENDING) return;

        scheduleNextTick(() -> {
            if (state == State.FULFILLED) {
                for (Consumer<T> callback : onFulfilledCallbacks) {
                    callback.accept(value);
                }
            } else if (state == State.REJECTED) {
                for (Consumer<Throwable> callback : onRejectedCallbacks) {
                    callback.accept(error);
                }
            }
            // 清理回调，防止内存泄漏
            onFulfilledCallbacks.clear();
            onRejectedCallbacks.clear();
        });
    }

    /**
     * 调度到 Bukkit 主线程的下一个 Tick
     */
    private void scheduleNextTick(Runnable runnable) {
        // 如果插件被禁用了，调度可能会失败，这里简单处理
        if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    // --- API 方法 ---

    /**
     * TypeScript: then<R>(onFulfilled?: (value: T) => R | Promise<R>, onRejected?: (error: any) => R | Promise<R>): Promise<R>;
     */
    public <R> BukkitPromise<R> then(Function<T, R> onFulfilled) {
        return then(onFulfilled, null);
    }

    /**
     * 新增：支持无返回值的回调 (Consumer)，自动返回 BukkitPromise<Void>
     * 对应 TS: .then((v) => { console.log(v); }) // 不需要 return
     */
    public BukkitPromise<Void> then(Consumer<T> onFulfilled) {
        return then(
                value -> {
                    onFulfilled.accept(value);
                    return null; // 自动帮你返回 null，变成 Void 类型
                },
                null
        );
    }

    public <R> BukkitPromise<R> then(Function<T, R> onFulfilled, Function<Throwable, R> onRejected) {
        BukkitPromise<R> nextPromise = new BukkitPromise<>(this.plugin);

        // 处理成功的回调
        Consumer<T> successHandler = (val) -> {
            try {
                if (onFulfilled != null) {
                    R result = onFulfilled.apply(val);
                    nextPromise.resolve(result);
                } else {
                    // 透传值
                    nextPromise.resolve((R) val);
                }
            } catch (Exception e) {
                nextPromise.reject(e);
            }
        };

        // 处理失败的回调
        Consumer<Throwable> errorHandler = (err) -> {
            try {
                if (onRejected != null) {
                    R result = onRejected.apply(err);
                    nextPromise.resolve(result); // Catch 之后通常视为恢复正常，除非再次 throw
                } else {
                    nextPromise.reject(err); // 透传错误
                }
            } catch (Exception e) {
                nextPromise.reject(e);
            }
        };

        if (this.state == State.PENDING) {
            this.onFulfilledCallbacks.add(successHandler);
            this.onRejectedCallbacks.add(errorHandler);
        } else if (this.state == State.FULFILLED) {
            scheduleNextTick(() -> successHandler.accept(this.value));
        } else if (this.state == State.REJECTED) {
            scheduleNextTick(() -> errorHandler.accept(this.error));
        }

        return nextPromise;
    }

    /**
     * TypeScript: catch(onRejected: (reason: any) => PromiseLike<never> | never): Promise<T>;
     */
    public BukkitPromise<T> catchError(Function<Throwable, T> onRejected) {
        return then(null, onRejected);
    }

    // 简单的 Consumer 版本 catch，不改变返回值类型（通常用于链的末尾处理错误）
    public void catchException(Consumer<Throwable> onRejected) {
        then(null, err -> {
            onRejected.accept(err);
            return null;
        });
    }

    /**
     * TypeScript: finally(onFinally: () => void): Promise<T>;
     */
    public BukkitPromise<T> finallyRun(Runnable onFinally) {
        return then(
                val -> {
                    onFinally.run();
                    return val;
                },
                err -> {
                    onFinally.run();
                    // 抛出异常以保持 reject 状态，或者这里需要 trick 一下
                    // Java 中 Function 不能直接 throw checked exception，
                    // 但这里我们是在库内部。
                    // 为了简单起见，我们重新抛出 RuntimeException 或者透传错误
                    throw new RuntimeException(err);
                }
        );
    }

    // --- 静态工具方法 ---

    public static <T> BukkitPromise<T> resolve(Plugin plugin, T value) {
        return new BukkitPromise<>(plugin, (resolve, reject) -> resolve.resolve(value));
    }

    public static <T> BukkitPromise<T> reject(Plugin plugin, Throwable error) {
        return new BukkitPromise<>(plugin, (resolve, reject) -> reject.reject(error));
    }

    /**
     * 辅助方法：延迟特定 tick 数后 resolve
     */
    public static BukkitPromise<Void> delay(Plugin plugin, long ticks) {
        return new BukkitPromise<>(plugin, (resolve, reject) -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> resolve.resolve(null), ticks);
        });
    }
}
