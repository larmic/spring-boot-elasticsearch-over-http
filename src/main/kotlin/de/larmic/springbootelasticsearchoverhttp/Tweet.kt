package de.larmic.springbootelasticsearchoverhttp

class Tweet(val id: String, val message: String) {
    companion object {
        const val documentIndex = "twitter"
        const val documentType = "tweet"
    }
}