#!/bin/bash
DIR=`dirname $0`
rsync -aPv --delete ${DIR} standtke@www.imedias.ch:/home/standtke/lernstick/dlcopy