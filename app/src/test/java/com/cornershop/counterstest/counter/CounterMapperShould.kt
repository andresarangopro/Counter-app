package com.cornershop.counterstest.counter

import com.cornershop.counterstest.entities.Counter
import com.cornershop.counterstest.presentation.parcelable.CounterAdapter
import com.cornershop.counterstest.presentation.parcelable.toListCounterAdapter
import com.cornershop.counterstest.utils.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CounterMapperShould:BaseUnitTest() {

    private val counterList = listOf(Counter("1","title",1))
    val counterAdapter = counterList.toListCounterAdapter()

    @Test
    fun keepSameId(){

        counterList.forEachIndexed(){index,item->
            assertEquals(item.id, counterAdapter[index].id)
        }

    }

    @Test
    fun keepSameTitle(){
        counterList.forEachIndexed(){index,item->
            assertEquals(item.title, counterAdapter[index].title)
        }
    }

    @Test
    fun keepSameCount(){
        counterList.forEachIndexed(){index,item->
            assertEquals(item.count,counterAdapter[index].count)
        }
    }

    @Test
    fun mapDefaultSelectedStatus(){
        counterList.forEachIndexed(){index,item->
            assertEquals(false,counterAdapter[index].selected)
        }

    }

}