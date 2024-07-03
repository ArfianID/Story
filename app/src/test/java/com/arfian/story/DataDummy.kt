package com.arfian.story

import com.arfian.story.data.service.responses.StoryItem

object DataDummy {
 
    fun generateDummyQuoteResponse(): List<StoryItem> {
        val items: MutableList<StoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = StoryItem(
                i.toString(),
                "created at $i",
                "name $i",
                "description $i",
                i.toDouble(),
                i.toString(),
                i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}