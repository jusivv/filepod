set -eu
echo "build version: $1"
mvn clean package -U -am -pl boot -DskipTests
docker build --build-arg="FILEPOD_VERSION=$1" -t filepod:$1 .