package de.gammas.kubeleader.core

import io.kubernetes.client.extended.leaderelection.LeaderElectionConfig
import io.kubernetes.client.extended.leaderelection.LeaderElector
import io.kubernetes.client.extended.leaderelection.resourcelock.EndpointsLock
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
    private val amILeader = AtomicBoolean(false)

    init {
        Configuration.setDefaultApiClient(client)
    }

    fun run() {
        log.info("{} Starting KubeLeader",kubeLeaderConfig.identity)

        val lock = EndpointsLock(kubeLeaderConfig.namespace,kubeLeaderConfig.lockName, kubeLeaderConfig.identity)
        val leaderElectionConfig =
            LeaderElectionConfig(lock, kubeLeaderConfig.leaseDuration, kubeLeaderConfig.renewDeadline, kubeLeaderConfig.retryPeriod)

        LeaderElector(leaderElectionConfig).use { leaderElector ->
            leaderElector.run(
                {
                    log.info("{} Start leading",kubeLeaderConfig.identity)
                    amILeader.set(true)
                },
                {
                    log.info("{}  Stop leading",kubeLeaderConfig.identity)
                    amILeader.set(false)
                })
        }
    }
    fun amILeader(): Boolean {
        return amILeader.get()
    }
}