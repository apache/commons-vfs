#!/bin/sh

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

cd $PRGDIR/../..

if [ ! -d target/test-data/read-tests ]
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

cd target/test-data

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
	cp $i ../../src/test-data/$i
done
