package com.quilmes.qplus.model

enum class NexoStoreCoolerStatus {
    PENDING, SYNCED, NOT_FOUND, REQUIRES_COMMISSIONING
}

class NexoStoreCooler(val name: String,
                      val status: NexoStoreCoolerStatus,
                      val progress: Int)