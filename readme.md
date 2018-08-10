```ssh
docker run -d -p 9200:9200 -p 9300:9300 --name spring-boot-elastic-over-http -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "cluster.name=elasticsearch" docker.elastic.co/elasticsearch/elasticsearch:6.3.1
```

## Read all tweets

```ssh
curl -i -H "Accept: application/json" --request GET http://localhost:8080/
```

## Post a new tweet

```ssh
curl -i -H "Content-Type: application/json" --request POST --data 'hello, this is a tweet!' http://localhost:8080/      
```

## Read a specific tweet

```ssh
curl -i -H "Accept: application/json" --request GET http://localhost:8080/{tweet-id}      
```

## Delete a specific tweet

```ssh
curl -i -H "Accept: application/json" --request DELETE http://localhost:8080/{tweet-id}      
```

## Update a specific tweet

```ssh
curl -i -H "Content-Type: application/json" "Accept: application/json" --request PUT --data 'hello, this is a changed tweet!' http://localhost:8080/{tweet-id}      
```
