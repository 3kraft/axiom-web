package org.zalando.axiom.web.krueger

fun <T, R> List<Map<T, R>>.flatten() : Map<T, R> = this.flatMap { e -> e.entries }.toMapBy({ e -> e.key }, { e -> e.value })
