package de.gammas.kubeleader.spring.boot.aspect

import de.gammas.kubeleader.core.KubeLeader
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class KubeLeaderAspect(
    private val  kubeLeader: KubeLeader
) {
    private val log = LoggerFactory.getLogger(KubeLeaderAspect::class.java)

    @Around("@annotation(de.gammas.kubeleader.spring.boot.annotation.IfIsKubeLeader)")
    fun around(joinPoint: ProceedingJoinPoint){
        if (kubeLeader.isLeader()){
            log.debug("Process is leader so method is proceeded")
            joinPoint.proceed()
        } else {
            log.debug("Process is not leader so method is not proceeded")
        }
    }
}