package com.arfian.story

import com.arfian.story.data.room.StoryEntity


object DataDummy {
 
    fun generateDummyQuoteResponse(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = StoryEntity(
                i.toString(),
                "created at $i",
                "name $i",
                "description $i",
                i.toString(),
                i.toDouble(),
                i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}