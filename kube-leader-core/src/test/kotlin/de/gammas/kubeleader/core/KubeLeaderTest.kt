package de.gammas.kubeleader.core

import org.awaitility.Awaitility.await
import org.awaitility.Duration.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
class KubeLeaderTest {

    private val kubeLeaderThreadForCleanUp = mutableListOf<KubeLeaderThread>()


    @Test
    fun `assert that only one leader exists when 100 instances are spawned concurrently`() {
        val kubeLeaderThreads = (1..100)
            .map { i -> startKubeLeaderAsync("$i") }
        Thread.sleep(TEN_SECONDS.valueInMS)
        await().timeout(TWO_MINUTES).until { kubeLeaderThreads.getLeaders().isNotEmpty() }
        assertEquals(1, kubeLeaderThreads.getLeaders().size)
    }

    @Test
    fun `assert that non leader will become leader when current leader is stopped `() {
        val currentLeaderThread = startKubeLeaderAsync("currentLeader")
        await().timeout(TWO_MINUTES).until { currentLeaderThread.first.isLeader() }


        val nonLeaderThread = startKubeLeaderAsync("nonLeader")
        currentLeaderThread.second.interrupt()
        await().timeout(TWO_MINUTES).until { nonLeaderThread.first.isLeader() }
        assertEquals(true, nonLeaderThread.first.isLeader())
    }

    @Test
    fun `assert that leader will become leader again when it is restarted`() {
        val leaderThread = startKubeLeaderAsync("leader")
        await().timeout(TWO_MINUTES).until { leaderThread.first.isLeader() }
        assertEquals(true, leaderThread.first.isLeader())
        leaderThread.second.interrupt()

        val restartedLeaderThread = startKubeLeaderAsync("leader")
        await().timeout(TWO_MINUTES).until { restartedLeaderThread.first.isLeader() }
        assertEquals(true, restartedLeaderThread.first.isLeader())

    }

    @AfterEach
    fun cleanUpKubLeaderThreads(){
        kubeLeaderThreadForCleanUp.forEach {
            it.second.interrupt()

        }
        kubeLeaderThreadForCleanUp.clear()
    }

    private fun startKubeLeaderAsync(identity: String): KubeLeaderThread {
        val kubeLeaderConfig = KubeLeaderConfig(lockName = "test-app",identity= identity);
        val kubeLeader = KubeLeader(kubeLeaderConfig = kubeLeaderConfig)
        val thread = Thread(kubeLeader::run)
        val kubeLeaderThread =  KubeLeaderThread(kubeLeader, thread)

        kubeLeaderThreadForCleanUp.add(kubeLeaderThread)
        thread.start()

        return kubeLeaderThread
    }
    private fun List<KubeLeaderThread>.getLeaders() = this.filter { it.first.isLeader() }
}

typealias KubeLeaderThread = Pair<KubeLeader, Thread>
