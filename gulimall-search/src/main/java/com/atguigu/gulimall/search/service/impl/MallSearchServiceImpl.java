package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: LDeng
 * @Date: 2020-12-19 13:28
 */

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    //去ES检索
    @Override
    public SearchResult search(SearchParam param) {
        //1, 动态构建出查询需要的DSL语句
        SearchResult result=null;
        //1.1 准备检索请求
        SearchRequest searchRequest= buildSearchRequest(param);


        try {
            //1.2 执行检索请求， 得到ES返回的结果 response
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //1.3 分析响应数据，封装成页面需要的格式
            result = buildSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }




    //准备检索请求===============================================
    /*
    #模糊匹配
    #过滤(属性，分类，品牌，价格区间，库存情况)
    #排序
    #分页
    #高亮
    #聚合分析
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//构建DSL语句
        /**
         * #模糊匹配#过滤(属性，分类，品牌，价格区间，库存情况)
         */
        //1, 构建bool-query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1 must-模糊匹配
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //1.2 filter-按照三级分类id查询
        if(param.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //1.2 filter-按照品牌id查询
        if(param.getBrandId()!=null && param.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }
        //1.3 filter-按照是否有库存进行查询
        boolQuery.filter(QueryBuilders.termQuery("hasStock",(param.getHasStock()==1)?true:false));
        //1.4 filter- 按照价格区间查询
        if(!StringUtils.isEmpty(param.getSkuPrice())){
            //价格区间的格式 //1_500/_500/500_
            /*
             "range": {
                        "skuPrice": {
                      "gte": 0,
                      "lte": 6000
                        }
                    }
             */
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if(s.length==2){//区间
                rangeQuery.gte(new BigDecimal(s[0]));
                rangeQuery.lte(new BigDecimal(s[1]));
            }else if(s.length==1){
                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(new BigDecimal(s[0]));
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(new BigDecimal(s[0]));
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //1.5 filter-所有指定的属性进行查询
        if(param.getAttrs()!=null && param.getAttrs().size()>0){
            for(String attrStr : param.getAttrs()){
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                //attr =1_5寸：8寸
                String[] s = attrStr.split("_");
                String attrId=s[0];//检索的属性id
                String[] attrValues = s[1].split(":");//检索的属性值
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrId",attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //每一个必须都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //把所有查询条件都拿来封装
        sourceBuilder.query(boolQuery);
        /**
         * 排序，分页，高亮，
         */
        //2.1 排序
        if(!StringUtils.isEmpty(param.getSort())){
            String sort=param.getSort();
            String[] s = sort.split("_");
            SortOrder order=s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0],order);
        }
        //2.2 分页
                //假设pagesize=5
                //pageNum:1 from:0 size:5 [0,1,2,3,4]
                //pageNum:2 from:5 size:5 [5,6,7,8,9]
                //from 计算方式： (pageNum-1)*pagesize
        sourceBuilder.from((param.getPageNumber()-1)*EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3 高亮
        if(!StringUtils.isEmpty(param.getKeyword())){//关键字不为空才高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }


        /**
         * 聚合分析
         */
        //1, 《《《《《《《《品牌聚合》》》》》》》
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //1.1 品牌聚合的子聚合，得到品牌的图片和名称
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //2, 《《《《《《《分类聚合》》》》》》》
        TermsAggregationBuilder catelog_agg = AggregationBuilders.terms("catelog_agg");
        catelog_agg.field("catalogId").size(20);
        //2.1分类聚合的子聚合，得到分类的名称
        catelog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catelog_agg);

        //3， 《《《《《《《属性聚合》》》》》》
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId, 得到商品有多少属性
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 聚合分析出当前attr_id对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr-name_agg").field("attrs.attrName").size(1));
        // 聚合分析出当前attr_id对应的所有可能的属性值 attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr-value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

     //   =========测试 DSL 语句==========
        String s = sourceBuilder.toString();
        System.out.println("构建的DSL：");
        System.out.println(s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    //构建结果数据==============================================
    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {

        SearchResult result = new SearchResult();
        //1, 返回所有查询到的商品
       // result.setProducts();
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels=new ArrayList<>();
        if(hits.getHits()!=null&&hits.getHits().length>0){
            for(SearchHit hit : hits.getHits()){
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel sku=new SkuEsModel();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //高亮标题
                if(!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);
        //2, 当前所有商品涉及到的所有属性信息
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        List<SearchResult.AttrVo> attrVos=new ArrayList<>();
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> buckets2 = attr_id_agg.getBuckets();
        for (Terms.Bucket bucket : buckets2) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //属性id
            Long attrId = bucket.getKeyAsNumber().longValue();
            //属性名字
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr-name_agg");
            String attr_name = attr_name_agg.getBuckets().get(0).getKeyAsString();
            //属性所有可能的值, 多个， 用字符串集合返回
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr-value_agg");
            List<String> attr_values = attr_value_agg.getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attr_name);
            attrVo.setAttrValue(attr_values);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        //3, 当前商品涉及到的品牌信息
        ParsedLongTerms brand_agg=response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brandVos=new ArrayList<>();
        List<? extends Terms.Bucket> buckets1 = brand_agg.getBuckets();
        for (Terms.Bucket bucket : buckets1) {
            SearchResult.BrandVo brandVo=new SearchResult.BrandVo();
            //id
            long brandId = bucket.getKeyAsNumber().longValue();
            //名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brand_name = brand_name_agg.getBuckets().get(0).getKeyAsString();
            //图片地址
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brand_img = brand_img_agg.getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brand_name);
            brandVo.setBrandImg(brand_img);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

       //4, 当前商品涉及到的分类信息
        ParsedLongTerms catelog_agg = response.getAggregations().get("catelog_agg");
        List<SearchResult.CatalogVo> catalogVos=new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catelog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到并设置vo分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //得到并设置vo分类名， 在子聚合中
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //5，分页信息-页码
          result.setPageNumber(param.getPageNumber());
       //6， 分页信息-总记录数
        Long total = hits.getTotalHits().value;
        result.setTotal(total);
        //7， 分页信息-总页码
        int totalPages = (int)((total%EsConstant.PRODUCT_PAGESIZE==0)?(total/EsConstant.PRODUCT_PAGESIZE):total/(EsConstant.PRODUCT_PAGESIZE+1));
        result.setTotalPages(totalPages);

        return result;
    }

}
