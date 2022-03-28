package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.usian.mapper.SearchItemMapper;
import com.usian.pojo.SearchItem;
import com.usian.utils.JsonUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Loser
 * @date 2021年11月26日 21:22
 */
@Service
@Transactional
public class SearchItemServiceImpl implements SearchItemService{

    @Autowired
    private SearchItemMapper searchItemMapper;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${ES_INDEX_NAME}")
    private  String ES_INDEX_NAME;
    @Value("${ES_TYPE_NAME}")
    private  String ES_TYPE_NAME;

    @Override
    public Boolean importAll() {
            //要求: 每次导入1000条数据,批量请求ES
        try {
            if (!isExitsIndex()){
                createIndex();
            }
            int page = 1;
            while (true){
            PageHelper.startPage(page,1000);
            List<SearchItem> itemList = searchItemMapper.getItemList();
            if (itemList == null || itemList.size() == 0){
                break;
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (SearchItem searchItem : itemList) {
                bulkRequest.add(new IndexRequest(ES_INDEX_NAME,ES_TYPE_NAME).source(JsonUtils.objectToJson(searchItem), XContentType.JSON));
                }
                restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            page++;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SearchItem> list(String q, Long page, Integer pageSize) {

        try {
            //按照卖点,描述,描述,类别,查询商品:multi_match
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(q,new String[]{
                    "item_title","item_desc","item_sell_point","item_category_name"}));
            //设置搜索源
            SearchRequest searchRequest = new SearchRequest(ES_INDEX_NAME);
            searchRequest.types(ES_TYPE_NAME);

            //分页查询 ,page,pageSize
            //设置分页信息
            /**
             * page  from pageSize
             * 1       0    20
             * 2       20   20
             * 3       40   20
             * (page-1) * pageSize = from
             */
            Long from = (page - 1) * pageSize;
            searchSourceBuilder.from(from.intValue());
            searchSourceBuilder.size(pageSize);
            //高亮显示:设置高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<font color = 'red'>");
            highlightBuilder.postTags("</font>");
            //设置高亮的字段
            highlightBuilder.fields().add(new HighlightBuilder.Field("item_title"));
            searchSourceBuilder.highlighter(highlightBuilder);

            searchRequest.source(searchSourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //获取响应的对象中的商品信息
            SearchHit[] hits = response.getHits().getHits();
            //返回的是List<SearchItem>
            List<SearchItem> searchItemList = new ArrayList<>();
            for (int i = 0; i < hits.length; i++) {
                SearchHit hit = hits[i];
                SearchItem searchItem = JsonUtils.jsonToPojo(hit.getSourceAsString(), SearchItem.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields != null && highlightFields.size() > 0){
                    String item_title = highlightFields.get("item_title").getFragments()[0].toString();
                    //将高亮的字段替换为 item_title
                    searchItem.setItem_title(item_title);
                }
                searchItemList.add(searchItem);
            }
                return searchItemList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 索引库同步
     * @param itemId
     * @return
     */
    @Override
    public void insertDocument(String itemId) throws IOException {
        //根据商品id查询商品信息
        SearchItem searchItem = searchItemMapper.getItemById(itemId);
        IndexRequest indexRequest = new IndexRequest(ES_INDEX_NAME,ES_TYPE_NAME);
        indexRequest.source(JsonUtils.objectToJson(searchItem),XContentType.JSON);
        restHighLevelClient.index(indexRequest,RequestOptions.DEFAULT);
    }

    /**
     * 创建索引库与索引表
     * @throws IOException
     */
    private void createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(ES_INDEX_NAME);
        createIndexRequest.settings("{\n" +
                "    \"number_of_shards\": 2,\n" +
                "    \"number_of_replicas\": 1\n" +
                "  }",XContentType.JSON);

        createIndexRequest.mapping(ES_TYPE_NAME,"{\n" +
                "  \"_source\": {\n" +
                "    \"excludes\": [\n" +
                "      \"item_desc\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"id\":{\n" +
                "      \"type\": \"keyword\"\n" +
                "    },\n" +
                "    \"item_title\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_sell_point\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_price\": {\n" +
                "      \"type\": \"float\"\n" +
                "    },\n" +
                "    \"item_image\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"index\": false\n" +
                "    },\n" +
                "    \"item_category_name\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_desc\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    }\n" +
                "  }\n" +
                "}",XContentType.JSON);
        restHighLevelClient.indices().create(createIndexRequest,RequestOptions.DEFAULT);
    }

    /**
     * 判断是否存在索引库
     * @return
     * @throws IOException
     */
    public Boolean isExitsIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(ES_INDEX_NAME);
        return restHighLevelClient.indices().exists(getIndexRequest,RequestOptions.DEFAULT);
    }
}
