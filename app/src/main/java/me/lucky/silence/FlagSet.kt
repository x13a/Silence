package me.lucky.silence

interface FlagValue {
    val value: Int
}

class FlagSet<T : FlagValue>(
    val value: Int,
    private val allFlags: Iterable<T>,
) {
    fun has(flag: T): Boolean = value.and(flag.value) != 0

    fun with(flag: T, enabled: Boolean): FlagSet<T> = FlagSet(
        when (enabled) {
            true -> value.or(flag.value)
            false -> value.and(flag.value.inv())
        },
        allFlags,
    )

    fun active(): Sequence<T> = allFlags.asSequence().filter { has(it) }

    companion object {
        inline fun <reified T> from(
            value: Int,
        ) where T : Enum<T>, T : FlagValue =
            FlagSet(value, enumValues<T>().asIterable())
    }
}
