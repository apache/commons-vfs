#!/bin/sh

##   Licensed to the Apache Software Foundation (ASF) under one or more
##   contributor license agreements.  See the NOTICE file distributed with
##   this work for additional information regarding copyright ownership.
##   The ASF licenses this file to You under the Apache License, Version 2.0
##   (the "License"); you may not use this file except in compliance with
##   the License.  You may obtain a copy of the License at
## 
##       http://www.apache.org/licenses/LICENSE-2.0
## 
##   Unless required by applicable law or agreed to in writing, software
##   distributed under the License is distributed on an "AS IS" BASIS,
##   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##   See the License for the specific language governing permissions and
##   limitations under the License.

set -e

PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

cd $PRGDIR/../../..

if [ ! -d target/test-classes/test-data/read-tests ]
then
	echo "Please run maven to have the environment setup correctly"
	exit 1
fi

if [ ! -x "`type -p jar`" ]
then
	echo "cant execute jar?"
	exit 1
fi
if [ ! -x "`type -p tar`" ]
then
	echo "cant execute tar?"
	exit 1
fi
if [ ! -x "`type -p zip`" ]
then
	echo "cant execute zip?"
	exit 1
fi
if [ ! -x "`type -p bzip2`" ]
then
        echo "cant execute bzip2?"
        exit 1
fi

cd target/test-classes/test-data
mkdir read-tests/emptydir

rm -f test.jar test.tar test.tbz2 test.tgz test.zip
rm -f nested.jar nested.tar nested.tbz2 nested.tgz nested.zip

echo "Creating test.jar ..."
jar -cvfm test.jar test.mf read-tests code

echo "Creating test.tar ..."
tar cvf test.tar read-tests code

echo "Creating test.tbz2 ..."
tar cjvf test.tbz2 read-tests code

echo "Creating test.tgz ..."
tar czvf test.tgz read-tests code

echo "Creating test.zip ..."
zip -r test.zip read-tests code

echo "Creating nested.jar ..."
jar cvf nested.jar test.jar

echo "Creating nested.tar ..."
tar cvf nested.tar test.tar

echo "Creating nested.tbz2 ..."
tar cjvf nested.tbz2 test.tbz2

echo "Creating nested.tgz ..."
tar czvf nested.tgz test.tgz

echo "Creating nested.zip ..."
zip nested.zip test.zip

for i in test.jar test.tar test.tbz2 test.tgz test.zip nested.jar nested.tar nested.tbz2 nested.tgz nested.zip
do
	cp $i ../../../src/test/resources/test-data/$i
done

echo Done.

