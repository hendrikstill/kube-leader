package de.gammas.kubeleader.spring.boot.autoconfiguration

import de.gammas.kubeleader.core.KubeLeader
import de.gammas.kubeleader.core.KubeLeaderConfig
import de.gammas.kubeleader.spring.configure.KubeLeaderAspect
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.task.TaskExecutorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import java.net.InetAddress
import java.time.Duration

@Configuration
@ConditionalOnClass(KubeLeader::class)
@EnableConfigurationProperties(KubeLeaderProperties::class)
@EnableAsync
class KubeLeaderAutoConfiguration(
    private val kubeLeaderProperties: KubeLeaderProperties,
    private val environment: Environment,
    @Qualifier("applicationTaskExecutor")
    private val asyncTaskExecutor: AsyncTaskExecutor,
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
    fun kubeLeader(kubeLeaderConfig: KubeLeaderConfig): KubeLeader {
        return KubeLeader(kubeLeaderConfig = kubeLeaderConfig).runAsync()
    }

    @Bean
    @ConditionalOnMissingBean
    fun kubeLeaderAspect(kubeLeader: KubeLeader) = KubeLeaderAspect(kubeLeader)

    //TODO Only Start directly when on K8S or explicitly enabled
    private fun getAppName() = environment.getProperty("spring.application.name") ?: "undefined"
    private fun getHostname() = InetAddress.getLocalHost().hostName

    private fun KubeLeader.runAsync() = this.apply {
        asyncTaskExecutor.execute { run() } }
}