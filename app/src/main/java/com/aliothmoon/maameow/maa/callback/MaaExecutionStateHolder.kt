package com.aliothmoon.maameow.maa.callback

import com.aliothmoon.maameow.domain.state.MaaExecutionState

interface MaaExecutionStateHolder {
    fun reportRunState(state: MaaExecutionState)
}
