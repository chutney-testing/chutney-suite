package com.chutneytesting.kotlin.synchronize

import com.chutneytesting.kotlin.annotations.Scenario
import com.chutneytesting.kotlin.dsl.ChutneyScenario
import com.chutneytesting.kotlin.util.ClassGraphUtil
import io.github.classgraph.ClassGraph
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

/**
 * Cosmetic to create a list of scenarios
 */
class SynchronizeScenariosBuilder {
    var scenarios: List<ChutneyScenario> = mutableListOf()

    companion object {
        fun scenariosBuilder(packageName: String): SynchronizeScenariosBuilder.() -> Unit = {
            ClassGraphUtil.findAllAnnotatedFunctions(packageName, Scenario::class).forEach { scenario: KFunction<*> ->
                +scenario
            }
        }
    }

    operator fun ChutneyScenario.unaryPlus() {
        scenarios = scenarios + this
    }

    operator fun List<ChutneyScenario>.unaryPlus() {
        scenarios = scenarios + this
    }

    operator fun <R> KFunction<R>.unaryPlus() {
        scenarios = scenarios +
            (this.call()?.let {
                when (it) {
                    is ChutneyScenario -> listOf(it)
                    is List<*> -> it.filterIsInstance<ChutneyScenario>()
                    else -> throw UnsupportedOperationException()
                }
            })!!
    }

    operator fun ChutneyScenario.unaryMinus() {
        // scenarios = scenarios - this
        // cosmetic to ignore scenario
    }
}
