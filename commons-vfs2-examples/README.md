# Test Provider(s) with the Shell

## Build modules in the parent folder

    mvn clean install

## Test `http` and `https` providers

    mvn -Pshell -Dhttp

## Test `http4` and `http4s` providers

    mvn -Pshell -Dhttp4

## Test `http`, `https`, `http4` and `http4s` providers together

    mvn -Pshell -Dhttp -Dhttp4

