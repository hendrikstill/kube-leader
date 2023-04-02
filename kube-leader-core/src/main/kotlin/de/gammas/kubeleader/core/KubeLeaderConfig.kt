package de.gammas.kubeleader.core

import java.time.Duration

data class KubeLeaderConfig(
    val identity: String,
    val lockName: String,
    val namespace: String = "default",
    val leaseDuration: Duration = Duration.ofSeconds(10),
    val renewDeadline: Duration = Duration.ofSeconds(8),
    val retryPeriod: Duration = Duration.ofSeconds(2)
)
