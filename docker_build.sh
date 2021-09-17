#!/bin/bash

docker run --name rtmp  \
  -p 8080:8080 -p 1935:1935  \
  --expose=59000-65000 \
  -v nginx:/home/nginx \
  -itd alqutami/rtmp-hls

docker cp rtmp:/etc/nginx/nginx.conf nginx/nginx.conf.bak

docker cp  nginx/nginx.conf rtmp:/etc/nginx/nginx.conf