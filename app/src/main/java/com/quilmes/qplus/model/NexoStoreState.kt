package com.quilmes.qplus.model

import com.elstatgroup.elstat.sdk.model.identifier.NexoBluetoothIdentifier

data class NexoStoreState(
        val detectedControllers: List<NexoBluetoothIdentifier> = listOf(),
        val expectedControllers: List<NexoBluetoothIdentifier> = listOf(),
        val decommissionedControllers: List<NexoBluetoothIdentifier> = listOf(),
        val syncedControllers: List<NexoBluetoothIdentifier> = listOf(),
        val syncProgress: Map<NexoBluetoothIdentifier, Int> = mapOf()
)