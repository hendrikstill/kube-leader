package de.gammas.kubeleader.spring.boot.autoconfiguration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "kubeleader")
@ConstructorBinding
data class KubeLeaderProperties(
    val enabled: Boolean = true,
    val identity : String?,
    val lockName : String?,
    val namespace : String?,
    val leaseDuration: Duration?,
    val renewDeadline: Duration?,
    val retryPeriod: Duration?
)
