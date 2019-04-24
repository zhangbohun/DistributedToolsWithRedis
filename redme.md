1.[x] 已实现功能
    1.[x] 基于 redis 实现分布式锁
    2.[x] 可重入（通过 JUC ReentrantLock 实现）
    3.[x] try-with-resources（实现 AutoCloseable 接口）
    4.[x] 分布式锁方法注解
    5.[x] 同步变量（可锁变量）功能
2.[ ] todo list
    1.[ ] 通过redis订阅机制实现可中断锁
    2.[ ] 公平锁，读写锁等等 JUC 并发工具（Semaphore, CountDownLatch, CyclicBarrier）