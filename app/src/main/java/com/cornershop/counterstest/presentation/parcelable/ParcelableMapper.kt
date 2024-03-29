package com.cornershop.counterstest.presentation.parcelable

import com.cornershop.counterstest.entities.Counter

fun List<Counter>.toListCounterAdapter() = this.map {
    it.run {
        CounterAdapter(
            id,
            id_remote,
            title,
            count,
            false
        )
    }

}

fun List<CounterAdapter>.toListCounter() = this.map {
    it.run {
        Counter(
            id,
            id_remote,
            title,
            count
        )
    }

}

fun CounterAdapter.toCounterDomain() = Counter(
    id,
    id_remote,
    title,
    count
)