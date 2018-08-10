package de.larmic.springbootelasticsearchoverhttp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.larmic.springbootelasticsearchoverhttp.Tweet.Companion.documentIndex
import de.larmic.springbootelasticsearchoverhttp.Tweet.Companion.documentType
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.UUIDs
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.fetch.subphase.FetchSourceContext
import org.springframework.stereotype.Service
import java.util.*



@Service
class TweetRepository(private val restHighLevelClient: RestHighLevelClient) {

    private val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(KotlinModule())

    fun storeTweet(tweet: String): String {
        val id = UUIDs.base64UUID()
        val document = Tweet(id, tweet)
        val writeValueAsString = mapper.writeValueAsString(document)
        val request = IndexRequest(Tweet.documentIndex, Tweet.documentType, id)
        request.source(writeValueAsString, XContentType.JSON)
        return restHighLevelClient.index(request).id
    }

    fun getTweet(id: String): Tweet? {
        val getRequest = GetRequest(Tweet.documentIndex, Tweet.documentType, id)
        val getResponse = restHighLevelClient.get(getRequest)
        return when (getResponse.isExists) {
            true -> {
                val sourceAsString = getResponse.sourceAsString
                mapper.readValue(sourceAsString, Tweet::class.java)
            }
            false -> null
        }
    }

    fun getAllTweets(): List<Tweet> {
        // see https://www.elastic.co/guide/en/elasticsearch/client/java-rest/6.x/java-rest-high-search.html
        val searchRequest = SearchRequest(Tweet.documentIndex)
        searchRequest.types(Tweet.documentType)
        return restHighLevelClient.search(searchRequest).hits
                .map { it.sourceAsString }
                .map { mapper.readValue(it, Tweet::class.java) }
    }

    fun updateTweet(id: String, tweet: String): RestStatus {
        if (!tweetExists(id)) {
            return RestStatus.NOT_FOUND
        }

        val jsonMap = HashMap<String, Any>()
        jsonMap["message"] = tweet
        val request = UpdateRequest(Tweet.documentIndex, Tweet.documentType, id).doc(jsonMap, XContentType.JSON)
        val updateResponse = restHighLevelClient.update(request)
        return updateResponse.status()
    }

    fun deleteTweet(id: String): RestStatus {
        val request = DeleteRequest(Tweet.documentIndex, Tweet.documentType, id)
        val deleteResponse = restHighLevelClient.delete(request)
        return deleteResponse.status()
    }

    private fun tweetExists(id: String): Boolean {
        val getRequest = GetRequest(documentIndex, documentType, id)
        getRequest.fetchSourceContext(FetchSourceContext(false))
        getRequest.storedFields("_none_")
        return restHighLevelClient.exists(getRequest)
    }
}