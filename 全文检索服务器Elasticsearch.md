-----------------全文检索服务器Elasticsearch-------------
一、elasticsearch介绍
    1、什么是elasticsearch？
        es是基于lucene的分布式的全文检索服务器，对外提供restful接口
    2、es原理
        正排索引：查字典时从第一页开始直到找到为止【通过文章找关键字】
        倒排索引：查字典时通过目录查找【通过关键字找文章】
        倒排索引[表]的组成：
            document(文档)
            term------>document(地址)
            term(分词)
    3、es的启动器
        elasticsearch-rest-high-level-client
        elasticsearch
    4、es的版本和客户端
        版本：6.2.3
        客户端：
            RestHignLevelClient：官方推荐
            TransportClient：8.0后会删除
二、es安装和启动
    1、安装
        a、设置虚拟机的内存>1.5G【重启】
        b、创建admin
        c、切换admin
        d、解压安装包【admin】
        e、修改elasticsearch.yml、jvm.options[admin]
        f、解决文件创建权限、内存问题[root]
    2、启动和关闭【admin】
        启动：
            ./elasticsearch
            ./elasticsearch -d
        关闭：
            ctrl+c
            或
            ps-ef|grep elasticsearch
            kill -9 pid
三、es快速入门
    1、index管理
        1、创建index
            PUT /java2104
            {
              "settings": {
                "number_of_shards": 2,
                "number_of_replicas": 0
              }
            }

            注意：一台服务器是，备份分片数量设置为0
        2、删除index
            DELETE /java2104
    2、type管理
        1、创建type
            POST /java2104/course/_mapping
            {
              "properties": {
                "name":{
                  "type": "text"
                },
                "description":{
                  "type": "text"
                },
                "studymodel":{
                  "type": "keyword"
                }
              }
            }
    3、document管理
        1、新增document
            POST /java2104/course
            {
              "name":".net从入门到放弃",
              "description":".net程序员谁都不服",
              "studymodel":"201003"
            }
        2、修改document
            PUT /java2104/course/2
            {
              "name":"php从入门到放弃",
              "description":"php是世界上最好的语言",
              "studymodel":"201001"
            }
        3、删除document
            DELETE /java2104/course/2
        4、查询document
            GET /java2104/course/1
四、ik分词器
    1、安装
        解压ik压缩包，把elasticsearch上传到plug，并重命名为ik
    2、自定义词库
        IKAnalyzer.cfg.xml：配置扩展词典和停用词典
        main.dic：扩展词典，例如：奥利给
        stopword.dic：停用词典，例如：a、an、the

        注意：main.dic和stopword.dic必须另存为UTF-8格式
    3、分词模式
        ik_max_word：细粒度
        ik_smart：粗粒度
五、field介绍
    1、field类型
        文本：text、keyword
        数字：integer、long、float、double

    2、field属性
        type：类型，例如：text、keyword、integer
        analyzer：分词方式，例如：ik_smart、ik_max_word
        index：是否索引，例如：true、false
        _source：是否存储，例如：excludes、includes

    3、选择field标准
                        标准           属性
        分词           是否有意思       type
        索引           是否搜索         index
        存储           是否展示         _source

        注意：type为keyword和数字、index为false 时无需关注analyzer
六、集群搭建
    1、步骤
        a、拷贝elasticsearch-2
        b、修改elasticsearch.yml
            node.name: usian_node_2
            discovery.zen.ping.unicast.hosts: ["192.168.204.135:9300", "192.168.204.136:9300"]
        c、删除节点2的data

    2、健康状况：
        红：主备死
        黄：主活备死
        绿：主备活

    3、几台？
        分2片，每片一个备份，共两台
