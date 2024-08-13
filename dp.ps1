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
$ErrorActionPreference = "Stop"
& chcp 65001 >$null
$port = 22
$user = "develop"
$sdc_bin_home = "D:\home\wbb\code\isid\deps\streamsets-datacollector"

function log {
    param ([string]$l = "INFO", [string]$m, [string]$f = "log.txt")
    # 获取当前日期和时间
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss.fff"
    # 创建日志条目
    $l = $l.ToUpper()
    $content = "$timestamp $l $m"
    # 在控制台输出日志条目（可选）
    Write-Host $content
    # 将日志条目写入日志文件
    # $content | Out-File -Append -FilePath $logFile
}

log info "开始编译"
$ips = @()
foreach ($arg in $args) {$ips += $arg}
if ($ips.Count -eq 0) {
    $ips = @("10.65.10.146")
    $user = "root"
}

log info "commons-vfs2"
& mvn -DskipTests=true install -f commons-vfs2/pom.xml
foreach ($ip in $args) {
    log info "部署: $ip"
    & scp -rP ${port} commons-vfs2/target/commons-vfs2-2.10.1-SNAPSHOT.jar ${user}@${ip}:/opt/nsfocus/deps/streamsets-datacollector/streamsets-libs/streamsets-datacollector-basic-lib/lib/commons-vfs2-2.10.1-SNAPSHOT.jar >$null
    & ssh -p ${port} ${user}@${ip} "/opt/nsfocus/bin/start_streamsets.sh 2>/dev/null" >$null
}

