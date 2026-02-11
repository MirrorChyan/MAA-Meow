package com.aliothmoon.maameow.domain.state

enum class MaaExecutionState {
    IDLE,      // 空闲
    STARTING,  // 启动中
    RUNNING,   // 运行中
    ERROR      // 错误
}