#!/bin/bash

cd `dirname $0`
. _project.sh

export answer=y
export DBFLUTE_ENVIRONMENT_TYPE=integration

echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
echo "...Calling the ReplaceSchema task"
echo "nnnnnnnnnn/"
sh $DBFLUTE_HOME/etc/cmd/_df-replace-schema.sh $MY_PROPERTIES_PATH
taskReturnCode=$?

if [ $taskReturnCode -ne 0 ];then
  exit $taskReturnCode;
fi
