[![Build Status](https://travis-ci.org/navicore/supersix-jwt.svg?branch=master)](https://travis-ci.org/navicore/supersix-jwt)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6be03bf590ad4a5b88c2598b4456cb4f)](https://www.codacy.com/app/navicore/supersix-jwt?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=navicore/supersix-jwt&amp;utm_campaign=Badge_Grade)

Supersix JWT Service
===

Maintains actors per key and issues appropriate JWTs with claims and TTL

### To Run

* run Cassandra with Docker
```
docker run -p 9042:9042 --name my-cassandra -d cassandra:3.11
```

`sbt run`

