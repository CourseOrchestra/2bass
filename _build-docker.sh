rm -rf target/2bass-*/
unzip target/2bass-*.zip -d target
docker build -t curs/2bass .
