package de.gammas.kubeleader.core

import io.kubernetes.client.extended.leaderelection.LeaderElectionConfig
import io.kubernetes.client.extended.leaderelection.LeaderElector
import io.kubernetes.client.extended.leaderelection.resourcelock.EndpointsLock
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.Config
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class KubeLeader(private val identity: String) {
    private val log = LoggerFactory.getLogger(KubeLeader::class.java)

    private val amILeader = AtomicBoolean(false)

    fun run() {
        log.info("{} Starting KubeLeader",identity)
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)

        val lock = EndpointsLock("default", "my-app", identity)
        val leaderElectionConfig =
            LeaderElectionConfig(lock, Duration.ofMillis(10_000), Duration.ofMillis(8_000), Duration.ofMillis(2_000))

        LeaderElector(leaderElectionConfig).use { leaderElector ->
            leaderElector.run(
                {
                    log.info("{} Start leading",identity)
                    amILeader.set(true)
                },
                {
                    log.info("{}  Stop leading",identity)
                    amILeader.set(false)
                },
                {
                    log.info("Some other got the lock")
                })
        }
    }
    fun amILeader(): Boolean {
        return amILeader.get()
    }
}