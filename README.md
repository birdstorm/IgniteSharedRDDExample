### Demo Instructions

首先确定已经下载[apache ignite binary](http://mirrors.tuna.tsinghua.edu.cn/apache//ignite/2.3.0/apache-ignite-fabric-2.3.0-bin.zip)

在`~/.bash_profile`或`~/.profile`中设置全局环境变量`$IGNITE_HOME`为binary地址

在`$SPARK_HOME/spark-env.sh`的最后加入以下几行
```
# Optionally set IGNITE_HOME here.
# IGNITE_HOME=/path/to/ignite

IGNITE_LIBS="${IGNITE_HOME}/libs/*"

for file in ${IGNITE_HOME}/libs/*
do
    if [ -d ${file} ] && [ "${file}" != "${IGNITE_HOME}"/libs/optional ]; then
        IGNITE_LIBS=${IGNITE_LIBS}:${file}/*
    fi
done

export SPARK_CLASSPATH=$IGNITE_LIBS
```

回到项目根目录，运行`mvn clean install`，将在target目录看到`target/ignitetest-1.0-SNAPSHOT-jar-with-dependencies.jar`

在根目录下先启动一个ignite node
```
$IGNITE_HOME/bin/ignite.sh src/main/resources/config/example-shared-rdd.xml 
```

然后
```
spark-submit \
    --class com.pingcap.ignite.sparkjob.SharedRDDExample \
    --master local[2] \
    ./target/ignitetest-1.0-SNAPSHOT-jar-with-dependencies.jar
```

可以观察sharedRDD在不同spark job中的共享