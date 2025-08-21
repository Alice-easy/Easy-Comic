package com.easycomic.memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 对象池管理器
 * 
 * 功能：
 * 1. 管理多种类型的对象池
 * 2. 自动回收和复用对象
 * 3. 池大小控制和监控
 * 4. 线程安全的对象获取和归还
 */
class ObjectPoolManager {
    
    // 存储不同类型的对象池
    private val pools = ConcurrentHashMap<String, ObjectPool<*>>()
    
    // 池操作互斥锁
    private val poolMutex = Mutex()
    
    /**
     * 获取或创建对象池
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getPool(
        poolName: String,
        factory: () -> T,
        reset: (T) -> Unit = {},
        maxSize: Int = DEFAULT_POOL_SIZE
    ): ObjectPool<T> {
        return pools.getOrPut(poolName) {
            ObjectPool(factory, reset, maxSize)
        } as ObjectPool<T>
    }
    
    /**
     * 移除对象池
     */
    suspend fun removePool(poolName: String) = poolMutex.withLock {
        pools.remove(poolName)?.clear()
    }
    
    /**
     * 清理所有对象池
     */
    suspend fun clearAllPools() = poolMutex.withLock {
        pools.values.forEach { it.clear() }
        pools.clear()
    }
    
    /**
     * 获取所有池的统计信息
     */
    fun getPoolStats(): Map<String, ObjectPoolStats> {
        return pools.mapValues { (_, pool) ->
            ObjectPoolStats(
                poolSize = pool.size(),
                maxSize = pool.maxSize,
                totalCreated = pool.totalCreated,
                totalReused = pool.totalReused
            )
        }
    }
    
    companion object {
        private const val DEFAULT_POOL_SIZE = 10
    }
}

/**
 * 通用对象池实现
 */
class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit = {},
    val maxSize: Int = 10
) {
    private val pool = ConcurrentLinkedQueue<T>()
    private val mutex = Mutex()
    
    @Volatile
    var totalCreated = 0L
        private set
    
    @Volatile
    var totalReused = 0L
        private set
    
    /**
     * 获取对象
     */
    suspend fun acquire(): T = mutex.withLock {
        val obj = pool.poll()
        return if (obj != null) {
            totalReused++
            obj
        } else {
            totalCreated++
            factory()
        }
    }
    
    /**
     * 归还对象
     */
    suspend fun release(obj: T) = mutex.withLock {
        if (pool.size < maxSize) {
            reset(obj)
            pool.offer(obj)
        }
    }
    
    /**
     * 获取当前池大小
     */
    fun size(): Int = pool.size
    
    /**
     * 清空对象池
     */
    suspend fun clear() = mutex.withLock {
        pool.clear()
    }
}

/**
 * 对象池统计信息
 */
data class ObjectPoolStats(
    val poolSize: Int,
    val maxSize: Int,
    val totalCreated: Long,
    val totalReused: Long
) {
    val reuseRate: Float
        get() = if (totalCreated + totalReused > 0) {
            totalReused.toFloat() / (totalCreated + totalReused)
        } else 0f
}

/**
 * 预定义的对象池类型
 */
object CommonObjectPools {
    
    /**
     * ByteArray对象池（用于文件读取缓冲区）
     */
    fun getByteArrayPool(poolManager: ObjectPoolManager, bufferSize: Int = 8192): ObjectPool<ByteArray> {
        return poolManager.getPool(
            poolName = "ByteArray_$bufferSize",
            factory = { ByteArray(bufferSize) },
            reset = { /* ByteArray不需要重置 */ },
            maxSize = 5
        )
    }
    
    /**
     * StringBuilder对象池（用于字符串构建）
     */
    fun getStringBuilderPool(poolManager: ObjectPoolManager): ObjectPool<StringBuilder> {
        return poolManager.getPool(
            poolName = "StringBuilder",
            factory = { StringBuilder() },
            reset = { it.clear() },
            maxSize = 10
        )
    }
    
    /**
     * ArrayList对象池（用于临时列表）
     */
    fun <T> getArrayListPool(poolManager: ObjectPoolManager, poolName: String = "ArrayList"): ObjectPool<ArrayList<T>> {
        return poolManager.getPool(
            poolName = poolName,
            factory = { ArrayList<T>() },
            reset = { it.clear() },
            maxSize = 8
        )
    }
    
    /**
     * HashMap对象池（用于临时映射）
     */
    fun <K, V> getHashMapPool(poolManager: ObjectPoolManager, poolName: String = "HashMap"): ObjectPool<HashMap<K, V>> {
        return poolManager.getPool(
            poolName = poolName,
            factory = { HashMap<K, V>() },
            reset = { it.clear() },
            maxSize = 8
        )
    }
}

/**
 * 对象池使用的扩展函数
 */
suspend inline fun <T, R> ObjectPool<T>.use(block: (T) -> R): R {
    val obj = acquire()
    try {
        return block(obj)
    } finally {
        release(obj)
    }
}