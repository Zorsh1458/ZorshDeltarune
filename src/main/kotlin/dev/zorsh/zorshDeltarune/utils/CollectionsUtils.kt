package dev.zorsh.zorshDeltarune.utils

operator fun <U> List<U>?.plus(element: U): List<U> {
    return (this ?: listOf()) + element
}

private operator fun <K, V> MutableMap<K, List<V>>.set(dPlayer: K, value: List<V>) {
    set(dPlayer, value)
}