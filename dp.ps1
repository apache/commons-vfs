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


& mvn -DskipTests=true package -f commons-vfs2/pom.xml
foreach ($ip in $args) {
    Write-Host "部署: $ip"
    & scp -rP ${port} .\commons-vfs2\target\commons-vfs2-2.10.0-SNAPSHOT.jar ${user}@${ip}:/home/wbb/code/isid/deps/streamsets-datacollector/streamsets-libs/streamsets-datacollector-basic-lib/lib/commons-vfs2-2.10.0-SNAPSHOT.jar >$null
    & ssh -p ${port} ${user}@${ip} "/opt/nsfocus/bin/start_streamsets.sh 2>/dev/null" >$null
}

