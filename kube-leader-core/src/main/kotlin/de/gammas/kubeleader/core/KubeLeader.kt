package de.gammas.kubeleader.core

import io.kubernetes.client.extended.leaderelection.LeaderElectionConfig
import io.kubernetes.client.extended.leaderelection.LeaderElector
import io.kubernetes.client.extended.leaderelection.resourcelock.LeaseLock
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.Config
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

class KubeLeader(
    private val client: ApiClient = Config.defaultClient(),
    private val kubeLeaderConfig: KubeLeaderConfig
) {

    private val log = LoggerFactory.getLogger(KubeLeader::class.java)
    private val isLeader = AtomicBoolean(false)

    init {
        Configuration.setDefaultApiClient(client)
    }

    fun run() {
        log.info("Starting KubeLeader with configuration: {}",kubeLeaderConfig)

        val lock = LeaseLock(kubeLeaderConfig.namespace,kubeLeaderConfig.lockName, kubeLeaderConfig.identity)
        val leaderElectionConfig =
            LeaderElectionConfig(lock, kubeLeaderConfig.leaseDuration, kubeLeaderConfig.renewDeadline, kubeLeaderConfig.retryPeriod)

        LeaderElector(leaderElectionConfig).use { leaderElector ->
            leaderElector.run(
                {
                    log.debug("Getting Leadership for lock {} with identity {}",kubeLeaderConfig.lockName, kubeLeaderConfig.identity)
                    isLeader.set(true)
                },
                {
                    log.debug("Losing Leadership for lock {} with identity {}",kubeLeaderConfig.lockName, kubeLeaderConfig.identity)
                    isLeader.set(false)
                })
        }
    }
    fun isLeader(): Boolean {
        return isLeader.get()
    }
}