---------------全文检索服务器ElasticSearch----------------
一、ElasticSearch介绍
 1、什么是ElasticSearch？
    es是基于lucene的分布式的全文检索服务器，对外提供restful接口

 2、es原理
    正排索引：查字典时从第一页开始直到找到为止
    倒排索引：查字典时通过目录查找
    倒排索引表的组成：
            document
            term------->document
            term
 3、es的客户端
    TransportClient：8.0后会删除
    restHignLevelClient：官方推荐

 4、es的版本和启动器
    版本：6.x.x
    启动器：elasticsearch、elasticsearch-rest-high-level-client

二、es安装
 1、安装
    a、设置虚拟机内存>1.5G
    b、创建admin用户
    c、切换用户：su admin
    d、解压安装包
    e、修改elasticsearch.yml、jvm.options【root】
    d、解决线程、内存【root】

 2、启动和关闭
    启动：
        ./elasticsearch
        #或
        ./elasticsearch -d
    关闭：
        ctrl+c
        #
        ps-ef|grep elasticsearch
        kill -9 pid

三、es快速入门
 1、index管理
    a、创建index
        PUT /java2005
        {
          "settings": {
            "number_of_shards": 2,
            "number_of_replicas": 0
          }
        }
        注意：一台服务器时，备份分配数量设置为0

    b、修改index
        PUT /java2005/_settings
        {
          "number_of_replicas": 1
        }
        注意：index一旦创建，主分片数量不能被改变

    c、删除index
        DELETE /java2005

 2、mapping管理
    a、创建
        POST /java2005/course/_mapping
        {
            "properties":{
                "name":{
                    "type":"text"
                }
            }
        }

 3、document管理
    a、新增
        POST /java2005/course/1
        {
          "name":"php是世界上最好的语言",
          "description":"php从入门到自杀",
          "studymodel":"201003"
        }
    b、修改
        PUT /java2005/course/1
        {
          "name":"php是世界上最好的语言",
          "description":"php从入门到自杀",
          "studymodel":"201003"
        }
    c、查询
        GET java2005/course/1
    d、删除
            DELETE /java2005/course/3

四、ik分词器
 1、安装
    解压到plugs目录下，并重命名为ik

 2、自定义词库
    IKAnalyzer.cfg.xml：配置扩展词典和停用词典
    main.dic：扩展词典，例如：奥利给
    stopword.dic：停用词典，例如：a、an、the、的、地、得

    注意：扩展词典和停用词典必须另存为UTF-8

 3、分词模式
    ik_max_word：细粒度
    ik_smart：粗粒度

五、field的详细介绍
    1、常用的field类型
        文本：text、keyword
        数字：integer、long、float、double

    2、filed的属性
         type：text、integer
         analyzer：ik_smart、ik_max_word
         index：

    3、选择field的标准
                            标准          属性
          分词            是否有意义       type
          索引            是否搜索         index
          存储            是否展示         _source

          注意：index为false/type为keyword/数字 时无须关注analyzer属性

六、集群
 1、集群：2主分片，每片1个备份，2台服务器
 2、搭建步骤：
    a、拷贝节点1，命名为ElasticSearch-2
    b、修改elasticsearch.yml
        node.name: usian_node_2
        discovery.zen.ping.unicast.hosts: ["192.168.204.135:9300", "192.168.204.136:9300"]
    c、删除节点2的data目录
    d、测试

        总结：
            绿色：主备活
            黄色：主活备死
            红色：主备死




