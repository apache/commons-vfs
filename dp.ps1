#Licensed to the Apache Software Foundation (ASF) under one or more
#contributor license agreements.  See the NOTICE file distributed with
#this work for additional information regarding copyright ownership.
#The ASF licenses this file to You under the Apache License, Version 2.0
#(the "License"); you may not use this file except in compliance with
#the License.  You may obtain a copy of the License at
#
#http://www.apache.org/licenses/LICENSE-2.0
#
#Unless required by applicable law or agreed to in writing, software
#distributed under the License is distributed on an "AS IS" BASIS,
#WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#See the License for the specific language governing permissions and
#limitations under the License.
$ENV_DICT = @{
    # 'key' = @("外板IP", "内板IP", 1: 强制使用透明模式)
    'private' = @("10.65.10.146", "10.65.10.146", 0)
    '101' = @("10.65.77.102", "10.65.77.101", 0)
    '105' = @("10.65.77.106", "10.65.77.105", 0)
    '47' = @("10.65.77.48", "10.65.77.47", 0)
    '49' = @("10.65.77.50", "10.65.77.49", 0)
    '83' = @("10.65.77.84", "10.65.77.83", 0)
    '51' = @("10.65.77.52", "10.65.77.51", 0)
    '129' = @("10.65.77.130", "10.65.77.129", 0)
    '145' = @("10.65.77.146", "10.65.77.145", 0)
}
if ($null -eq $args[0] -or "" -eq $args[0]) {
    $key = "private"
} else {
    $key = [string]$args[0]
}
$args = $ENV_DICT[$key]
Write-Host $args
$TRANSPARENT = $args[2]
$args = @($args[0])
$port = 22
$user = "root"


& mvn -DskipTests=true install -f pom.xml
foreach ($ip in $args) {
    Write-Host "部署: $ip"
    & scp -rP ${port} .\commons-vfs2\target\commons-vfs2-2.10.1-SNAPSHOT.jar ${user}@${ip}:/home/wbb/code/isid/deps/streamsets-datacollector/streamsets-libs/streamsets-datacollector-basic-lib/lib/commons-vfs2-2.10.1-SNAPSHOT.jar >$null
    & ssh -p ${port} ${user}@${ip} "/opt/nsfocus/bin/start_streamsets.sh 2>/dev/null" >$null
}

