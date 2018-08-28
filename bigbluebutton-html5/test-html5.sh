# Change to HTML5 directory
cd $(dirname $0)
echo "Working directory: $PWD"

# Build and run Docker image
docker build -t b2 .
docker=$(docker run -d -p 80:80/tcp -p 443:443/tcp -p 1935:1935 -p 5066:5066 -p 3478:3478 -p 3478:3478/udp b2 -h localhost)
echo $docker

# Check if HTML5 client is ready 
cd tests/puppeteer
node test-html5-check.js
status=$?
echo $status

conf=$(docker exec $(docker ps -q) bbb-conf --secret | grep "Secret:")
secret=$(echo $conf | cut -d' ' -f2)
export BBB_SHARED_SECRET=$secret
echo $BBB_SHARED_SECRET

# Run tests
if [ $status -eq 0 ]; then
  node test-chat.js
  node test-draw.js
  node test-upload.js
  node test-switch-slides.js
  node test-status.js
fi

# Stop Docker container
docker stop $docker
