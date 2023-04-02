package de.gammas.kubeleader.spring.boot.autoconfiguration

import de.gammas.kubeleader.core.KubeLeader
import de.gammas.kubeleader.core.KubeLeaderConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.time.Duration

@Configuration
@ConditionalOnClass(KubeLeader::class)
@EnableConfigurationProperties(KubeLeaderProperties::class)
class KubeLeaderAutoConfiguration(
    private val kubeLeaderProperties: KubeLeaderProperties,
    private val environment: Environment
) {

    @Bean
    @ConditionalOnMissingBean
    fun kubeLeaderConfig(): KubeLeaderConfig {
        return KubeLeaderConfig(
            lockName = kubeLeaderProperties.lockName ?: getAppName(),
            identity = kubeLeaderProperties.identity ?: getHostname(),
            namespace = kubeLeaderProperties.namespace ?: "default",
            leaseDuration = kubeLeaderProperties.leaseDuration ?: Duration.ofSeconds(10),
            renewDeadline = kubeLeaderProperties.renewDeadline ?: Duration.ofSeconds(8),
            retryPeriod = kubeLeaderProperties.retryPeriod ?: Duration.ofSeconds(2)
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun kubeLeader(kubeLeaderConfig: KubeLeaderConfig) = KubeLeader(kubeLeaderConfig = kubeLeaderConfig).apply { run() }

    private fun getAppName() = environment.getProperty("spring.application.name") ?: "undefined"
    private fun getHostname() = InetAddress.getLocalHost().hostName

}